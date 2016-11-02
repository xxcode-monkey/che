#!/bin/bash
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

# Move this into a dedicated function that is only called when the variable is absolutely
# needed. This will speed performance for methods that do not need this value set.
init_host_arch() {
  GLOBAL_HOST_ARCH=${GLOBAL_HOST_ARCH:=$DEFAULT_GLOBAL_HOST_ARCH}
}

init_name_map() {
  GLOBAL_NAME_MAP=${GLOBAL_NAME_MAP:=$(docker info | grep "Name:" | cut -d" " -f2)}
}

init_host_ip() {
  GLOBAL_HOST_IP=${GLOBAL_HOST_IP:=$(docker run --net host --rm codenvy/che-ip:nightly)}
}

init_uname() {
  GLOBAL_UNAME=${GLOBAL_UNAME:=$(docker run --rm alpine sh -c "uname -r")}
#  GLOBAL_GET_DOCKER_HOST_IP=$(get_docker_host_ip)
}

cli_init() {
#  GLOBAL_NAME_MAP=$(docker info | grep "Name:" | cut -d" " -f2)
#  GLOBAL_HOST_ARCH=$(docker version --format {{.Client}} | cut -d" " -f5)
#  GLOBAL_HOST_IP=$(docker run --net host --rm codenvy/che-ip:nightly)
#  GLOBAL_UNAME=$(docker run --rm alpine sh -c "uname -r")
#  GLOBAL_GET_DOCKER_HOST_IP=$(get_docker_host_ip)

  # Odd - bash fails if this is embedded, so define it globally once
  DEFAULT_GLOBAL_HOST_ARCH=$(docker version --format {{.Client}} | cut -d" " -f5)

  DEFAULT_CHE_VERSION="nightly"
  CHE_VERSION=${CHE_VERSION:-${DEFAULT_CHE_VERSION}}

  DEFAULT_CHE_CLI_ACTION="help"
  CHE_CLI_ACTION=${CHE_CLI_ACTION:-${DEFAULT_CHE_CLI_ACTION}}

  # This is the IP address that Che is binding itself to.
  init_host_ip
  DEFAULT_CHE_HOST=$GLOBAL_HOST_IP
  CHE_HOST=${CHE_HOST:-${DEFAULT_CHE_HOST}}

  # This is the accessible hostname.
  # If Che is on docker for mac / windows, then this is set to localhost
  DEFAULT_CHE_HOSTNAME=$(get_docker_external_hostname)
  CHE_HOSTNAME=${CHE_HOSTNAME:-${DEFAULT_CHE_HOSTNAME}}

  DEFAULT_CHE_PORT="8080"
  CHE_PORT=${CHE_PORT:-${DEFAULT_CHE_PORT}}

  DEFAULT_CHE_CONFIG=$(get_mount_path $PWD)/config
  CHE_CONFIG=${CHE_CONFIG:-${DEFAULT_CHE_CONFIG}}

  DEFAULT_CHE_INSTANCE=$(get_mount_path $PWD)/instance
  CHE_INSTANCE=${CHE_INSTANCE:-${DEFAULT_CHE_INSTANCE}}

  DEFAULT_CHE_BACKUP_FOLDER=$(get_mount_path $PWD)
  CHE_BACKUP_FOLDER=${CHE_BACKUP_FOLDER:-${DEFAULT_CHE_BACKUP_FOLDER}}

  DEFAULT_CHE_DEVELOPMENT_MODE="off"
  DEFAULT_CHE_DEVELOPMENT_REPO=$(get_mount_path $PWD)
  CHE_DEVELOPMENT_MODE=${CHE_DEVELOPMENT_MODE:-${DEFAULT_CHE_DEVELOPMENT_MODE}}
  if [ "${CHE_DEVELOPMENT_MODE}" == "on" ]; then
    CHE_DEVELOPMENT_REPO=$(get_mount_path ${DEFAULT_CHE_DEVELOPMENT_REPO})
    if [[ ! -d "${CHE_DEVELOPMENT_REPO}"  ]]; then
      info "cli" "Development mode is on and could not find valid repo or packaged assembly"
      info "cli" "Set CHE_DEVELOPMENT_REPO to the root of your git clone repo"
      return 2;
    fi
    if [[ ! -d $(echo "${CHE_DEVELOPMENT_REPO}"/assembly/assembly-main/target) ]]; then
      info "cli" "Development mode is on and could not find assembly target"
      info "cli" "Have you 'mvn clean install' in /assembly/assembly-main yet?"
      return 2;
    else
      CHE_ASSEMBLY_ROOT=$(get_mount_path $(echo "${CHE_DEVELOPMENT_REPO}"/assembly/assembly-main/target))

      # Search for a zip file
      CHE_ASSEMBLY_BASE=$(ls -d "${CHE_ASSEMBLY_ROOT}"/eclipse-che-*.zip | cut -d "-" -f 4-)
      CHE_ASSEMBLY_VERSION=${CHE_ASSEMBLY_BASE%.zip}
      CHE_ASSEMBLY="${CHE_ASSEMBLY_ROOT}"/eclipse-che-$CHE_ASSEMBLY_VERSION/eclipse-che-$CHE_ASSEMBLY_VERSION

      if [[ ! -d "${CHE_ASSEMBLY}" ]]; then
        info "cli" "CHE_DEVELOPMENT_MODE=on is on and could not a valid Che assembly"
        info "cli" "We looked for $CHE_ASSEMBLY"
        return 2;
      fi
    fi
  fi

  ## Location where version manifests that list images are stored
  CHE_MANIFEST_DIR=$(get_mount_path ~/."${CHE_MINI_PRODUCT_NAME}"/manifests)

  ## Where images to be used offline are saved to
  CHE_OFFLINE_FOLDER=$(get_mount_path $PWD)/offline

  # Manifests files to be used by puppet when generating a configuration
  CHE_CONFIG_MANIFESTS_FOLDER="$CHE_CONFIG/manifests"

  # Modules to be used by puppet when generating a configuration
  CHE_CONFIG_MODULES_FOLDER="$CHE_CONFIG/modules"

  CHE_VERSION_FILE="che.ver"
  CHE_ENVIRONMENT_FILE="che.env"
  CHE_COMPOSE_FILE="docker-compose.yml"
  CHE_SERVER_CONTAINER_NAME="che"
  CHE_CONFIG_BACKUP_FILE_NAME="che_config_backup.tar"
  CHE_INSTANCE_BACKUP_FILE_NAME="che_instance_backup.tar"
  CHE_GLOBAL_VERSION_IMAGE="eclipse/che-version"

  # For some situations, Docker requires a path for volume mount which is posix-based.
  # In other cases, the same file needs to be in windows format
  if has_docker_for_windows_client; then
    REFERENCE_ENVIRONMENT_FILE=$(convert_posix_to_windows $(echo "${CHE_CONFIG}/${CHE_ENVIRONMENT_FILE}"))
    REFERENCE_COMPOSE_FILE=$(convert_posix_to_windows $(echo "${CHE_INSTANCE}/${CHE_COMPOSE_FILE}"))
  else
    REFERENCE_ENVIRONMENT_FILE="${CHE_CONFIG}/${CHE_ENVIRONMENT_FILE}"
    REFERENCE_COMPOSE_FILE="${CHE_INSTANCE}/${CHE_COMPOSE_FILE}"
  fi

  DOCKER_CONTAINER_NAME_PREFIX="che_"

  # TODO: Change this to use the current folder or perhaps ~?
  if is_boot2docker && has_docker_for_windows_client; then
    if [[ "${CHE_INSTANCE,,}" != *"${USERPROFILE,,}"* ]]; then
      CHE_INSTANCE=$(get_mount_path "${USERPROFILE}/.${CHE_MINI_PRODUCT_NAME}/")
      warning "Boot2docker for Windows - CHE_INSTANCE set to $CHE_INSTANCE"
    fi
    if [[ "${CHE_CONFIG,,}" != *"${USERPROFILE,,}"* ]]; then
      CHE_CONFIG=$(get_mount_path "${USERPROFILE}/.${CHE_MINI_PRODUCT_NAME}/")
      warning "Boot2docker for Windows - CHE_CONFIG set to $CHE_CONFIG"
    fi
  fi
}

### Should we load profile before we parse the command line?

cli_parse () {
  debug $FUNCNAME
  if [ $# -eq 0 ]; then
    CHE_CLI_ACTION="help"
  else
    case $1 in
      version|init|config|start|stop|restart|destroy|rmi|config|download|offline|info|network|debug|help|-h|--help)
        CHE_CLI_ACTION=$1
      ;;
      *)
        # unknown option
        error "You passed an unknown command line option."
        return 1;
      ;;
    esac
  fi
}

cli_cli() {
  case ${CHE_CLI_ACTION} in
    download)
      shift
      cmd_download "$@"
    ;;
    init)
      shift
      cmd_init "$@"
    ;;
    config)
      shift
      cmd_config "$@"
    ;;
    start)
      shift
      cmd_start "$@"
    ;;
    stop)
      shift
      cmd_stop "$@"
    ;;
    restart)
      shift
      cmd_restart "$@"
    ;;
    destroy)
      shift
      cmd_destroy "$@"
    ;;
    rmi)
      shift
      cmd_rmi "$@"
    ;;
    version)
      shift
      cmd_version "$@"
    ;;
    offline)
      shift
      cmd_offline
    ;;
    info)
      shift
      cmd_info "$@"
    ;;
    debug)
      shift
      cmd_debug "$@"
    ;;
    network)
      shift
      cmd_network "$@"
    ;;
    help)
      usage
    ;;
  esac
}

get_mount_path() {
  debug $FUNCNAME
  FULL_PATH=$(get_full_path "${1}")
  POSIX_PATH=$(convert_windows_to_posix "${FULL_PATH}")
  CLEAN_PATH=$(get_clean_path "${POSIX_PATH}")
  echo $CLEAN_PATH
}

get_full_path() {
  debug $FUNCNAME
  # create full directory path
  echo "$(cd "$(dirname "${1}")"; pwd)/$(basename "$1")"
}

convert_windows_to_posix() {
  debug $FUNCNAME
  echo "/"$(echo "$1" | sed 's/\\/\//g' | sed 's/://')
}

convert_posix_to_windows() {
  debug $FUNCNAME
  # Remove leading slash
  VALUE="${1:1}"

  # Get first character (drive letter)
  VALUE2="${VALUE:0:1}"

  # Replace / with \
  VALUE3=$(echo ${VALUE} | tr '/' '\\' | sed 's/\\/\\\\/g')

  # Replace c\ with c:\ for drive letter
  echo "$VALUE3" | sed "s/./$VALUE2:/1"
}

get_clean_path() {
  debug $FUNCNAME
  INPUT_PATH=$1
  # \some\path => /some/path
  OUTPUT_PATH=$(echo ${INPUT_PATH} | tr '\\' '/')
  # /somepath/ => /somepath
  OUTPUT_PATH=${OUTPUT_PATH%/}
  # /some//path => /some/path
  OUTPUT_PATH=$(echo ${OUTPUT_PATH} | tr -s '/')
  # "/some/path" => /some/path
  OUTPUT_PATH=${OUTPUT_PATH//\"}
  echo ${OUTPUT_PATH}
}

get_docker_host_ip() {
  debug $FUNCNAME
  case $(get_docker_install_type) in
   boot2docker)
     NETWORK_IF="eth1"
   ;;
   native)
     NETWORK_IF="docker0"
   ;;
   *)
     NETWORK_IF="eth0"
   ;;
  esac

  log "docker run --rm --net host \
            alpine sh -c \
            \"ip a show ${NETWORK_IF}\" | \
            grep 'inet ' | \
            cut -d/ -f1 | \
            awk '{ print \$2}'"
  docker run --rm --net host \
            alpine sh -c \
            "ip a show ${NETWORK_IF}" | \
            grep 'inet ' | \
            cut -d/ -f1 | \
            awk '{ print $2}'
}

has_docker_for_windows_client(){
  debug $FUNCNAME
  init_host_arch
  if [ "${GLOBAL_HOST_ARCH}" = "windows" ]; then
    return 0
  else
    return 1
  fi
}

get_docker_install_type() {
  debug $FUNCNAME
  if is_boot2docker; then
    echo "boot2docker"
  elif is_docker_for_windows; then
    echo "docker4windows"
  elif is_docker_for_mac; then
    echo "docker4mac"
  else
    echo "native"
  fi
}

is_boot2docker() {
  debug $FUNCNAME
  init_uname
  if echo "$GLOBAL_UNAME" | grep -q "boot2docker"; then
    return 0
  else
    return 1
  fi
}

is_docker_for_windows() {
  debug $FUNCNAME
  if is_moby_vm && has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_docker_for_mac() {
  debug $FUNCNAME
  if is_moby_vm && ! has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_native() {
  debug $FUNCNAME
  if [ $(get_docker_install_type) = "native" ]; then
    return 0
  else
    return 1
  fi
}

is_moby_vm() {
  debug $FUNCNAME
  init_name_map
  if echo "$GLOBAL_NAME_MAP" | grep -q "moby"; then
    return 0
  else
    return 1
  fi
}

has_docker_for_windows_client(){
  debug $FUNCNAME
  init_host_arch
  if [ "${GLOBAL_HOST_ARCH}" = "windows" ]; then
    return 0
  else
    return 1
  fi
}

get_docker_external_hostname() {
  if is_docker_for_mac || is_docker_for_windows; then
    echo "localhost"
  else
    echo ""
  fi
}

has_external_hostname() {
  if [ "${HOSTNAME}" = "" ]; then
    return 1
  else
    return 0
  fi
}

docker_exec() {
  debug $FUNCNAME
  if has_docker_for_windows_client; then
    log "MSYS_NO_PATHCONV=1 docker.exe \"$@\""
    MSYS_NO_PATHCONV=1 docker.exe "$@"
  else
    log "$(which docker) \$@\""
    "$(which docker)" "$@"
  fi
}

update_image_if_not_found() {
  debug $FUNCNAME

  text "${GREEN}INFO:${NC} (${CHE_MINI_PRODUCT_NAME} download): Checking for image '$1'..."
  CURRENT_IMAGE=$(docker images -q "$1")
  if [ "${CURRENT_IMAGE}" == "" ]; then
    text "not found\n"
    update_image $1
  else
    text "found\n"
  fi
}

update_image() {
  debug $FUNCNAME

  if [ "${1}" == "--force" ]; then
    shift
    info "download" "Removing image $1"
    log "docker rmi -f $1 >> \"${LOGS}\""
    docker rmi -f $1 >> "${LOGS}" 2>&1 || true
  fi

  if [ "${1}" == "--pull" ]; then
    shift
  fi

  info "download" "Pulling image $1"
  text "\n"
  docker pull $1 || true
  text "\n"
}

port_open(){
  log "netstat -an | grep 0.0.0.0:$1 >> \"${LOGS}\" 2>&1"
  netstat -an | grep 0.0.0.0:$1 >> "${LOGS}" 2>&1
  NETSTAT_EXIT=$?

  if [ $NETSTAT_EXIT = 0 ]; then
    return 1
  else
    return 0
  fi
}

container_exist_by_name(){
  docker inspect ${1} > /dev/null 2>&1
  if [ "$?" == "0" ]; then
    return 0
  else
    return 1
  fi
}

get_server_container_id() {
  log "docker inspect -f '{{.Id}}' ${1}"
  docker inspect -f '{{.Id}}' ${1}
}

wait_until_container_is_running() {
  CONTAINER_START_TIMEOUT=${1}

  ELAPSED=0
  until container_is_running ${2} || [ ${ELAPSED} -eq "${CONTAINER_START_TIMEOUT}" ]; do
    log "sleep 1"
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

container_is_running() {
  if [ "$(docker ps -qa -f "status=running" -f "id=${1}" | wc -l)" -eq 0 ]; then
    return 1
  else
    return 0
  fi
}

wait_until_server_is_booted () {
  SERVER_BOOT_TIMEOUT=${1}

  ELAPSED=0
  until server_is_booted ${2} || [ ${ELAPSED} -eq "${SERVER_BOOT_TIMEOUT}" ]; do
    log "sleep 2"
    sleep 2
    # Total hack - having to restart haproxy for some reason on windows
    ELAPSED=$((ELAPSED+1))
  done
}

get_hostname(){
  if has_external_hostname; then
    echo $CHE_HOSTNAME
  else
    echo $CHE_HOST
  fi  
}

server_is_booted() {
  log "curl -I -k $(get_hostname):$CHE_PORT/api/"
  HTTP_STATUS_CODE=$(curl -I -k $(get_hostname):$CHE_PORT/api/ \
                     -s -o "${LOGS}" --write-out "%{http_code}")
  if [[ "${HTTP_STATUS_CODE}" = "200" ]] || [[ "${HTTP_STATUS_CODE}" = "302" ]]; then
    return 0
  else
    return 1
  fi
}

check_if_booted() {
  CURRENT_CHE_SERVER_CONTAINER_ID=$(get_server_container_id $CHE_SERVER_CONTAINER_NAME)
  wait_until_container_is_running 20 ${CURRENT_CHE_SERVER_CONTAINER_ID}
  if ! container_is_running ${CURRENT_CHE_SERVER_CONTAINER_ID}; then
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for ${CHE_MINI_PRODUCT_NAME} container to start."
    return 1
  fi

  info "start" "Server logs at \"docker logs -f ${CHE_SERVER_CONTAINER_NAME}\""
  info "start" "Server booting..."
  wait_until_server_is_booted 60 ${CURRENT_CHE_SERVER_CONTAINER_ID}

  if server_is_booted ${CURRENT_CHE_SERVER_CONTAINER_ID}; then
    info "start" "Booted and reachable"
    info "start" "Ver: $(get_installed_version)"
    info "start" "Use: http://$(get_hostname):${CHE_PORT}"
    info "start" "API: http://$(get_hostname):${CHE_PORT}/swagger"
  else
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for server. Run \"docker logs ${CHE_SERVER_CONTAINER_NAME}\" to inspect the issue."
    return 1
  fi
}

is_initialized() {
  debug $FUNCNAME
  if [[ -d "${CHE_CONFIG_MANIFESTS_FOLDER}" ]] && \
     [[ -d "${CHE_CONFIG_MODULES_FOLDER}" ]] && \
     [[ -f "${REFERENCE_ENVIRONMENT_FILE}" ]] && \
     [[ -f "${CHE_CONFIG}/${CHE_VERSION_FILE}" ]]; then
    return 0
  else
    return 1
  fi
}

has_version_registry() {
  if [ -d ~/."${CHE_MINI_PRODUCT_NAME}"/manifests/$1 ]; then
    return 0;
  else
    return 1;
  fi
}

get_version_registry() {
  info "cli" "Downloading version registry..."

  log "docker pull ${CHE_GLOBAL_VERSION_IMAGE} >> \"${LOGS}\" 2>&1 || true"
  docker pull ${CHE_GLOBAL_VERSION_IMAGE} >> "${LOGS}" 2>&1 || true
  log "docker_exec run --rm -v \"${CHE_MANIFEST_DIR}\":/copy ${CHE_GLOBAL_VERSION_IMAGE}"
  docker_exec run --rm -v "${CHE_MANIFEST_DIR}":/copy ${CHE_GLOBAL_VERSION_IMAGE}
}

list_versions(){
  # List all subdirectories and then print only the file name
  for version in "${CHE_MANIFEST_DIR}"/* ; do
    text " ${version##*/}\n"
  done
}

version_error(){
  text "\nWe could not find version '$1'. Available versions:\n"
  list_versions
  text "\nSet CHE_VERSION=<version> and rerun.\n\n"
}

### Returns the list of Codenvy images for a particular version of Codenvy
### Sets the images as environment variables after loading from file
get_image_manifest() {
  info "cli" "Checking registry for version '$1' images"
  if ! has_version_registry $1; then
    version_error $1
    return 1;
  fi

  IMAGE_LIST=$(cat "$CHE_MANIFEST_DIR"/$1/images)
  IFS=$'\n'
  for SINGLE_IMAGE in $IMAGE_LIST; do
    log "eval $SINGLE_IMAGE"
    eval $SINGLE_IMAGE
  done
}

can_upgrade() {
  #  4.7.2 -> 5.0.0-M2-SNAPSHOT  <insert-syntax>
  #  4.7.2 -> 4.7.3              <insert-syntax>
  while IFS='' read -r line || [[ -n "$line" ]]; do
    VER=$(echo $line | cut -d ' ' -f1)
    UPG=$(echo $line | cut -d ' ' -f2)

    # Loop through and find all matching versions
    if [[ "${VER}" == "${1}" ]]; then
      if [[ "${UPG}" == "${2}" ]]; then
        return 0
      fi
    fi
  done < "$CHE_MANIFEST_DIR"/upgrades

  return 1
}

print_upgrade_manifest() {
  #  4.7.2 -> 5.0.0-M2-SNAPSHOT  <insert-syntax>
  #  4.7.2 -> 4.7.3              <insert-syntax>
  while IFS='' read -r line || [[ -n "$line" ]]; do
    VER=$(echo $line | cut -d ' ' -f1)
    UPG=$(echo $line | cut -d ' ' -f2)
    text "  "
    text "%s" $VER
    for i in `seq 1 $((25-${#VER}))`; do text " "; done
    text "%s" $UPG
    text "\n"
  done < "$CHE_MANIFEST_DIR"/upgrades
}

print_version_manifest() {
  while IFS='' read -r line || [[ -n "$line" ]]; do
    VER=$(echo $line | cut -d ' ' -f1)
    CHA=$(echo $line | cut -d ' ' -f2)
    UPG=$(echo $line | cut -d ' ' -f3)
    text "  "
    text "%s" $VER
    for i in `seq 1 $((25-${#VER}))`; do text " "; done
    text "%s" $CHA
    for i in `seq 1 $((18-${#CHA}))`; do text " "; done
    text "%s" $UPG
    text "\n"
  done < "$CHE_MANIFEST_DIR"/versions
}

get_installed_version() {
  if ! is_initialized; then
    echo "<not-installed>"
  else
    cat "${CHE_CONFIG}"/$CHE_VERSION_FILE
  fi
}

get_installed_installdate() {
  if ! is_initialized; then
    echo "<not-installed>"
  else
    cat "${CHE_CONFIG}"/$CHE_VERSION_FILE
  fi
}

# Usage:
#   confirm_operation <Warning message> [--force|--no-force]
confirm_operation() {
  debug $FUNCNAME

  FORCE_OPERATION=${2:-"--no-force"}

  if [ ! "${FORCE_OPERATION}" == "--quiet" ]; then
  
    # Warn user with passed message
    info "${1}"

    text "\n"
    read -p "      Are you sure? [N/y] " -n 1 -r
    text "\n\n"
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
      return 1;
    else
      return 0;
    fi
  fi
}

# Runs puppet image to generate configuration
generate_configuration_with_puppet() {
  debug $FUNCNAME
  info "config" "Generating ${CHE_MINI_PRODUCT_NAME} configuration..."
  # Note - bug in docker requires relative path for env, not absolute
  log "docker_exec run -it --rm \
                  --env-file=\"${REFERENCE_ENVIRONMENT_FILE}\" \
                  -v \"${CHE_INSTANCE}\":/opt/${CHE_MINI_PRODUCT_NAME}:rw \
                  -v \"${CHE_CONFIG_MANIFESTS_FOLDER}\":/etc/puppet/manifests:ro \
                  -v \"${CHE_CONFIG_MODULES_FOLDER}\":/etc/puppet/modules:ro \
                      $IMAGE_PUPPET \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/${CHE_MINI_PRODUCT_NAME}.pp --show_diff \"$@\""
  docker_exec run -it --rm \
                  --env-file="${REFERENCE_ENVIRONMENT_FILE}" \
                  -v "${CHE_INSTANCE}":/opt/${CHE_MINI_PRODUCT_NAME}:rw \
                  -v "${CHE_CONFIG_MANIFESTS_FOLDER}":/etc/puppet/manifests:ro \
                  -v "${CHE_CONFIG_MODULES_FOLDER}":/etc/puppet/modules:ro \
                      $IMAGE_PUPPET \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/${CHE_MINI_PRODUCT_NAME}.pp --show_diff "$@" >> "${LOGS}"
}

###########################################################################
### END HELPER FUNCTIONS
###
### START CLI COMMANDS
###########################################################################
cmd_download() {
  FORCE_UPDATE=${1:-"--no-force"}

  get_version_registry
  get_image_manifest $CHE_VERSION

  IFS=$'\n'
  for SINGLE_IMAGE in $IMAGE_LIST; do
    VALUE_IMAGE=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
    if [[ $FORCE_UPDATE == "--force" ]] ||
       [[ $FORCE_UPDATE == "--pull" ]]; then
      update_image $FORCE_UPDATE $VALUE_IMAGE
    else
      update_image_if_not_found $VALUE_IMAGE
    fi
  done
}

cmd_init() {
  FORCE_UPDATE=${1:-"--no-force"}
  if [ "${FORCE_UPDATE}" == "--no-force" ]; then
    # If codenvy.environment file exists, then fail
    if is_initialized; then
      info "init" "Already initialized."
      return 1
    fi
  fi

  cmd_download $FORCE_UPDATE

  if [ -z ${IMAGE_INIT+x} ]; then
    get_image_manifest $CHE_VERSION
  fi

  info "init" "Installing configuration and bootstrap variables:"
  log "mkdir -p \"${CHE_CONFIG}\""
  mkdir -p "${CHE_CONFIG}"
  log "mkdir -p \"${CHE_INSTANCE}\""
  mkdir -p "${CHE_INSTANCE}"

  if [ ! -w "${CHE_CONFIG}" ]; then
    error "CHE_CONFIG is not writable. Aborting."
    return 1;
  fi

  if [ ! -w "${CHE_INSTANCE}" ]; then
    error "CHE_INSTANCE is not writable. Aborting."
    return 1;
  fi

  if [ "${CHE_DEVELOPMENT_MODE}" = "on" ]; then
    # docker pull codenvy/bootstrap with current directory as volume mount.
    docker_exec run --rm \
                    -v "${CHE_CONFIG}":/copy \
                    -v "${CHE_DEVELOPMENT_REPO}":/files \
                       $IMAGE_INIT #> /dev/null 2>&1
  else
    # docker pull the bootstrap image with current directory as volume mount.
    docker_exec run --rm -v "${CHE_CONFIG}":/copy $IMAGE_INIT #> /dev/null 2>&1
  fi

  # After initialization, add che.env with self-discovery.
  sed -i'.bak' "s|#CHE_HOST=.*|CHE_HOST=${CHE_HOST}|" "${REFERENCE_ENVIRONMENT_FILE}"
  info "init" "  CHE_HOST=${CHE_HOST}"
  sed -i'.bak' "s|#CHE_PORT=.*|CHE_PORT=${CHE_PORT}|" "${REFERENCE_ENVIRONMENT_FILE}"
  info "init" "  CHE_PORT=${CHE_PORT}"
  sed -i'.bak' "s|#CHE_VERSION=.*|CHE_VERSION=${CHE_VERSION}|" "${REFERENCE_ENVIRONMENT_FILE}"
  info "init" "  CHE_VERSION=${CHE_VERSION}"
  sed -i'.bak' "s|#CHE_CONFIG=.*|CHE_CONFIG=${CHE_CONFIG}|" "${REFERENCE_ENVIRONMENT_FILE}"
  info "init" "  CHE_CONFIG=${CHE_CONFIG}"
  sed -i'.bak' "s|#CHE_INSTANCE=.*|CHE_INSTANCE=${CHE_INSTANCE}|" "${REFERENCE_ENVIRONMENT_FILE}"
  info "init" "  CHE_INSTANCE=${CHE_INSTANCE}"

  if [ "${CHE_DEVELOPMENT_MODE}" == "on" ]; then
    sed -i'.bak' "s|#CHE_ENVIRONMENT=.*|CHE_ENVIRONMENT=development|" "${REFERENCE_ENVIRONMENT_FILE}"
    info "init" "  CHE_ENVIRONMENT=development"
    sed -i'.bak' "s|#CHE_DEVELOPMENT_REPO=.*|CHE_DEVELOPMENT_REPO=${CHE_DEVELOPMENT_REPO}|" "${REFERENCE_ENVIRONMENT_FILE}"
    info "init" "  CHE_DEVELOPMENT_REPO=${CHE_DEVELOPMENT_REPO}"
    sed -i'.bak' "s|#CHE_ASSEMBLY=.*|CHE_ASSEMBLY=${CHE_ASSEMBLY}|" "${REFERENCE_ENVIRONMENT_FILE}"
    info "init" "  CHE_ASSEMBLY=${CHE_ASSEMBLY}"
  else
    sed -i'.bak' "s|#CHE_ENVIRONMENT=.*|CHE_ENVIRONMENT=production|" "${REFERENCE_ENVIRONMENT_FILE}"
    info "init" "  CHE_ENVIRONMENT=production"
  fi

  rm -rf "${REFERENCE_ENVIRONMENT_FILE}".bak > /dev/null 2>&1

  # Write the Codenvy version to codenvy.ver
  echo "$CHE_VERSION" > "${CHE_CONFIG}/${CHE_VERSION_FILE}"
}

cmd_config() {

  # If the system is not initialized, initalize it.
  # If the system is already initialized, but a user wants to update images, then re-download.
  FORCE_UPDATE=${1:-"--no-force"}
  if ! is_initialized; then
    cmd_init $FORCE_UPDATE
  elif [[ "${FORCE_UPDATE}" == "--pull" ]] || \
       [[ "${FORCE_UPDATE}" == "--force" ]]; then
    cmd_download $FORCE_UPDATE
  fi

  # If the CHE_VERSION set by an environment variable does not match the value of
  # the codenvy.ver file of the installed instance, then do not proceed as there is a
  # confusion between what the user has set and what the instance expects.
  INSTALLED_VERSION=$(get_installed_version)
  if [[ $CHE_VERSION != $INSTALLED_VERSION ]]; then
    info "config" "CHE_VERSION=$CHE_VERSION does not match ${CODENVY_ENVIRONMENT_FILE}=$INSTALLED_VERSION. Aborting."
    return 1
  fi

  if [ -z ${IMAGE_PUPPET+x} ]; then
    get_image_manifest $CHE_VERSION
  fi

  # if dev mode is on, pick configuration sources from repo.
  # please note that in production mode update of configuration sources must be only on update.
  if [ "${CHE_DEVELOPMENT_MODE}" == "on" ]; then
    docker_exec run --rm \
                    -v "${CHE_CONFIG}":/copy \
                    -v "${CHE_DEVELOPMENT_REPO}":/files \
                       $IMAGE_INIT
  fi

  # print puppet output logs to console if dev mode is on
  if [ "${CHE_DEVELOPMENT_MODE}" == "on" ]; then
     generate_configuration_with_puppet
  else
     generate_configuration_with_puppet 
  fi

  # Replace certain environment file lines with wind
  if has_docker_for_windows_client; then
    info "config" "Customizing docker-compose for Windows"
    CHE_ENVFILE_CHE=$(convert_posix_to_windows $(echo \
                                   "${CHE_INSTANCE}/config/che/che.env"))
    sed "s|^.*${CHE_MINI_PRODUCT_NAME}\.env.*$|\ \ \ \ \ \ \-\ \'${CHE_ENVFILE_CHE}\'|" -i "${REFERENCE_COMPOSE_FILE}"
  fi
}

cmd_start() {
  debug $FUNCNAME

  # If Codenvy is already started or booted, then terminate early.
  if container_exist_by_name $CHE_SERVER_CONTAINER_NAME; then
    CURRENT_CHE_SERVER_CONTAINER_ID=$(get_server_container_id $CHE_SERVER_CONTAINER_NAME)
    if container_is_running ${CURRENT_CHE_SERVER_CONTAINER_ID} && \
       server_is_booted ${CURRENT_CHE_SERVER_CONTAINER_ID}; then
       info "start" "Codenvy is already running"
       info "start" "Server logs at \"docker logs -f ${CHE_SERVER_CONTAINER_NAME}\""
       info "start" "Ver: $(get_installed_version)"
       info "start" "Use: http://${CHE_HOST}"
       info "start" "API: http://${CHE_HOST}/swagger"
       return
    fi
  fi

  # To protect users from accidentally updating their Codenvy servers when they didn't mean
  # to, which can happen if CHE_VERSION=latest
  FORCE_UPDATE=${1:-"--no-force"}
  # Always regenerate puppet configuration from environment variable source, whether changed or not.
  # If the current directory is not configured with an .env file, it will initialize
  cmd_config $FORCE_UPDATE

  # Begin tests of open ports that we require
  info "start" "Preflight checks"
  text   "         port $CHE_PORT (http):       $(port_open $CHE_PORT && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  if ! $(port_open $CHE_PORT); then
    error "Ports required to run $CHE_MINI_PRODUCT_NAME are used by another program. Aborting..."
    return 1;
  fi
  text "\n"

  # Start Codenvy
  # Note bug in docker requires relative path, not absolute path to compose file
  info "start" "Starting containers..."
  log "docker-compose --file=\"${REFERENCE_COMPOSE_FILE}\" -p=$CHE_MINI_PRODUCT_NAME up -d >> \"${LOGS}\" 2>&1"
  docker-compose --file="${REFERENCE_COMPOSE_FILE}" -p=$CHE_MINI_PRODUCT_NAME up -d >> "${LOGS}" 2>&1
  check_if_booted
}

cmd_stop() {
  debug $FUNCNAME

  if [ $# -gt 0 ]; then
    error "${CHE_MINI_PRODUCT_NAME} stop: You passed unknown options. Aborting."
    return
  fi

  info "stop" "Stopping containers..."
  log "docker-compose --file=\"${REFERENCE_COMPOSE_FILE}\" -p=$CHE_MINI_PRODUCT_NAME stop >> \"${LOGS}\" 2>&1 || true"
  docker-compose --file="${REFERENCE_COMPOSE_FILE}" -p=$CHE_MINI_PRODUCT_NAME stop >> "${LOGS}" 2>&1 || true
  info "stop" "Removing containers..."
  log "yes | docker-compose --file=\"${REFERENCE_COMPOSE_FILE}\" -p=$CHE_MINI_PRODUCT_NAME rm >> \"${LOGS}\" 2>&1 || true"
  yes | docker-compose --file="${REFERENCE_COMPOSE_FILE}" -p=$CHE_MINI_PRODUCT_NAME rm >> "${LOGS}" 2>&1 || true
}

cmd_restart() {
  debug $FUNCNAME

  FORCE_UPDATE=${1:-"--no-force"}
  if [[ "${FORCE_UPDATE}" == "--force" ]] ||\
     [[ "${FORCE_UPDATE}" == "--pull" ]]; then
    info "restart" "Stopping and removing containers..."
    cmd_stop
    info "restart" "Initiating clean start"
    cmd_start ${FORCE_UPDATE}
  else
    info "restart" "Generating updated config..."
    cmd_config
    info "restart" "Restarting services..."
    log "docker-compose --file=\"${REFERENCE_COMPOSE_FILE}\" -p=$CHE_MINI_PRODUCT_NAME restart >> \"${LOGS}\" 2>&1"
    docker-compose --file="${REFERENCE_COMPOSE_FILE}" -p=$CHE_MINI_PRODUCT_NAME restart >> "${LOGS}" 2>&1 || true
    check_if_booted
  fi
}

cmd_destroy() {
  debug $FUNCNAME

  WARNING="destroy !!! Stopping services and !!! deleting data !!! this is unrecoverable !!!"
  if ! confirm_operation "${WARNING}" "$@"; then
    return;
  fi

  # Stop alls services
  cmd_stop
  info "destroy" "Deleting instance and config..."

  # This is a tactic to remove files on the host when we do not have 
  # root permissions on the host. Using containers to destroy files created
  # inside of containers avoids certain ownership permission issues.
  docker_exec run --rm -v "${CHE_CONFIG}":/root/che-config \
                  -v "${CHE_INSTANCE}":/root/che-instance \
                      alpine sh -c "rm -rf /root/che-instance/* && rm -rf /root/che-config/*"


  rm -rf "${CHE_INSTANCE}"
  rm -rf "${CHE_CONFIG}"
}

cmd_rmi() {
  info "rmi" "Checking registry for version '$CHE_VERSION' images"
  if ! has_version_registry $CHE_VERSION; then
    version_error $CHE_VERSION
    return 1;
  fi

  WARNING="rmi !!! Removing images disables codenvy and forces a pull !!!"
  if ! confirm_operation "${WARNING}" "$@"; then
    return;
  fi

  IMAGE_LIST=$(cat "$CHE_MANIFEST_DIR"/$CHE_VERSION/images)
  IFS=$'\n'
  info "rmi" "Removing ${CHE_MINI_PRODUCT_NAME} Docker images..."

  for SINGLE_IMAGE in $IMAGE_LIST; do
    VALUE_IMAGE=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
    info "rmi" "Removing $VALUE_IMAGE..."
    log "docker rmi -f ${VALUE_IMAGE} >> \"${LOGS}\" 2>&1 || true"
    docker rmi -f $VALUE_IMAGE >> "${LOGS}" 2>&1 || true
  done

  # This is Codenvy's singleton instance with the version registry
  info "rmi" "Removing $CHE_GLOBAL_VERSION_IMAGE"
  docker rmi -f $CHE_GLOBAL_VERSION_IMAGE >> "${LOGS}" 2>&1 || true
}

cmd_version() {
  debug $FUNCNAME

  text "$CHE_PRODUCT_NAME:\n"
  text "  Version:      %s\n" $(get_installed_version)
  text "  Installed:    %s\n" $(get_installed_installdate)
  text "  CLI version:  $CHE_CLI_VERSION\n"
}

cmd_offline() {
  info "offline" "Checking registry for version '$CHE_VERSION' images"
  if ! has_version_registry $CHE_VERSION; then
    version_error $CHE_VERSION
    return 1;
  fi

  # Make sure the images have been pulled and are in your local Docker registry
  cmd_download

  mkdir -p $CODENVY_OFFLINE_FOLDER

  IMAGE_LIST=$(cat "$CHE_MANIFEST_DIR"/$CHE_VERSION/images)
  IFS=$'\n'
  info "offline" "Saving ${CHE_MINI_PRODUCT_NAME} Docker images as tar files..."

  for SINGLE_IMAGE in $IMAGE_LIST; do
    VALUE_IMAGE=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
    TAR_NAME=$(echo $VALUE_IMAGE | sed "s|\/|_|")
    info "offline" "Saving $CHE_OFFLINE_FOLDER/$TAR_NAME.tar..."
    if ! $(docker save $VALUE_IMAGE > $CHE_OFFLINE_FOLDER/$TAR_NAME.tar); then
      error "Docker was interrupted while saving $CHE_OFFLINE_FOLDER/$TAR_NAME.tar"
      return 1;
    fi
  done

  # This is Codenvy's singleton instance with the version registry
  docker save $CHE_GLOBAL_VERSION_IMAGE > "${CHE_OFFLINE_FOLDER}"/CHE_VERSION.tar
  info "offline" "Images saved as tars in $CHE_OFFLINE_FOLDER"
}

cmd_info() {
  debug $FUNCNAME
  if [ $# -eq 0 ]; then
    TESTS="--debug"
  else
    TESTS=$1
  fi

  case $TESTS in
    --all|-all)
      cmd_debug
      cmd_network
    ;;
    --network|-network)
      cmd_network
    ;;
    --debug|-debug)
      cmd_debug
    ;;
    *)
      info "info" "Unknown info flag passed: $1."
      return;
    ;;
  esac
}

cmd_debug() {
  debug $FUNCNAME
  info "---------------------------------------"
  info "------------   CLI INFO   -------------"
  info "---------------------------------------"
  info ""
  info "-----------    CHE INFO   ------------"
  info "CHE_VERSION           = ${CHE_VERSION}"
  info "CHE_INSTANCE          = ${CHE_INSTANCE}"
  info "CHE_CONFIG            = ${CHE_CONFIG}"
  info "CHE_HOST              = ${CHE_HOST}"
  info "CHE_DEVELOPMENT_MODE  = ${CHE_DEVELOPMENT_MODE}"
  info "CHE_DEVELOPMENT_REPO  = ${CHE_DEVELOPMENT_REPO}"
  info "CHE_BACKUP_FOLDER     = ${CHE_BACKUP_FOLDER}"
  info ""
  info "-----------  PLATFORM INFO  -----------"
#  info "CLI DEFAULT PROFILE       = $(has_default_profile && echo $(get_default_profile) || echo "not set")"
  info "CLI_VERSION               = ${CHE_CLI_VERSION}"
  info "DOCKER_INSTALL_TYPE       = $(get_docker_install_type)"
  info "IS_NATIVE                 = $(is_native && echo \"YES\" || echo \"NO\")"
  info "IS_WINDOWS                = $(has_docker_for_windows_client && echo \"YES\" || echo \"NO\")"
  info "IS_DOCKER_FOR_WINDOWS     = $(is_docker_for_windows && echo \"YES\" || echo \"NO\")"
  info "HAS_DOCKER_FOR_WINDOWS_IP = $(has_docker_for_windows_client && echo \"YES\" || echo \"NO\")"
  info "IS_DOCKER_FOR_MAC         = $(is_docker_for_mac && echo \"YES\" || echo \"NO\")"
  info "IS_BOOT2DOCKER            = $(is_boot2docker && echo \"YES\" || echo \"NO\")"
  info "IS_MOBY_VM                = $(is_moby_vm && echo \"YES\" || echo \"NO\")"
  info ""
}

cmd_network() {
  debug $FUNCNAME

  if [ -z ${IMAGE_PUPPET+x} ]; then
    get_image_manifest $CHE_VERSION
  fi

  info ""
  info "---------------------------------------"
  info "--------   CONNECTIVITY TEST   --------"
  info "---------------------------------------"
  # Start a fake workspace agent
  log "docker_exec run -d -p 12345:80 --name fakeagent alpine httpd -f -p 80 -h /etc/ >> \"${LOGS}\""
  docker_exec run -d -p 12345:80 --name fakeagent alpine httpd -f -p 80 -h /etc/ >> "${LOGS}"

  AGENT_INTERNAL_IP=$(docker inspect --format='{{.NetworkSettings.IPAddress}}' fakeagent)
  AGENT_INTERNAL_PORT=80
  AGENT_EXTERNAL_IP=$CHE_HOST
  AGENT_EXTERNAL_PORT=12345


  ### TEST 1: Simulate browser ==> workspace agent HTTP connectivity
  HTTP_CODE=$(curl -I localhost:${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o "${LOGS}" --connect-timeout 5 \
                          --write-out "%{http_code}") || echo "28" >> "${LOGS}"

  if [ "${HTTP_CODE}" = "200" ]; then
      info "Browser    => Workspace Agent (localhost): Connection succeeded"
  else
      info "Browser    => Workspace Agent (localhost): Connection failed"
  fi

  ### TEST 1a: Simulate browser ==> workspace agent HTTP connectivity
  HTTP_CODE=$(curl -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o "${LOGS}" --connect-timeout 5 \
                          --write-out "%{http_code}") || echo "28" >> "${LOGS}"

  if [ "${HTTP_CODE}" = "200" ]; then
      info "Browser    => Workspace Agent ($AGENT_EXTERNAL_IP): Connection succeeded"
  else
      info "Browser    => Workspace Agent ($AGENT_EXTERNAL_IP): Connection failed"
  fi

  ### TEST 2: Simulate Che server ==> workspace agent (external IP) connectivity
  export HTTP_CODE=$(docker run --rm --name fakeserver \
                                --entrypoint=curl \
                                ${IMAGE_CHE} \
                                  -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                                  -s -o "${LOGS}" \
                                  --write-out "%{http_code}")

  if [ "${HTTP_CODE}" = "200" ]; then
      info "Server     => Workspace Agent (External IP): Connection succeeded"
  else
      info "Server     => Workspace Agent (External IP): Connection failed"
  fi

  ### TEST 3: Simulate Che server ==> workspace agent (internal IP) connectivity
  export HTTP_CODE=$(docker run --rm --name fakeserver \
                                --entrypoint=curl \
                                ${IMAGE_CHE} \
                                  -I ${AGENT_INTERNAL_IP}:${AGENT_INTERNAL_PORT}/alpine-release \
                                  -s -o "${LOGS}" \
                                  --write-out "%{http_code}")

  if [ "${HTTP_CODE}" = "200" ]; then
      info "Server     => Workspace Agent (Internal IP): Connection succeeded"
  else
      info "Server     => Workspace Agent (Internal IP): Connection failed"
  fi

  log "docker rm -f fakeagent >> \"${LOGS}\""
  docker rm -f fakeagent >> "${LOGS}"
}
