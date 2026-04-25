# ArtifexAI Backend

AI-powered game asset generation platform built with Spring Boot. Generate high-quality 2D game images and videos using Google Gemini AI with intelligent prompt optimization and cloud-based asset management.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start (Docker)](#quick-start-docker)
- [Local Development Setup](#local-development-setup)
- [Docker Deployment](#docker-deployment)
- [Features](#features)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)
- [Security Notes](#security-notes)
- [License](#license)

## Prerequisites

- **Docker Desktop** or Docker Engine (for Docker deployment)
- **Java 17** or higher (for local development)
- **Maven** (for local development)
- **AWS Account** (for S3 and CloudFront)
- **Google Cloud Account** (for Gemini AI and Vertex AI)
- **fal-ai Account** (for Flux, Qwen, and FireRed image models — [sign up at fal.ai](https://fal.ai))
- **Required credential files**:
    - `.env` - Environment variables
    - `cloudfront-private-key.pem` - CloudFront private key
    - `gemini-vertex-*.json` - Google Cloud Vertex AI credentials

## Quick Start (Docker)

The easiest way to run ArtifexAI is using Docker.

### 1. Setup Required Files

Ensure these files are in the project root:
- `.env` (copy from `.env.example` and fill in your credentials)
- `cloudfront-private-key.pem`
- `gemini-vertex-*.json`

### 2. Start the Application

**Windows (PowerShell):**
```powershell
.\docker-manage.ps1 start
```

**Linux/Mac (Bash):**
```bash
chmod +x docker-manage.sh
./docker-manage.sh start
```

The application will be available at **http://localhost:7070**

### 3. View Logs

```powershell
# Windows
.\docker-manage.ps1 logs app

# Linux/Mac
./docker-manage.sh logs app
```

### 4. Stop the Application

```powershell
# Windows
.\docker-manage.ps1 stop

# Linux/Mac
./docker-manage.sh stop
```

## Local Development Setup

### 1. Environment Variables

Copy the `.env.example` file to create your own `.env` file:
```bash
cp .env.example .env
```

Fill in all required values in the `.env` file:

| Variable                          | Description |
|-----------------------------------|-------------|
| `API_TOKEN`                       | Your application API token |
| `POSTGRESQL_URL`                  | Full PostgreSQL connection URL (e.g. `postgresql://user:password@host/database`) |
| `POSTGRESQL_USERNAME`             | PostgreSQL username |
| `POSTGRESQL_PASSWORD`             | PostgreSQL password |
| `SMTP_SENDER_EMAIL`               | Email address for sending emails |
| `SMTP_SENDER_PASSWORD`            | SMTP password/app password |
| `GEMINI_VERTEX_PROJECT`           | Google Cloud Vertex AI project ID |
| `AWS_ACCESS_KEY_ID`               | AWS access key ID |
| `AWS_SECRET_ACCESS_KEY`           | AWS secret access key |
| `AWS_REGION`                      | AWS region (default: ap-southeast-1) |
| `AWS_S3_BUCKET_NAME`              | AWS S3 bucket name |
| `AWS_CLOUDFRONT_DOMAIN`           | CloudFront distribution domain |
| `AWS_CLOUDFRONT_KEY_PAIR_ID`      | CloudFront key pair ID |
| `AWS_CLOUDFRONT_PRIVATE_KEY_PATH` | Path to CloudFront private key (relative: `cloudfront-private-key.pem`) |
| `GOOGLE_CLIENT_ID`                | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET`            | Google OAuth2 client secret |
| `GITHUB_CLIENT_ID`                | GitHub OAuth2 client ID |
| `GITHUB_CLIENT_SECRET`            | GitHub OAuth2 client secret |
| `FAL_AI_KEY`                      | fal-ai API key (get one at [fal.ai/dashboard/keys](https://fal.ai/dashboard/keys)) |

### 2. Install Dependencies

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The server will start on port 7070.

## Docker Deployment

### Architecture

The Docker setup includes:
- **ArtifexAI Application** - Your Spring Boot application
- **Mounted Credentials** - `.env`, `.pem`, and `.json` files are mounted as read-only volumes
- **Cloud Services** - Uses your PostgreSQL database, AWS S3/CloudFront, and Google Cloud

### Docker Compose Configuration

The `docker-compose.yml` orchestrates:
- Builds the application Docker image
- Mounts credential files
- Sets up networking
- Passes AWS credentials as environment variables

### Mounted Files

| Host File | Container Path | Purpose |
|-----------|---------------|---------|
| `.env` | `/app/.env` | Environment variables |
| `cloudfront-private-key.pem` | `/app/cloudfront-private-key.pem` | CloudFront URL signing |
| `gemini-vertex-*.json` | `/app/credentials/google-credentials.json` | Vertex AI credentials |

### AWS Credentials

AWS SDK automatically uses credentials from environment variables set in `docker-compose.yml`:
- `AWS_ACCESS_KEY_ID` - From your `.env` file
- `AWS_SECRET_ACCESS_KEY` - From your `.env` file
- `AWS_REGION` - From your `.env` file (default: ap-southeast-1)

### Management Commands

**Windows (PowerShell):**
```powershell
# Check required files
.\docker-manage.ps1 check

# Build Docker image
.\docker-manage.ps1 build

# Start services
.\docker-manage.ps1 start

# Stop services
.\docker-manage.ps1 stop

# Restart services
.\docker-manage.ps1 restart

# View all logs
.\docker-manage.ps1 logs

# View app logs only
.\docker-manage.ps1 logs app

# Check service status
.\docker-manage.ps1 status

# Clean up everything
.\docker-manage.ps1 clean

# Show help
.\docker-manage.ps1 help
```

**Linux/Mac (Bash):**
```bash
# Same commands, just use ./docker-manage.sh instead
./docker-manage.sh start
./docker-manage.sh logs app
./docker-manage.sh stop
```

### Manual Docker Commands

If you prefer manual control:

```bash
# Build and start
docker-compose up -d --build

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# View container status
docker-compose ps

# Rebuild app only
docker-compose up -d --build app

# Restart app
docker-compose restart app
```

## Features

### AI-Powered Content Generation
- **Image Generation** - Create splash art, sprite sheets, and game assets using Gemini or fal-ai (Flux, Qwen, FireRed)
- **Image Editing** - AI-powered image variations, style transfer, and enhancements via Gemini and fal-ai FireRed Image Edit v1.1
- **Video Generation** - Generate animated 2D game videos (4, 6, or 8 seconds) with text-to-video or image-to-video
- **Style Transfer** - Transform existing images to match your project's art style

### Intelligent Optimization
- **Prompt Optimization** - Vertex AI automatically enhances your prompts for better results
- **Instruction Learning** - System learns from generations to improve future outputs
- **Context Awareness** - Maintains project context across all generations

### Project Management
- **Project System** - Organize assets by game projects
- **Album Management** - Automatic media organization and retrieval
- **Art Style Consistency** - Maintain consistent visual style across all assets

### Authentication & Security
- **JWT Authentication** - Secure token-based authentication
- **OAuth2 Integration** - Google and GitHub social login
- **User Management** - Complete user registration and profile management

### Cloud Infrastructure
- **AWS S3 Storage** - Scalable media storage
- **CloudFront CDN** - Fast global content delivery with signed URLs (12-hour expiration)
- **PostgreSQL** - Relational database for persistent storage
- **Google Cloud Vertex AI** - Gemini image/video generation and prompt optimization
- **fal-ai** - Flux, Qwen, and FireRed image generation & editing models

### Communication
- **Email System** - Account verification and password reset emails
- **Template Engine** - Customizable email templates

### Developer Features
- **RESTful API** - Clean, documented API endpoints
- **Swagger UI** - Interactive API documentation
- **Docker Support** - Containerized deployment
- **Environment-based Config** - Easy configuration management

## API Documentation

Once the application is running, access the Swagger UI documentation at:
```
http://localhost:7070/swagger-ui.html
```

### Main API Endpoints

**Authentication** (`/api/authentication/v1`)
- `POST /register` - User registration
- `POST /login` - User login
- OAuth2 endpoints for Google and GitHub

**Image Generation** (`/api/image_generation/v1`)
- `POST /splash_art` - Generate splash art
- `POST /variation` - Generate image variations
- `POST /sprite_sheet` - Generate sprite sheets
- `POST /style_change` - Change image art style

**Video Generation** (`/api/video_generation/v1`)
- `POST /generate` - Generate videos (text-to-video or image-to-video)
    - Supports 4, 6, or 8 second durations
    - Optional reference image for image-to-video

**Project Management** (`/api/project/v1`)
- Project CRUD operations
- Project album management

**Media Management** (`/api/media/v1`)
- Media upload and retrieval
- Album management

**User Management** (`/api/user/v1`)
- User profile operations

All endpoints require authentication via `X-auth-token` header (except registration and login).

## Configuration

### Template Folders

The application uses template files for various features:

**Mail Templates** (`mail_template/`):
- `confirm-email.txt` - Email confirmation template
- `reset-password.txt` - Password reset template

**Prompt Templates** (`prompt_template/`):
- `image-change-art-style.txt` - Art style transformation
- `image-edit.txt` - Image editing prompts
- `instruction-optimization.txt` - Instruction optimization
- `instruction-update.txt` - Instruction updates
- `prompt-optimization.txt` - Prompt optimization
- `splash-art-generation.txt` - Splash art generation
- `sprite-sheet-generation.txt` - Sprite sheet generation
- `video-generation.txt` - Video generation prompts

These folders are automatically included in the Docker image.

### Port Configuration

- **Application**: 7070
- Ensure port 7070 is not in use by another application

### Network Configuration

Docker uses the `artifexai-network` bridge network for container communication.

## Troubleshooting

### Docker Issues

**Application fails to start:**
1. Check logs: `.\docker-manage.ps1 logs app`
2. Verify all required files exist:
    - `.env`
    - `cloudfront-private-key.pem`
    - `gemini-vertex-476303-6817ac1e312f.json`
3. Ensure AWS credentials are filled in `.env`
4. Check if port 7070 is available

**AWS credentials not working:**
1. Verify `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` in `.env`
2. Ensure credentials have permissions for S3 and CloudFront
3. Check AWS region matches your bucket region

**Google Cloud credentials not working:**
1. Verify `GOOGLE_APPLICATION_CREDENTIALS` points to correct file
2. Ensure JSON file is properly mounted
3. Check service account has necessary permissions

**CloudFront signed URLs failing:**
1. Verify `cloudfront-private-key.pem` file exists
2. Check `AWS_CLOUDFRONT_KEY_PAIR_ID` matches your key pair
3. Ensure private key file has correct permissions

### Local Development Issues

**Cannot connect to PostgreSQL:**
- Check your `POSTGRESQL_URL`, `POSTGRESQL_USERNAME`, and `POSTGRESQL_PASSWORD` in `.env`
- Ensure the PostgreSQL server is running and accessible
- Verify the database exists and the user has the required privileges
- If using a managed service (e.g. Render, AWS RDS), ensure the server allows inbound connections

**AWS errors:**
- Verify AWS credentials in environment variables
- Check AWS region configuration
- Ensure IAM permissions are correct

**Google Cloud errors:**
- Set `GOOGLE_APPLICATION_CREDENTIALS` environment variable to JSON file path
- Verify service account permissions

**fal-ai errors:**
- Ensure `FAL_AI_KEY` is set correctly in your `.env`
- Verify your fal-ai account has sufficient credits at [fal.ai/dashboard](https://fal.ai/dashboard)
- Check model availability at [fal.ai/models](https://fal.ai/models)

**Build errors:**
- Run `mvn clean install` to refresh dependencies
- Check Java version (must be 17+)
- Clear Maven cache: `mvn dependency:purge-local-repository`

## Project Structure

```
ArtifexAI/
├── src/                           # Source code
│   ├── main/
│   │   ├── java/                  # Java source files
│   │   └── resources/             # Application properties
│   └── test/                      # Test files
├── mail_template/                 # Email templates
├── prompt_template/               # AI prompt templates
├── outputs/                       # Generated media (not in git)
├── docker-compose.yml             # Docker orchestration
├── Dockerfile                     # Application Docker image
├── .dockerignore                  # Docker build exclusions
├── .env                          # Environment variables (not in git)
├── .env.example                  # Environment template
├── cloudfront-private-key.pem    # CloudFront private key (not in git)
├── gemini-vertex-*.json          # Google Cloud credentials (not in git)
├── docker-manage.ps1             # Windows Docker management script
├── docker-manage.sh              # Linux/Mac Docker management script
├── pom.xml                       # Maven configuration
└── README.md                     # This file
```

## Security Notes

**Credential Protection:**
- All sensitive credentials are stored in the `.env` file
- The `.env` file is excluded from version control (`.gitignore`)
- Private keys (`.pem`) and credentials (`.json`) are never committed
- All credential files are mounted as **read-only** (`:ro`) in Docker

**Best Practices:**
- Use strong, unique passwords for all services
- Rotate credentials regularly
- Never share your `.env` file
- Never commit credentials to the repository
- Use environment variables for all sensitive data
- CloudFront uses signed URLs with expiration times (12 hours default)
- Enable 2FA on AWS and Google Cloud accounts

**Important:**
- Never hardcode credentials in code
- Never log sensitive information
- Use HTTPS for all external communications
- Regularly update dependencies for security patches

## Production Considerations

For production deployment:

1. **Use secrets management** - AWS Secrets Manager, HashiCorp Vault, etc.
2. **Use a managed PostgreSQL service** - AWS RDS, Azure Database for PostgreSQL, or Supabase with proper security configuration
3. **Set resource limits** - Configure CPU and memory limits in docker-compose.yml
4. **Enable SSL/TLS** - Use HTTPS with valid certificates
5. **Set up monitoring** - CloudWatch, Prometheus, Grafana, etc.
6. **Configure logging** - Centralized logging with ELK stack or similar
7. **Use container orchestration** - Kubernetes, AWS ECS, etc.
8. **Implement health checks** - Add health check endpoints
9. **Set up CI/CD** - Automated testing and deployment
10. **Regular backups** - Database and configuration backups

## Contributing

1. Never commit sensitive information
2. Always use environment variables for configuration
3. Test with Docker before deploying
4. Follow the existing code structure
5. Write tests for new features
6. Update documentation for changes

## License

Copyright © 2025 ArtifexAI Team

---

**Need help?** Check the troubleshooting section or review the logs using the Docker management scripts.
