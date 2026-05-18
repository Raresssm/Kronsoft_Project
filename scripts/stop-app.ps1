$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $repoRoot

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
$composeArgs = @()
if ($compose.Length -gt 1) {
    $composeArgs += $compose[1..($compose.Length - 1)]
}
$composeArgs += @("down")

Write-Host "Stopping Agora Campus containers without deleting data..."
& $compose[0] @composeArgs

Write-Host ""
Write-Host "Stopped. Local database data is preserved."
Write-Host "To delete all local data, use: docker-compose down -v"
