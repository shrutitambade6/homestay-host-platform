# =============================================================================
# Task 11 - Docker image and container lifecycle, with a command log for evidence.
# Run from the project root in PowerShell (Docker Desktop must be running):
#     powershell -ExecutionPolicy Bypass -File scripts\docker-lifecycle.ps1
# Output: evidence\docker-lifecycle-log.txt
# =============================================================================
param(
    [string]$Image = "homestay-host-platform",
    [string]$Tag = "1.0.0",
    [string]$Name = "homestay-app",
    [int]$Port = 8085
)

$ErrorActionPreference = "Continue"
New-Item -ItemType Directory -Force -Path evidence | Out-Null
$Log = "evidence\docker-lifecycle-log.txt"
"Docker lifecycle log - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" | Out-File $Log -Encoding utf8

function Step([string]$Title, [string]$Command) {
    $header = "`n==== $Title ====`n> $Command"
    Write-Host $header -ForegroundColor Cyan
    $header | Out-File $Log -Append -Encoding utf8
    $output = Invoke-Expression "$Command 2>&1" | Out-String
    Write-Host $output
    $output | Out-File $Log -Append -Encoding utf8
}

function WaitHealthy {
    for ($i = 1; $i -le 30; $i++) {
        try {
            $r = Invoke-RestMethod -Uri "http://localhost:$Port/actuator/health" -TimeoutSec 3
            if ($r.status -eq "UP") { return "Health: UP after $($i * 3) s" }
        } catch { }
        Start-Sleep -Seconds 3
    }
    return "Health: NOT UP after 90 s"
}

function LogHealth {
    $h = WaitHealthy
    Write-Host $h -ForegroundColor Yellow
    $h | Out-File $Log -Append -Encoding utf8
}

Step "1. Docker version"            "docker version"
Step "2. Build image"               "docker build -t ${Image}:${Tag} -t ${Image}:latest ."
Step "3. List images"               "docker images $Image"
Step "4. Image history (layers)"    "docker history ${Image}:${Tag}"
Step "5. Remove old container"      "docker rm -f $Name"
Step "6. Run container (port map)"  "docker run -d --name $Name -p ${Port}:8080 -v homestay-data:/app/data ${Image}:${Tag}"
LogHealth
Step "7. Running containers"        "docker ps --filter name=$Name"
Step "8. Port mapping"              "docker port $Name"
Step "9. Health endpoint"           "curl.exe -s http://localhost:$Port/actuator/health"
Step "10. Version endpoint"         "curl.exe -s http://localhost:$Port/api/version"
Step "11. Container logs (tail)"    "docker logs --tail 25 $Name"
Step "12. Inspect state"            "docker inspect --format 'Status={{.State.Status}} Health={{.State.Health.Status}} Image={{.Config.Image}} IP={{.NetworkSettings.IPAddress}}' $Name"
Step "13. Resource usage"           "docker stats --no-stream $Name"
Step "14. Exec into container"      "docker exec $Name sh -c 'whoami; java -version 2>&1 | head -1; ls -la /app'"
Step "15. Stop container"           "docker stop $Name"
Step "16. Status after stop"        "docker ps -a --filter name=$Name"
Step "17. Restart container"        "docker start $Name"
LogHealth
Step "18. Restart (in place)"       "docker restart $Name"
LogHealth
Step "19. Volume (data persists)"   "docker volume inspect homestay-data"
Step "20. Final state"              "docker ps --filter name=$Name"

Write-Host "`nLeaving the container running on http://localhost:$Port" -ForegroundColor Green
Write-Host "To remove it:  docker stop $Name; docker rm $Name   (image: docker rmi ${Image}:${Tag})"
Write-Host "Log written to $Log" -ForegroundColor Green
