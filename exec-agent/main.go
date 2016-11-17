package main

import (
	"flag"
	"fmt"
	"github.com/eclipse/che/exec-agent/auth"
	"github.com/eclipse/che/exec-agent/process"
	"github.com/eclipse/che/exec-agent/rest"
	"github.com/eclipse/che/exec-agent/rpc"
	"github.com/eclipse/che/exec-agent/term"
	"github.com/gorilla/mux"
	"log"
	"net/http"
	"os"
	"time"
	"strings"
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
	staticFlag    string
	pathFlag      string
)

func init() {
	// Server configuration
	flag.StringVar(&serverAddress, "addr", ":9000", "IP:PORT or :PORT the address to start the server on")
	flag.StringVar(&staticFlag, "static", "./static/", "path to static content")
	flag.StringVar(&pathFlag, "path", "", "Path of the pty server. Go regexp syntax is suported.")

	// Auth configuration
	flag.BoolVar(&auth.Enabled, "enable-auth", false, "Whether authenticate on workspace master or not")
	flag.StringVar(&auth.ApiEndpoint,
		"auth-api-endpoint",
		os.Getenv("CHE_API_ENDPOINT"),
		"Auth api-endpoint, by default 'CHEAPI-ENDPOINT' environment variable is used for this")

	// Process configuration
	flag.IntVar(&process.CleanupPeriodInMinutes, "process-cleanup-period", 2, "How often processs cleanup will happen(in minutes)")
	flag.IntVar(&process.CleanupThresholdInMinutes,
		"process-lifetime",
		60,
		"How much time will dead and unused process live(in minutes), if -1 passed then processes won't be cleaned at all")
	curDir, _ := os.Getwd()
	curDir += string(os.PathSeparator) + "logs"
	flag.StringVar(&process.LogsDir, "logs-dir", curDir, "Base directory for process logs")
}

func main() {
	flag.Parse()

	// print configuration
	fmt.Println("Exec-agent configuration")
	fmt.Printf("- Server address: %s\n", serverAddress)
	fmt.Printf("- Static content: %s\n", staticFlag)
	fmt.Printf("- Base path: '%s'\n", pathFlag)
	fmt.Printf("- Auth enabled: %t\n", auth.Enabled)
	fmt.Printf("- Auth endpoint: %s\n", auth.ApiEndpoint)
	fmt.Printf("- Process cleanup job period: %dm\n", process.CleanupPeriodInMinutes)
	fmt.Printf("- Not used & dead processes stay for: %dm\n", process.CleanupThresholdInMinutes)
	fmt.Printf("- Process logs dir: %s\n", process.LogsDir)
	fmt.Println()

	// cleanup logs dir
	if err := os.RemoveAll(process.LogsDir); err != nil {
		log.Fatal(err)
	}

	basePath := pathFlag
	if basePath != "" {
		if strings.HasSuffix(basePath, "/") {
			basePath = basePath[:len(basePath) - 1]
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

	router.PathPrefix("/").Handler(http.FileServer(http.Dir(staticFlag)))
	http.Handle("/", router)
	server := &http.Server{
		Handler:      router,
		Addr:         serverAddress,
		WriteTimeout: 10 * time.Second,
		ReadTimeout:  10 * time.Second,
	}
	log.Fatal(server.ListenAndServe())
}
