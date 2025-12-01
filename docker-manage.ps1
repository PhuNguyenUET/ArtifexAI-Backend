param(
    [Parameter(Position=0)]
    [string]$Command = "help",

    [Parameter(Position=1)]
    [string]$Service = ""
)

function Print-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Print-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Print-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Print-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Check-Docker {
    $dockerCommand = Get-Command docker -ErrorAction SilentlyContinue

    if (-not $dockerCommand) {
        Print-Error "Docker is not installed or not in PATH"
        Print-Info "Please install Docker Desktop from: https://www.docker.com/products/docker-desktop"
        Write-Host ""
        Print-Info "After installing Docker Desktop:"
        Write-Host "  1. Start Docker Desktop"
        Write-Host "  2. Wait for it to fully start (whale icon in system tray)"
        Write-Host "  3. Run this script again"
        exit 1
    }

    $dockerInfo = docker info 2>&1
    if ($LASTEXITCODE -ne 0) {
        Print-Error "Docker daemon is not running"
        Print-Info "Please start Docker Desktop and wait for it to fully start"
        Write-Host ""
        Print-Info "Look for the Docker whale icon in your system tray"
        exit 1
    }
}

function Check-Files {
    Print-Info "Checking required files..."

    $missingFiles = @()

    if (-not (Test-Path ".env")) {
        $missingFiles += ".env"
    }

    if (-not (Test-Path "cloudfront-private-key.pem")) {
        $missingFiles += "cloudfront-private-key.pem"
    }

    if (-not (Test-Path "gemini-vertex-476303-6817ac1e312f.json")) {
        $missingFiles += "gemini-vertex-476303-6817ac1e312f.json"
    }

    if ($missingFiles.Count -gt 0) {
        Print-Error "Missing required files:"
        foreach ($file in $missingFiles) {
            Write-Host "  - $file"
        }
        Write-Host ""
        Print-Info "Please ensure all required files are in the project root directory"
        Write-Host ""
        if ($missingFiles -contains ".env") {
            Print-Info "To create .env file: copy .env.example to .env and fill in your credentials"
        }
        exit 1
    }

    Print-Success "All required files found"
}

function Build-App {
    Print-Info "Building Docker images..."
    docker compose build

    if ($LASTEXITCODE -eq 0) {
        Print-Success "Build completed"
    } else {
        Print-Error "Build failed"
        Write-Host ""
        Print-Info "Check the error messages above for details"
        exit 1
    }
}

function Start-Services {
    Check-Docker
    Check-Files

    Print-Info "Starting services..."
    docker compose up -d

    if ($LASTEXITCODE -eq 0) {
        Print-Success "Services started"
        Write-Host ""
        Write-Host "Application will be available at: " -NoNewline
        Write-Host "http://localhost:7070" -ForegroundColor Cyan
        Write-Host ""
        Print-Info "View logs with: .\docker-manage.ps1 logs"
        Print-Info "Stop services with: .\docker-manage.ps1 stop"
    } else {
        Print-Error "Failed to start services"
        Write-Host ""
        Print-Info "Check the error messages above for details"
        exit 1
    }
}

function Stop-Services {
    Check-Docker

    Print-Info "Stopping services..."
    docker compose down

    if ($LASTEXITCODE -eq 0) {
        Print-Success "Services stopped"
    } else {
        Print-Error "Failed to stop services"
        exit 1
    }
}

function Restart-Services {
    Check-Docker

    Print-Info "Restarting services..."
    docker compose restart

    if ($LASTEXITCODE -eq 0) {
        Print-Success "Services restarted"
    } else {
        Print-Error "Failed to restart services"
        exit 1
    }
}

function Show-Logs {
    param([string]$ServiceName)

    Check-Docker

    if ([string]::IsNullOrEmpty($ServiceName)) {
        Print-Info "Showing logs for all services (Ctrl+C to exit)..."
        docker compose logs -f
    } else {
        Print-Info "Showing logs for service: $ServiceName (Ctrl+C to exit)..."
        docker compose logs -f $ServiceName
    }
}

function Show-Status {
    Check-Docker

    Print-Info "Service status:"
    docker compose ps
}

function Clean-All {
    Check-Docker

    Print-Warn "This will remove all containers, volumes, and networks."
    $response = Read-Host "Are you sure? (y/N)"

    if ($response -eq "y" -or $response -eq "Y") {
        Print-Info "Cleaning up..."
        docker compose down -v --remove-orphans

        if ($LASTEXITCODE -eq 0) {
            Print-Success "Cleanup completed"
        } else {
            Print-Error "Cleanup failed"
            exit 1
        }
    } else {
        Print-Info "Cleanup cancelled"
    }
}

function Show-Help {
    Write-Host @"

ArtifexAI Docker Management Script

Usage: .\docker-manage.ps1 [command] [options]

Commands:
  check     Check if all required files exist
  build     Build Docker images
  start     Start all services (checks files and builds)
  stop      Stop all services
  restart   Restart all services
  logs      View logs (optional: specify service name)
  status    Show service status
  clean     Remove all containers, volumes, and networks
  help      Show this help message

Examples:
  .\docker-manage.ps1 start           # Start everything
  .\docker-manage.ps1 logs            # View all logs
  .\docker-manage.ps1 logs app        # View app logs only
  .\docker-manage.ps1 stop            # Stop all services
  .\docker-manage.ps1 clean           # Clean up everything

"@
}

switch ($Command.ToLower()) {
    "check" {
        Check-Docker
        Check-Files
    }
    "build" {
        Check-Docker
        Check-Files
        Build-App
    }
    "start" {
        Check-Docker
        Check-Files
        Build-App
        Start-Services
    }
    "stop" {
        Stop-Services
    }
    "restart" {
        Restart-Services
    }
    "logs" {
        Show-Logs -ServiceName $Service
    }
    "status" {
        Show-Status
    }
    "clean" {
        Clean-All
    }
    default {
        Show-Help
    }
}

