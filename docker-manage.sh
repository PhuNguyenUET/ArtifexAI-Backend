#!/bin/bash

set +e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed or not in PATH"
        print_info "Please install Docker Desktop from: https://www.docker.com/products/docker-desktop"
        exit 1
    fi

    if ! docker info &> /dev/null; then
        print_error "Docker daemon is not running"
        print_info "Please start Docker Desktop"
        exit 1
    fi
}

check_files() {
    print_info "Checking required files..."

    local missing_files=()

    if [ ! -f ".env" ]; then
        missing_files+=(".env")
    fi

    if [ ! -f "cloudfront-private-key.pem" ]; then
        missing_files+=("cloudfront-private-key.pem")
    fi

    if [ ! -f "gemini-vertex-476303-6817ac1e312f.json" ]; then
        missing_files+=("gemini-vertex-476303-6817ac1e312f.json")
    fi

    if [ ${#missing_files[@]} -gt 0 ]; then
        print_error "Missing required files:"
        for file in "${missing_files[@]}"; do
            echo "  - $file"
        done
        echo ""
        print_info "Please ensure all required files are in the project root directory"
        exit 1
    fi

    print_success "All required files found"
}

build() {
    print_info "Building Docker images..."
    if docker compose build; then
        print_success "Build completed"
    else
        print_error "Build failed"
        exit 1
    fi
}

start() {
    check_docker
    check_files
    print_info "Starting services..."
    if docker compose up -d; then
        print_success "Services started"
        echo ""
        print_info "Application will be available at: ${BLUE}http://localhost:7070${NC}"
        echo ""
        print_info "View logs with: ./docker-manage.sh logs"
    else
        print_error "Failed to start services"
        exit 1
    fi
}

stop() {
    print_info "Stopping services..."
    if docker compose down; then
        print_success "Services stopped"
    else
        print_error "Failed to stop services"
        exit 1
    fi
}

restart() {
    print_info "Restarting services..."
    if docker compose restart; then
        print_success "Services restarted"
    else
        print_error "Failed to restart services"
        exit 1
    fi
}

logs() {
    if [ -z "$1" ]; then
        docker compose logs -f
    else
        docker compose logs -f "$1"
    fi
}

status() {
    print_info "Service status:"
    docker compose ps
}

clean() {
    print_warn "This will remove all containers, volumes, and networks."
    read -p "Are you sure? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Cleaning up..."
        if docker compose down -v --remove-orphans; then
            print_success "Cleanup completed"
        else
            print_error "Cleanup failed"
            exit 1
        fi
    else
        print_info "Cleanup cancelled"
    fi
}

show_help() {
    cat << 'EOF'
ArtifexAI Docker Management Script

Usage: ./docker-manage.sh [command] [options]

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
  ./docker-manage.sh start           # Start everything
  ./docker-manage.sh logs            # View all logs
  ./docker-manage.sh logs app        # View app logs only
  ./docker-manage.sh stop            # Stop all services
  ./docker-manage.sh clean           # Clean up everything

EOF
}

main() {
    case "${1:-help}" in
        check)
            check_docker
            check_files
            ;;
        build)
            check_docker
            check_files
            build
            ;;
        start)
            check_docker
            check_files
            build
            start
            ;;
        stop)
            check_docker
            stop
            ;;
        restart)
            check_docker
            restart
            ;;
        logs)
            check_docker
            logs "$2"
            ;;
        status)
            check_docker
            status
            ;;
        clean)
            check_docker
            clean
            ;;
        help|*)
            show_help
            ;;
    esac
}

main "$@"

