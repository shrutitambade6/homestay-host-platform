#!/usr/bin/env bash
# =============================================================================
# Tasks 13 + 14 - Ansible configuration management, provisioning, idempotency
# and rollback, run end-to-end inside WSL2 Ubuntu with a command log for evidence.
#
# Run inside the Ubuntu terminal (type your Ubuntu password once when asked):
#     bash /mnt/c/Users/Shruti/Desktop/homestay-run/5-ansible-wsl.sh
#
# Output: <project>\evidence\ansible\ansible-run-log.txt  (+ recap summary)
# Images: taken from Docker Desktop on Windows (docker.exe save) and shipped
#         to the node as archives, so no registry login is needed.
# =============================================================================
set -uo pipefail

WIN_HOME="${WIN_HOME:-/mnt/c/Users/Shruti}"
SRC="${SRC:-$WIN_HOME/Desktop/homestay-host-platform}"
EVID="$SRC/evidence/ansible"
WORK="$HOME/homestay-ansible"
IMG="$HOME/homestay-images"
REPO=homestay-host-platform          # image name used on the node
V1=1.0.0                             # first release
V2=1.0.8                             # upgrade (built by Jenkins homestay-cicd #8)
BAD=9.9.9-broken                     # a release that does not exist -> forces rollback

mkdir -p "$EVID" "$IMG"
LOG="$EVID/ansible-run-log.txt"
SUMMARY="$EVID/ansible-summary.txt"
STATUS="$EVID/ansible.status"
echo running > "$STATUS"
: > "$SUMMARY"
exec > >(tee "$LOG") 2>&1

step() { printf '\n\n==================== %s ====================\n' "$*"; }
run()  { printf '\n$ %s\n' "$*"; "$@"; local rc=$?; printf '[exit code %s]\n' "$rc"; return $rc; }
recap() { # $1 = label ; prints the PLAY RECAP line of the last ansible run
  local line; line=$(grep -E '^homestay-wsl +:' "$WORK/last-run.txt" | tail -1)
  printf '%-55s %s\n' "$1" "$line" | tee -a "$SUMMARY"
}
play() { # $1 = label, rest = ansible-playbook args
  local label="$1"; shift
  printf '\n$ ansible-playbook %s\n' "$*"
  ansible-playbook "$@" 2>&1 | tee "$WORK/last-run.txt"
  local rc=${PIPESTATUS[0]}
  printf '[exit code %s]\n' "$rc"
  recap "$label (exit $rc)"
  return $rc
}

echo "Homestay Ansible run - $(date '+%Y-%m-%d %H:%M:%S')  user=$USER host=$(hostname)"

step "0. sudo (type your Ubuntu password)"
sudo -v || { echo "sudo failed"; echo failed > "$STATUS"; exit 1; }
( while true; do sudo -n true 2>/dev/null; sleep 45; done ) & KEEPALIVE=$!
trap 'kill $KEEPALIVE 2>/dev/null' EXIT

step "1. Install Ansible on the control node"
run sudo apt-get update -qq
run sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -qq ansible python3-docker curl jq
run ansible --version
run ansible-galaxy collection list community.docker

step "2. Copy the Ansible project into the Linux filesystem"
# (Ansible ignores ansible.cfg in world-writable folders such as /mnt/c)
rm -rf "$WORK"
run cp -r "$SRC/ansible" "$WORK"
cd "$WORK" || exit 1
run ls -R .

step "3. Docker engine on the node"
if docker info >/dev/null 2>&1; then
  DOCKER_INSTALL=false
  echo "docker already reachable (Docker Desktop WSL integration) -> docker_install=false"
else
  DOCKER_INSTALL=true
  echo "no docker in this distro yet -> the docker role will install docker.io"
fi
echo "init system: $(ps -p 1 -o comm=)"

step "4. Export release images from Docker Desktop on Windows"
WD="/mnt/c/Program Files/Docker/Docker/resources/bin/docker.exe"
[ -x "$WD" ] || WD="$(command -v docker.exe || true)"
run "$WD" image ls
if "$WD" image inspect "$REPO:$V1" >/dev/null 2>&1; then
  echo "using existing $REPO:$V1 (built by the Task 11 lifecycle script)"
else
  run "$WD" tag "localhost:5000/$REPO:$V2" "$REPO:$V1"
fi
run "$WD" tag "localhost:5000/$REPO:$V2" "$REPO:$V2"
for v in $V1 $V2; do
  printf '\n$ docker.exe save %s:%s > %s/homestay-%s.tar\n' "$REPO" "$v" "$IMG" "$v"
  "$WD" save "$REPO:$v" > "$IMG/homestay-$v.tar" && ls -lh "$IMG/homestay-$v.tar"
done

COMMON=(-e "app_image_repo=$REPO" -e "docker_install=$DOCKER_INSTALL"
        -e "app_container_name=homestay-node" -e "app_data_volume=homestay-node-data")

step "5. Inventory, connectivity and syntax checks"
run ansible-inventory --graph
run ansible homestay_nodes -m ping
run ansible homestay_nodes -m setup -a 'filter=ansible_distribution*'
for p in playbooks/*.yml; do run ansible-playbook --syntax-check "$p"; done

echo "==== PLAY RECAP summary ====" >> "$SUMMARY"

step "6. TASK 13 - configure the node (packages, user, folders, config, docker)"
play "configure.yml (first run - expect changes)" playbooks/configure.yml "${COMMON[@]}"

step "7. TASK 13 - idempotency: run configure again"
play "configure.yml (second run - expect changed=0)" playbooks/configure.yml "${COMMON[@]}"

step "8. TASK 13 - dry run with --check --diff"
play "configure.yml --check --diff" playbooks/configure.yml --check --diff "${COMMON[@]}"

step "9. Node state after configuration"
run id homestay
run sudo tree -pug /opt/homestay
run sudo cat /opt/homestay/NODE-INFO.txt
run sudo cat /opt/homestay/config/homestay.env
run sudo docker version --format 'Docker server {{.Server.Version}}'

step "10. TASK 14 - provision + deploy release $V1 (site.yml)"
play "site.yml app_version=$V1 (first deploy)" playbooks/site.yml -e "app_version=$V1" \
     -e "app_image_archive=$IMG/homestay-$V1.tar" "${COMMON[@]}"

step "11. TASK 14 - idempotency: run site.yml again with the same version"
play "site.yml app_version=$V1 (rerun - expect changed=0)" playbooks/site.yml -e "app_version=$V1" \
     -e "app_image_archive=$IMG/homestay-$V1.tar" "${COMMON[@]}"

step "12. TASK 14 - upgrade to release $V2"
play "deploy.yml app_version=$V2 (upgrade)" playbooks/deploy.yml -e "app_version=$V2" \
     -e "app_image_archive=$IMG/homestay-$V2.tar" "${COMMON[@]}"
play "healthcheck.yml (after upgrade)" playbooks/healthcheck.yml "${COMMON[@]}"
run sudo sh -c 'echo "current=$(cat /opt/homestay/release/current) previous=$(cat /opt/homestay/release/previous)"'

step "13. TASK 14 - failed release $BAD -> automatic rollback (this play is EXPECTED to fail)"
play "deploy.yml app_version=$BAD (expected FAIL + auto-rollback)" playbooks/deploy.yml \
     -e "app_version=$BAD" -e app_health_retries=3 "${COMMON[@]}"
play "healthcheck.yml (after auto-rollback)" playbooks/healthcheck.yml "${COMMON[@]}"

step "14. TASK 14 - manual rollback to the previous stable release (rollback.yml)"
play "rollback.yml (back to $V1)" playbooks/rollback.yml "${COMMON[@]}"
play "healthcheck.yml (after manual rollback)" playbooks/healthcheck.yml "${COMMON[@]}"
run sudo sh -c 'echo "current=$(cat /opt/homestay/release/current) previous=$(cat /opt/homestay/release/previous)"'

step "15. Roll forward again to $V2 (final state)"
play "deploy.yml app_version=$V2 (roll forward)" playbooks/deploy.yml -e "app_version=$V2" \
     -e "app_image_archive=$IMG/homestay-$V2.tar" "${COMMON[@]}"
play "healthcheck.yml (final)" playbooks/healthcheck.yml "${COMMON[@]}"

step "16. Final evidence"
run sudo docker ps --filter name=homestay-node
run sudo docker images "$REPO"
run curl -s "http://localhost:8090/actuator/health"; echo
run curl -s "http://localhost:8090/api/version"; echo
cp "$WORK/ansible-run.log" "$EVID/ansible-internal.log" 2>/dev/null

echo
echo "================ SUMMARY ================"
cat "$SUMMARY"
echo
echo "Node app: http://localhost:8090  (open it in Windows too)"
echo "Logs:     $EVID"
echo done > "$STATUS"
