package main

import (
	"flag"
	"fmt"
	"log"
	"net/http"
	"net/url"
	"os"
	"strings"
	"time"

	"github.com/eclipse/che/exec-agent/auth"
	"github.com/eclipse/che/exec-agent/process"
	"github.com/eclipse/che/exec-agent/rest"
	"github.com/eclipse/che/exec-agent/rpc"
	"github.com/eclipse/che/exec-agent/term"
	"github.com/gorilla/mux"
)

var (
	AppHttpRoutes = []rest.RoutesGroup{
		process.HttpRoutes,
		rpc.HttpRoutes,
		term.HttpRoutes,
	}

	AppOpRoutes = []rpc.RoutesGroup{
		process.RpcRoutes,
	}

	serverAddress string
	staticDir     string
	basePath      string
	apiEndpoint   string

	authEnabled                      bool
	tokensExpirationTimeoutInMinutes uint
)

func init() {
	// server configuration
	flag.StringVar(
		&serverAddress,
		"addr",
		":9000",
		"IP:PORT or :PORT the address to start the server on",
	)
	flag.StringVar(
		&staticDir,
		"static",
		"./static/",
		"path to the directory where static content is located",
	)
	flag.StringVar(
		&basePath,
		"path",
		"",
		`the base path for all the rpc & rest routes, so route paths are treated not
	as 'server_address + route_path' but 'server_address + path + route_path'.
	For example for the server address 'localhost:9000', route path '/connect' and
	configured path '/api/' exec-agent server will register the following route:
	'localhost:9000/api/connect'.
	Regexp syntax is supported`,
	)

	// terminal configuration
	flag.StringVar(
		&term.Cmd,
		"cmd",
		"/bin/bash",
		"command to execute on slave side of the pty",
	)

	// workspace master server configuration
	flag.StringVar(
		&apiEndpoint,
		"api-endpoint",
		os.Getenv("CHE_API"),
		`api-endpoint used by exec-agent modules(such as activity checker or authentication)
	to request workspace master. By default the value from 'CHE_API' environment variable is used`,
	)

	// auth configuration
	flag.BoolVar(
		&authEnabled,
		"enable-auth",
		false,
		"whether authenicate requests on workspace master before allowing them to proceed",
	)
	flag.UintVar(
		&tokensExpirationTimeoutInMinutes,
		"tokens-expiration-timeout",
		auth.DefaultTokensExpirationTimeoutInMinutes,
		"how much time machine tokens stay in cache(if auth is enabled)",
	)

	// activity tracking
	flag.BoolVar(
		&term.ActivityTrackingEnabled,
		"enable-activity-tracking",
		false,
		"whether workspace master will be notified about terminal acitivity",
	)

	// process configuration
	flag.IntVar(
		&process.CleanupPeriodInMinutes,
		"process-cleanup-period",
		5,
		"how often processs cleanup job will be executed(in minutes)",
	)
	flag.IntVar(&process.CleanupThresholdInMinutes,
		"process-cleanup-threshold",
		60,
		`how much time will dead and unused process stay(in minutes),
	if -1 passed then processes won't be cleaned at all. Please note that the time
	of real cleanup is between configured threshold and threshold + process-cleanup-period.`,
	)
	curDir, _ := os.Getwd()
	curDir += string(os.PathSeparator) + "logs"
	flag.StringVar(
		&process.LogsDir,
		"logs-dir",
		curDir,
		"base directory for process logs",
	)
}

func main() {
	flag.Parse()

	log.SetOutput(os.Stdout)

	// print configuration
	fmt.Println("Exec-agent configuration")
	fmt.Println("  Server")
	fmt.Printf("    - Address: %s\n", serverAddress)
	fmt.Printf("    - Static content: %s\n", staticDir)
	fmt.Printf("    - Base path: '%s'\n", basePath)
	fmt.Println("  Terminal")
	fmt.Printf("    - Slave command: '%s'\n", term.Cmd)
	fmt.Printf("    - Activity tracking enabled: %t\n", term.ActivityTrackingEnabled)
	fmt.Println("  Authetnication")
	fmt.Printf("    - Enabled: %t\n", authEnabled)
	fmt.Printf("    - Tokens expiration timeout: %dm\n", tokensExpirationTimeoutInMinutes)
	fmt.Println("  Process executor")
	fmt.Printf("    - Logs dir: %s\n", process.LogsDir)
	fmt.Printf("    - Cleanup job period: %dm\n", process.CleanupPeriodInMinutes)
	fmt.Printf("    - Not used & dead processes stay for: %dm\n", process.CleanupThresholdInMinutes)
	if authEnabled || term.ActivityTrackingEnabled {
		fmt.Println("  Workspace master server")
		fmt.Printf("    - API endpoint: %s\n", apiEndpoint)
	}
	fmt.Println()

	term.ApiEndpoint = apiEndpoint

	// cleanup logs dir
	if err := os.RemoveAll(process.LogsDir); err != nil {
		log.Fatal(err)
	}

	basePath := basePath
	if basePath != "" {
		if strings.HasSuffix(basePath, "/") {
			basePath = basePath[:len(basePath)-1]
		}
		if strings.HasPrefix(basePath, "/") {
			basePath = basePath[1:]
		}
		basePath = "/{path:" + basePath + "}"
	}

	router := mux.NewRouter().StrictSlash(true)

	fmt.Print("⇩ Registered HttpRoutes:\n\n")
	for _, routesGroup := range AppHttpRoutes {
		fmt.Printf("%s:\n", routesGroup.Name)
		for _, route := range routesGroup.Items {
			route.Path = basePath + route.Path
			router.
				Methods(route.Method).
				Path(route.Path).
				Name(route.Name).
				HandlerFunc(rest.ToHttpHandlerFunc(route.HandleFunc))
			fmt.Printf("✓ %s\n", &route)
		}
		fmt.Println()
	}

	fmt.Print("\n⇩ Registered RpcRoutes:\n\n")
	for _, routesGroup := range AppOpRoutes {
		fmt.Printf("%s:\n", routesGroup.Name)
		for _, route := range routesGroup.Items {
			fmt.Printf("✓ %s\n", route.Method)
			rpc.RegisterRoute(route)
		}
	}

	if process.CleanupThresholdInMinutes > 0 {
		go process.CleanPeriodically()
	}
	if term.ActivityTrackingEnabled {
		go term.Activity.StartTracking()
	}

	router.PathPrefix("/").Handler(http.FileServer(http.Dir(staticDir)))

	var handler http.Handler

	if authEnabled {
		cache := auth.NewCache(time.Minute*time.Duration(tokensExpirationTimeoutInMinutes), time.Minute*5)

		handler = auth.Handler{
			Delegate:    router,
			ApiEndpoint: apiEndpoint,
			Cache:       cache,
			UnauthorizedHandler: func(w http.ResponseWriter, req *http.Request) {
				dropChannelsWithExpiredToken(req.URL.Query().Get("token"))
				http.Error(w, "Unauthorized", http.StatusUnauthorized)
			},
		}
	} else {
		handler = router
	}

	http.Handle("/", handler)

	server := &http.Server{
		Handler:      handler,
		Addr:         serverAddress,
		WriteTimeout: 10 * time.Second,
		ReadTimeout:  10 * time.Second,
	}
	log.Fatal(server.ListenAndServe())
}

func dropChannelsWithExpiredToken(token string) {
	for _, c := range rpc.GetChannels() {
		u, err := url.ParseRequestURI(c.RequestURI)
		if err != nil {
			log.Printf("Couldn't parse the RequestURI '%s' of channel '%s'", c.RequestURI, c.Id)
		} else if u.Query().Get("token") == token {
			log.Printf("Token for channel '%s' is expired, trying to drop the channel", c.Id)
			rpc.DropChannel(c.Id)
		}
	}
}
