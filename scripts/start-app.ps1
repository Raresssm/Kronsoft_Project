$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $repoRoot

function Wait-ForUrl {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [Parameter(Mandatory = $true)][string]$Name,
        [int]$TimeoutSeconds = 180
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                Write-Host "$Name is ready: $Url"
                return
            }
        } catch {
            Start-Sleep -Seconds 3
        }
    }

    throw "$Name did not become ready within $TimeoutSeconds seconds. Check logs with: docker compose logs $Name"
}

function Get-DockerComposeCommand {
    try {
        docker compose version *> $null
        if ($LASTEXITCODE -eq 0) {
            return @("docker", "compose")
        }
    } catch {
    }

    try {
        docker-compose version *> $null
        if ($LASTEXITCODE -eq 0) {
            return @("docker-compose")
        }
    } catch {
    }

    throw "Docker Compose was not found. Install Docker Desktop, then reopen this terminal."
}

$compose = Get-DockerComposeCommand

Write-Host "Starting Agora Campus with Docker Compose..."
$composeArgs = @()
if ($compose.Length -gt 1) {
    $composeArgs += $compose[1..($compose.Length - 1)]
}
$composeArgs += @("up", "--build", "-d")
& $compose[0] @composeArgs

Wait-ForUrl -Name "keycloak" -Url "http://localhost:8090/realms/agora-campus"
Wait-ForUrl -Name "backend" -Url "http://localhost:8080/v3/api-docs"
Wait-ForUrl -Name "frontend" -Url "http://localhost:3000"

Write-Host ""
Write-Host "Agora Campus is running."
Write-Host "Frontend:  http://localhost:3000"
Write-Host "Backend:   http://localhost:8080/swagger-ui.html"
Write-Host "Keycloak:  http://localhost:8090/admin"
Write-Host ""
Write-Host "Use admin/admin or demo/demo, or create a new account from the app."
