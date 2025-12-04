# ArtifexAI - AI-Powered Game Asset Generation Platform

## Executive Summary

**ArtifexAI** is an enterprise-grade, AI-powered backend system designed for professional game development teams to generate, manage, and organize high-quality 2D game assets. Built on Spring Boot 3.4.1 with Java 17, the platform integrates Google's Gemini 2.5 Flash and Veo 3.1 AI models through Vertex AI to provide intelligent image and video generation capabilities with context-aware prompt optimization.

The system features a comprehensive project management workflow, intelligent prompt engineering, cloud-based asset storage via AWS S3 and CloudFront, and a robust authentication system supporting both traditional credentials and OAuth2 providers (Google, GitHub).

---
- [Docker Deployment](#docker-deployment)
- [Features](#features)
- [API Documentation](#api-documentation)
- [System Architecture](#system-architecture)
- [Technology Stack](#technology-stack)
- [Core Features](#core-features)
- [System Modules](#system-modules)
- [Workflows](#workflows)
- [Data Models](#data-models)
- [API Endpoints](#api-endpoints)
- [Security Architecture](#security-architecture)
- [Deployment](#deployment)
- **Maven** (for local development)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)

---

## System Architecture

### High-Level Architecture

```
┌─────────────────┐
│   Client App    │
│  (Frontend/API) │
└────────┬────────┘
         │ HTTPS/REST
         ▼
┌─────────────────────────────────────────────┐
│         ArtifexAI Backend (Spring Boot)      │
│  ┌──────────────────────────────────────┐  │
│  │   Security Layer (JWT + OAuth2)       │  │
│  └──────────────────────────────────────┘  │
│  ┌──────────────────────────────────────┐  │
│  │        Controller Layer               │  │
│  │  • Authentication  • Projects         │  │
│  │  • Image Gen      • Media/Albums     │  │
│  │  • Video Gen      • User Management   │  │
│  └──────────────────────────────────────┘  │
│  ┌──────────────────────────────────────┐  │
│  │         Service Layer                 │  │
│  │  • Business Logic                     │  │
│  │  • Prompt Optimization                │  │
│  │  • Asset Generation                   │  │
│  └──────────────────────────────────────┘  │
│  ┌──────────────────────────────────────┐  │
│  │      Data Access Layer                │  │
│  │  • MongoDB Repositories               │  │
│  │  • Redis Cache                        │  │
│  └──────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
         │              │              │
         ▼              ▼              ▼
┌──────────────┐ ┌─────────────┐ ┌──────────────┐
│   MongoDB    │ │  Redis      │ │  Google AI   │
│   Database   │ │  Cache      │ │  (Gemini)    │
└──────────────┘ └─────────────┘ └──────────────┘
         │
         ▼
┌──────────────────────────────────────────┐
│        AWS Cloud Services                 │
│  ┌────────────┐      ┌─────────────┐    │
│  │   S3       │─────▶│ CloudFront  │    │
│  │  Storage   │      │    CDN      │    │
│  └────────────┘      └─────────────┘    │
└──────────────────────────────────────────┘
```

### Component Breakdown

1. **Presentation Layer**: RESTful API controllers handling HTTP requests
2. **Security Layer**: JWT-based authentication with OAuth2 integration
3. **Business Logic Layer**: Service implementations for core functionality
4. **AI Integration Layer**: Google Gemini AI interaction for content generation
5. **Persistence Layer**: MongoDB for data storage, Redis for caching
6. **Cloud Storage Layer**: AWS S3 for media storage, CloudFront for CDN delivery

---

## Technology Stack

### Backend Framework
- **Spring Boot 3.4.1** - Core application framework
- **Java 17** - Programming language
- **Maven** - Dependency management and build tool

### AI & Machine Learning
- **Google Gemini 2.5 Flash** - Image generation model
- **Google Veo 3.1 Generate Preview** - Video generation model
- **Google Vertex AI** - AI platform integration
- **Google Gen AI SDK 1.18.0** - AI client library

### Database & Caching
- **MongoDB** - Primary NoSQL database for document storage
- **Redis** - In-memory data store for caching and session management

### Cloud Services
- **AWS S3** - Object storage for generated assets
- **AWS CloudFront** - Content delivery network with signed URLs
- **Google Cloud Platform** - AI/ML services

### Security & Authentication
- **Spring Security** - Security framework
- **JWT (JSON Web Tokens)** - Stateless authentication
- **OAuth2** - Third-party authentication (Google, GitHub)
- **BCrypt** - Password hashing

### Communication & Integration
- **JavaMail** - Email service integration (SMTP)
- **OkHttp 4.12.0** - HTTP client
- **Unirest Java 3.11.09** - Simplified HTTP requests

### Documentation
- **SpringDoc OpenAPI 2.7.0** - API documentation (Swagger UI)

### Utilities
- **Lombok** - Boilerplate code reduction
- **ModelMapper** - Object mapping
- **Gson** - JSON serialization/deserialization
- **Apache Commons Lang3** - Utility functions
- **Dotenv** - Environment variable management

---

## Core Features

### 1. AI-Powered Asset Generation
- **Splash Art Generation**: Create promotional artwork from text descriptions
- **Sprite Sheet Generation**: Generate character sprites with multiple actions
- **Image Variation**: Create variations of existing images
- **Style Transfer**: Change art style of existing images
- **Video Generation**: Create animated game videos
- **Intelligent Prompt Optimization**: Automatically enhance user prompts for better results

### 2. Project Management
- **Project Contexts**: Maintain consistent art styles across assets
- **Instruction Management**: Track and update project-specific guidelines
- **Art Style Templates**: Support for 8 predefined styles (Pixelated, Hand-Drawn, Minimalist, Anime, Cartoon, Realistic, Hyper-Realistic, Custom)
- **Automatic Instruction Updates**: AI analyzes generated assets and suggests improvements

### 3. Media Management
- **Gallery System**: User-specific media galleries
- **Album Organization**: Group media by projects or custom collections
- **Cloud Storage**: Secure S3 storage with CloudFront CDN
- **Presigned URLs**: Time-limited secure access to media
- **Multi-format Support**: Images (PNG, JPEG, WebP) and Videos (MP4)

### 4. User Management
- **Multi-provider Authentication**: Email/password, Google OAuth2, GitHub OAuth2
- **Email Verification**: Secure email confirmation workflow
- **Password Management**: Reset password via email with time-limited tokens
- **Profile Management**: User profile updates and settings
- **Role-based Access Control**: USER and ADMIN roles

### 5. Security Features
- **JWT Authentication**: Stateless token-based authentication
- **Refresh Tokens**: Long-lived tokens for session renewal
- **Account Protection**: Failed login attempt tracking
- **CORS Configuration**: Cross-origin resource sharing controls
- **API Token Validation**: Internal API token for service authentication

---

## System Modules

### 1. Authentication Module (`com.Artiom.ArtifexAI.Authentication`)

**Purpose**: Handles user authentication and authorization

**Components**:
- `AuthenticationController`: Login, registration, token refresh endpoints
- `AuthenticationService`: Authentication business logic
- `JwtRequestFilterUser`: JWT token validation filter
- `OAuth2LoginSuccessHandler`: OAuth2 login success handling
- `CustomAuthenticationEntryPoint`: Custom authentication error handling

**Key Features**:
- JWT token generation and validation
- OAuth2 integration (Google, GitHub)
- Refresh token mechanism
- Stateless session management

### 2. User Module (`com.Artiom.ArtifexAI.User`)

**Purpose**: User account management and profile operations

**Components**:
- `UserController`: User-related endpoints
- `UserService`: User business logic
- `User` Model: User entity with authentication data
- `UserRepository`: MongoDB data access

**Key Features**:
- Password change functionality
- Email validation workflow
- Password reset via email
- User profile retrieval
- Account activation/deactivation

### 3. Project Module (`com.Artiom.ArtifexAI.Project`)

**Purpose**: Manage game development projects and their contexts

**Components**:
- `ProjectController`: Project CRUD endpoints
- `ProjectService`: Project management logic
- `Project` Model: Project entity with instructions and art style
- `ArtStyle` Enum: Predefined art style options

**Key Features**:
- Create/edit/delete projects
- Manage project instructions
- Art style configuration
- User-project association
- Automatic instruction updates from AI analysis

### 4. Image Generation Module (`com.Artiom.ArtifexAI.ImageGeneration`)

**Purpose**: AI-powered image and video generation

**Components**:
- `ImageGenerationController`: Image generation endpoints
- `VideoGenerationController`: Video generation endpoints
- `ImageGenerationService`: Image generation logic
- `VideoGenerationService`: Video generation logic

**Capabilities**:
- **Splash Art**: Generate promotional artwork from descriptions
- **Sprite Sheets**: Create character animation frames
- **Image Variation**: Generate variations with modifications
- **Style Change**: Transform image art styles
- **Video Generation**: Create animated game videos

### 5. Media Module (`com.Artiom.ArtifexAI.Media`)

**Purpose**: Media asset storage and organization

**Components**:
- `MediaController`: Media CRUD endpoints
- `AlbumController`: Album management endpoints
- `MediaService`: Media business logic
- `AlbumService`: Album organization logic
- `Media` Model: Media entity (images/videos)
- `Album` Model: Album entity for grouping media

**Key Features**:
- Client image uploads
- Server-generated media tracking
- Gallery view for all user media
- Album-based organization
- Project-specific albums
- Media deletion with cleanup

### 6. Prompt Optimization Module (`com.Artiom.ArtifexAI.PromptOptimization`)

**Purpose**: Enhance user prompts and analyze results

**Components**:
- `PromptOptimizationService`: Prompt improvement logic
- `PromptTemplateService`: Template management
- `PromptType` Enum: Different prompt template types

**Templates**:
- `splash-art-generation.txt`
- `sprite-sheet-generation.txt`
- `image-edit.txt`
- `image-change-art-style.txt`
- `video-generation.txt`
- `prompt-optimization.txt`
- `instruction-optimization.txt`
- `instruction-update.txt`

**Key Features**:
- Automatic prompt enhancement
- Context injection from projects
- Multi-image analysis
- Instruction suggestions
- Template-based prompt engineering

### 7. Persistence Module (`com.Artiom.ArtifexAI.Persistence`)

**Purpose**: Cloud storage integration for media assets

**Components**:
- `PersistenceService`: S3 and CloudFront integration
- Upload/download operations
- Presigned URL generation

**Key Features**:
- AWS S3 file uploads
- CloudFront signed URL generation (12-hour expiry)
- Base64 image upload support
- Media deletion
- Automatic MIME type handling

### 8. Mail Module (`com.Artiom.ArtifexAI.Mail`)

**Purpose**: Email communication system

**Components**:
- `SendMailService`: Email sending logic
- `MailTemplateService`: Email template management
- `SendMailTask` Model: Queued email tasks

**Templates**:
- `confirm-email.txt` - Email verification
- `reset-password.txt` - Password reset

**Key Features**:
- SMTP integration (Gmail)
- Template-based emails
- Token-based verification
- Password reset workflow

---

## Workflows

### 1. User Registration & Authentication Workflow

```
┌──────────────────────────────────────────────────────────┐
│                  Registration Flow                        │
└──────────────────────────────────────────────────────────┘

1. Client sends registration request
   POST /api/user/v1/register
   {
     "email": "user@example.com",
     "password": "********",
     "firstName": "John",
     "lastName": "Doe"
   }

2. System validates input and creates user account
   - Password hashed with BCrypt
   - User marked as inactive (isEmailValidated = false)
   - Generates email verification token

3. System sends confirmation email
   - Token valid for 24 hours
   - Contains verification link

4. User clicks verification link
   POST /api/user/v1/email/token
   { "token": "verification_token" }

5. System activates account
   - User marked as active
   - Can now log in

┌──────────────────────────────────────────────────────────┐
│              Authentication Flow (Email/Password)         │
└──────────────────────────────────────────────────────────┘

1. Client sends authentication request
   POST /api/user/v1/authenticate
   {
     "email": "user@example.com",
     "password": "********"
   }

2. System validates credentials
   - Checks password with BCrypt
   - Verifies account is active
   - Tracks failed attempts (max 5)

3. System generates tokens
   - Access Token (JWT): 1 hour expiry
   - Refresh Token: 30 days expiry

4. Response returned
   {
     "accessToken": "eyJhbGc...",
     "refreshToken": "eyJhbGc...",
     "user": { /* user data */ }
   }

5. Client includes JWT in requests
   Header: Authorization: Bearer <access_token>

6. Token refresh when expired
   POST /api/user/v1/refresh_jwt
   { "refreshToken": "..." }

┌──────────────────────────────────────────────────────────┐
│            OAuth2 Authentication Flow                     │
└──────────────────────────────────────────────────────────┘

1. Client initiates OAuth2 login
   GET /oauth2/authorization/google  (or /github)

2. Redirects to provider for authentication

3. Provider redirects back with authorization code
   Callback: /oauth2/callback/<provider>

4. System exchanges code for user info

5. OAuth2LoginSuccessHandler processes login
   - Creates user if doesn't exist
   - Updates existing user info
   - Generates JWT tokens

6. Redirects to client with tokens
   Frontend URL with token parameters
```

### 2. Project Creation and Asset Generation Workflow

```
┌──────────────────────────────────────────────────────────┐
│            Project Setup and Asset Generation             │
└──────────────────────────────────────────────────────────┘

STEP 1: Create a Project
─────────────────────────
POST /api/project/v1/create
{
  "projectName": "Fantasy RPG Game",
  "artStyle": "ANIME",
  "instructions": [
    "Medieval fantasy setting",
    "Vibrant colors",
    "Character-focused designs"
  ]
}

→ System creates project
  - Generates unique project ID
  - Creates dedicated project album
  - Stores art style and instructions
  - Associates with current user

STEP 2: Generate Splash Art
────────────────────────────
POST /api/image_generation/v1/splash_art
{
  "projectId": "proj_123",
  "splashDescription": "Epic battle scene with dragon",
  "numberOfOutputs": 3
}

→ System processes request:
  1. Retrieves project context (instructions + art style)
  2. Optimizes user prompt using AI
     - Input: "Epic battle scene with dragon"
     - Output: "Create an epic battle scene featuring a majestic 
       dragon with spread wings, heroes in medieval armor wielding 
       swords, dynamic action poses, dramatic lighting..."
  
  3. Injects context into template
     Template: splash-art-generation.txt
     Variables replaced:
     - {CONTEXT} → project instructions
     - {ART_STYLE} → ANIME
     - {SPLASH_ART_DESCRIPTION} → optimized prompt
  
  4. Sends to Gemini 2.5 Flash
     - Model: gemini-2.5-flash-image
     - Safety settings: BLOCK_NONE (allows creative content)
     - Media resolution: HIGH
     - Candidate count: 3
  
  5. Processes AI response
     - Extracts generated images (3 variants)
     - Uploads to S3: users/{userId}/images/{timestamp}.png
     - Creates Media records in MongoDB
     - Adds to project album
     - Generates CloudFront signed URLs
  
  6. Analyzes results with AI
     - Compares prompt vs actual images
     - Generates instruction improvements
     - Example: "Use more dramatic lighting effects for battle scenes"
     - Automatically adds to project instructions
  
  7. Returns response
     {
       "imageUrls": [
         "https://cdn.example.com/image1.png?signature=...",
         "https://cdn.example.com/image2.png?signature=...",
         "https://cdn.example.com/image3.png?signature=..."
       ],
       "updatedInstruction": "Use more dramatic lighting effects..."
     }

STEP 3: Generate Sprite Sheet
──────────────────────────────
POST /api/image_generation/v1/sprite_sheet
{
  "projectId": "proj_123",
  "characterDescription": "Female warrior with blue armor",
  "characterAction": "Running animation, 8 frames",
  "numberOfOutputs": 2
}

→ Similar workflow as splash art:
  - Uses sprite-sheet-generation.txt template
  - Maintains project art style consistency
  - Creates sprite frames suitable for animation

STEP 4: Create Image Variation
───────────────────────────────
POST /api/image_generation/v1/variation
{
  "projectId": "proj_123",
  "prompt": "Make the dragon breathe fire",
  "imageInfos": [
    {
      "imagePath": "users/user123/images/dragon.png",
      "mimeType": "IMAGE_PNG"
    }
  ],
  "numberOfOutputs": 2
}

→ Workflow with image input:
  1. Downloads reference image from S3
  2. Optimizes modification prompt
  3. Sends both text + image to Gemini
  4. Generates variations maintaining style
  5. Uploads and returns new variants

STEP 5: Change Art Style
─────────────────────────
POST /api/image_generation/v1/style_change
{
  "projectId": "proj_123",
  "newArtStyle": "PIXELATED",
  "imageInfos": [
    {
      "imagePath": "users/user123/images/character.png",
      "mimeType": "IMAGE_PNG"
    }
  ]
}

→ Transforms existing asset to different style
  - Applies new art style while preserving content
  - Useful for style exploration

STEP 6: Generate Video
───────────────────────
POST /api/video_generation/v1/generate
{
  "projectId": "proj_123",
  "videoDescription": "Dragon flying over mountains",
  "videoDuration": 6,
  "referenceImage": {
    "imagePath": "users/user123/images/dragon.png",
    "mimeType": "IMAGE_PNG"
  }
}

→ Video generation workflow:
  - Uses Gemini Veo 3.1 model
  - Duration: 4, 6, or 8 seconds
  - Optional reference image for consistency
  - Text-to-video or image-to-video
  - Uploads MP4 to S3
  - Adds to project album
```

### 3. Media Management Workflow

```
┌──────────────────────────────────────────────────────────┐
│              Media Organization Workflow                  │
└──────────────────────────────────────────────────────────┘

Gallery View (All User Media)
──────────────────────────────
GET /api/media/v1/gallery

→ Returns all images and videos for current user
  - Sorted by creation date (newest first)
  - Includes presigned CloudFront URLs
  - Valid for 12 hours

Album Organization
──────────────────
1. Create Custom Album
   POST /api/album/v1/create
   { "name": "Character Designs" }

2. Add Media to Album
   PUT /api/album/v1/add_image
   {
     "albumId": "album_123",
     "mediaId": "media_456"
   }

3. View Album Contents
   GET /api/media/v1/get_by_album?albumId=album_123

4. Remove from Album
   PUT /api/album/v1/delete_image
   {
     "albumId": "album_123",
     "mediaId": "media_456"
   }

Project-Specific Albums
───────────────────────
- Automatically created with each project
- All generated assets added automatically
- Maintains project asset organization
- Linked via projectId in Album model

Client Upload Workflow
──────────────────────
POST /api/media/v1/upload_client
{
  "base64Data": "data:image/png;base64,iVBORw0KG...",
  "mimeType": "IMAGE_PNG"
}

→ Process:
  1. Validates base64 data and MIME type
  2. Decodes base64 to bytes
  3. Uploads to S3: users/{userId}/uploads/{timestamp}.png
  4. Creates Media record
  5. Returns media ID and presigned URL
```

### 4. Password Reset Workflow

```
┌──────────────────────────────────────────────────────────┐
│              Password Reset Workflow                      │
└──────────────────────────────────────────────────────────┘

1. User Requests Password Reset
   POST /api/user/v1/forgot_password
   { "email": "user@example.com" }

2. System Generates Reset Token
   - Creates secure random token
   - Sets 1-hour expiration
   - Stores in user document

3. System Sends Email
   - Uses reset-password.txt template
   - Contains reset link with token
   - Sent via SMTP (Gmail)

4. User Clicks Reset Link
   - Navigates to client app with token

5. User Submits New Password
   POST /api/user/v1/create_new_password
   {
     "token": "reset_token_xyz",
     "newPassword": "new_secure_password"
   }

6. System Validates and Updates
   - Checks token validity and expiration
   - Hashes new password with BCrypt
   - Clears reset token
   - Password updated successfully
```

### 5. Prompt Optimization Workflow

```
┌──────────────────────────────────────────────────────────┐
│            Intelligent Prompt Enhancement                 │
└──────────────────────────────────────────────────────────┘

Input: User's simple prompt
  "dragon fighting knight"

Step 1: Prompt Optimization Service
────────────────────────────────────
Uses: prompt-optimization.txt template
Model: gemini-2.5-flash (text model)

Gemini analyzes and enhances:
  "Create a detailed 2D game art illustration depicting an epic 
   confrontation between a fierce dragon and a valiant knight. 
   The dragon should have scaled texture, powerful wings, and 
   breathing fire. The knight wears gleaming armor, holds a 
   sword, and stands in a defensive battle stance. Include 
   dynamic motion, dramatic lighting with shadows, and a fantasy 
   medieval environment. Focus on the intensity of the battle 
   with sparks, smoke effects, and action lines to convey movement."

Step 2: Context Injection
──────────────────────────
Project context added:
  - Art style: ANIME
  - Instructions: ["Vibrant colors", "Character-focused"]
  
Final prompt sent to image model:
  "You are a professional game artist creating 2D game assets.
   Project style: ANIME
   Project guidelines: Vibrant colors; Character-focused designs
   
   Create a detailed 2D game art illustration depicting..."

Step 3: Result Analysis
───────────────────────
After generation, AI analyzes:
  - Compares prompt intent vs actual output
  - Identifies successful elements
  - Suggests improvements

Example output:
  "Dragon scales rendered well with anime style, but fire 
   effects could use more vibrant color gradients to match 
   project guidelines. Consider emphasizing character expressions 
   more in future battle scenes."

This suggestion automatically added to project instructions
for future consistency.
```

---

## Data Models

### User Model

```java
@Document("z_user")
public class User {
    @Id
    private String id;                    // MongoDB ObjectId
    
    @Indexed
    private String email;                 // Unique email address
    private String password;              // BCrypt hashed password
    private String authProvider;          // LOCAL, GOOGLE, GITHUB
    private String role;                  // USER, ADMIN
    private boolean active;               // Account status
    
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    
    // Security features
    private int failedAttempt;           // Failed login counter (max 5)
    
    // Password reset
    private String resetPasswordToken;
    private long resetPasswordTokenExpire;
    
    // Email verification
    private boolean isEmailValidated;
    private String confirmEmailToken;
    private long confirmEmailTokenExpire;
}
```

**Collections**: `z_user`

**Indexes**: 
- `email` (background index for fast lookups)

### Project Model

```java
@Document("project_context")
public class Project {
    @Id
    private String id;                    // MongoDB ObjectId
    
    private String projectName;           // User-defined name
    private List<String> instructions;    // Project guidelines
    private ArtStyle artStyle;            // PIXELATED, ANIME, etc.
    
    private String userId;                // Owner reference
    
    @CreatedDate
    private Date createdDate;
    
    @LastModifiedDate
    private Date modifiedDate;
}
```

**ArtStyle Enum Values**:
- `PIXELATED` - Retro pixel art style
- `HAND_DRAWN` - Hand-drawn illustration style
- `MINIMALIST` - Simple, clean design
- `ANIME` - Japanese anime/manga style
- `CARTOON` - Western cartoon style
- `REALISTIC` - Realistic rendering
- `HYPER_REALISTIC` - Photorealistic style
- `CUSTOM` - User-defined custom style

**Collections**: `project_context`

### Media Model

```java
@Document("image")
public class Media {
    @Id
    private String id;                    // MongoDB ObjectId
    
    private MediaType mediaType;          // IMAGE or VIDEO
    private String userId;                // Owner reference
    private String mediaPath;             // S3 path
    
    // CloudFront signed URL info
    private PresignedMediaInfo presignedMediaInfo;
    
    @CreatedDate
    private Date createdDate;
}

public class PresignedMediaInfo {
    private String url;                   // CloudFront signed URL
    private long expiration;              // URL expiration timestamp
}
```

**MediaType Enum**:
- `IMAGE` - Static image (PNG, JPEG, WebP)
- `VIDEO` - Video file (MP4)

**Collections**: `image`

**Storage Structure**:
```
S3 Bucket: {AWS_S3_BUCKET_NAME}
├── users/
│   ├── {userId}/
│   │   ├── images/
│   │   │   ├── {timestamp}.png
│   │   │   └── {timestamp}.jpg
│   │   ├── videos/
│   │   │   └── {timestamp}.mp4
│   │   └── uploads/
│   │       └── {timestamp}.png
```

### Album Model

```java
@Document("album")
public class Album {
    @Id
    private String id;                    // MongoDB ObjectId
    
    private String name;                  // Album name
    private String userId;                // Owner reference
    private String projectId;             // Optional project link
    
    private List<String> images;          // Media IDs
    private List<String> videos;          // Media IDs
    
    @CreatedDate
    private Date createdDate;
    
    @LastModifiedDate
    private Date modifiedDate;
}
```

**Album Types**:
1. **Project Albums**: Automatically created with projects (`projectId` set)
2. **User Albums**: Custom albums created by users (`projectId` = "")

**Collections**: `album`

---

## API Endpoints

### Authentication Endpoints

#### POST /api/user/v1/authenticate
Authenticate user with email and password.

**Headers**:
```
X-auth-token: {API_TOKEN}
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Authentication successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "user_123",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "USER"
    }
  }
}
```

#### POST /api/user/v1/register
Register a new user account.

**Headers**:
```
X-auth-token: {API_TOKEN}
```

**Request Body**:
```json
{
  "email": "newuser@example.com",
  "password": "securePassword123",
  "firstName": "Jane",
  "lastName": "Smith"
}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Registration success",
  "data": null
}
```

#### POST /api/user/v1/refresh_jwt
Refresh expired access token.

**Headers**:
```
X-auth-token: {API_TOKEN}
```

**Request Body**:
```json
"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Success",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### GET /api/user/v1/authenticate_oauth2
OAuth2 authentication callback endpoint.

**Query Parameters**: Handled by Spring Security OAuth2

**Response**: Redirects to client app with JWT tokens

---

### Project Endpoints

#### POST /api/project/v1/create
Create a new project.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "projectName": "Fantasy RPG",
  "artStyle": "ANIME",
  "instructions": [
    "Medieval fantasy theme",
    "Vibrant colors",
    "Character-focused"
  ]
}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Create project successfully",
  "data": {
    "id": "proj_123",
    "projectName": "Fantasy RPG",
    "artStyle": "ANIME",
    "instructions": ["Medieval fantasy theme", "Vibrant colors"],
    "createdDate": "03/12/2025 10:30:00",
    "modifiedDate": "03/12/2025 10:30:00"
  }
}
```

#### GET /api/project/v1/get_by_id
Get project details by ID.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:
- `projectId`: Project ID

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Fetch project successfully",
  "data": {
    "id": "proj_123",
    "projectName": "Fantasy RPG",
    "artStyle": "ANIME",
    "instructions": ["Medieval fantasy theme"],
    "createdDate": "03/12/2025 10:30:00",
    "modifiedDate": "03/12/2025 10:30:00"
  }
}
```

#### PUT /api/project/v1/update_instructions
Update project instructions (replace all).

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "projectId": "proj_123",
  "instructions": [
    "Dark fantasy theme",
    "Muted color palette"
  ]
}
```

#### PUT /api/project/v1/add_instructions
Add new instructions to project (append).

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "projectId": "proj_123",
  "instructions": [
    "Use dramatic lighting"
  ]
}
```

#### DELETE /api/project/v1/delete
Delete a project.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:
- `projectId`: Project ID to delete

---

### Image Generation Endpoints

#### POST /api/image_generation/v1/splash_art
Generate splash art images.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "projectId": "proj_123",
  "splashDescription": "Epic dragon battle scene",
  "numberOfOutputs": 3
}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Image generation successful",
  "data": {
    "imageUrls": [
      "https://cdn.example.com/image1.png?Expires=1234&Signature=...",
      "https://cdn.example.com/image2.png?Expires=1234&Signature=...",
      "https://cdn.example.com/image3.png?Expires=1234&Signature=..."
    ],
    "updatedInstruction": "Use more dramatic lighting for battle scenes"
  }
}
```

#### POST /api/image_generation/v1/sprite_sheet
Generate sprite sheet for character animation.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "projectId": "proj_123",
  "characterDescription": "Knight in blue armor",
  "characterAction": "Running animation, 8 frames",
  "numberOfOutputs": 2
}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Sprite sheet generation successful",
  "data": {
    "imageUrls": [
      "https://cdn.example.com/sprite1.png?Expires=...",
      "https://cdn.example.com/sprite2.png?Expires=..."
    ],
    "updatedInstruction": "N/A"
  }
}
```

#### POST /api/image_generation/v1/variation
Generate variations of existing images.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "projectId": "proj_123",
  "prompt": "Add magical effects",
  "imageInfos": [
    {
      "imagePath": "users/user123/images/character.png",
      "mimeType": "IMAGE_PNG"
    }
  ],
  "numberOfOutputs": 2
}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Image variation generation successful",
  "data": {
    "imageUrls": [
      "https://cdn.example.com/variation1.png?Expires=...",
      "https://cdn.example.com/variation2.png?Expires=..."
    ],
    "updatedInstruction": "Magical effects work well with this style"
  }
}
```

#### POST /api/image_generation/v1/style_change
Change art style of existing images.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "projectId": "proj_123",
  "newArtStyle": "PIXELATED",
  "imageInfos": [
    {
      "imagePath": "users/user123/images/character.png",
      "mimeType": "IMAGE_PNG"
    }
  ]
}
```

---

### Video Generation Endpoints

#### POST /api/video_generation/v1/generate
Generate animated videos.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body** (Text-to-Video):
```json
{
  "projectId": "proj_123",
  "videoDescription": "Dragon flying over mountains at sunset",
  "videoDuration": 6
}
```

**Request Body** (Image-to-Video):
```json
{
  "projectId": "proj_123",
  "videoDescription": "Animate this character walking",
  "videoDuration": 8,
  "referenceImage": {
    "imagePath": "users/user123/images/character.png",
    "mimeType": "IMAGE_PNG"
  }
}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Video generation successful",
  "data": {
    "videoUrl": "https://cdn.example.com/video.mp4?Expires=..."
  }
}
```

**Duration Options**: 4, 6, or 8 seconds

---

### Media Management Endpoints

#### GET /api/media/v1/gallery
Get all user's media.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Gallery fetched successfully",
  "data": [
    {
      "id": "media_123",
      "mediaType": "IMAGE",
      "presignedUrl": "https://cdn.example.com/image.png?Expires=...",
      "createdDate": "03/12/2025 10:30:00"
    },
    {
      "id": "media_456",
      "mediaType": "VIDEO",
      "presignedUrl": "https://cdn.example.com/video.mp4?Expires=...",
      "createdDate": "03/12/2025 11:15:00"
    }
  ]
}
```

#### GET /api/media/v1/get_by_album
Get media from specific album.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:
- `albumId`: Album ID

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Album images fetched successfully",
  "data": [
    {
      "id": "media_123",
      "mediaType": "IMAGE",
      "presignedUrl": "https://cdn.example.com/image.png?Expires=...",
      "createdDate": "03/12/2025 10:30:00"
    }
  ]
}
```

#### POST /api/media/v1/upload_client
Upload client image (base64).

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "base64Data": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "mimeType": "IMAGE_PNG"
}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Upload client image successfully",
  "data": {
    "id": "media_789",
    "mediaType": "IMAGE",
    "presignedUrl": "https://cdn.example.com/upload.png?Expires=...",
    "createdDate": "03/12/2025 12:00:00"
  }
}
```

#### DELETE /api/media/v1/delete
Delete media file.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:
- `imageId`: Media ID to delete

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Delete image successfully",
  "data": null
}
```

---

### Album Management Endpoints

#### POST /api/album/v1/create
Create a new album.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "name": "Character Designs"
}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Create album successfully",
  "data": {
    "id": "album_123",
    "name": "Character Designs",
    "createdDate": "03/12/2025 10:30:00",
    "modifiedDate": "03/12/2025 10:30:00"
  }
}
```

#### PUT /api/album/v1/add_image
Add media to album.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "albumId": "album_123",
  "mediaId": "media_456"
}
```

#### PUT /api/album/v1/delete_image
Remove media from album.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "albumId": "album_123",
  "mediaId": "media_456"
}
```

#### DELETE /api/album/v1/delete
Delete an album.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:
- `albumId`: Album ID to delete

---

### User Management Endpoints

#### GET /api/user/v1/current_user
Get current authenticated user.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Get user success",
  "data": {
    "id": "user_123",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER",
    "active": true,
    "isEmailValidated": true
  }
}
```

#### PUT /api/user/v1/change_password
Change user password.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "oldPassword": "currentPassword123",
  "newPassword": "newSecurePassword456"
}
```

#### POST /api/user/v1/email/validate
Send email validation code.

**Headers**:
```
X-auth-token: {API_TOKEN}
Authorization: Bearer {JWT_TOKEN}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Send validation code success",
  "data": null
}
```

#### POST /api/user/v1/email/token
Verify email with token.

**Headers**:
```
X-auth-token: {API_TOKEN}
```

**Request Body**:
```json
"verification_token_xyz"
```

#### POST /api/user/v1/forgot_password
Request password reset.

**Headers**:
```
X-auth-token: {API_TOKEN}
```

**Request Body**:
```json
"user@example.com"
```

#### POST /api/user/v1/create_new_password
Create new password with reset token.

**Headers**:
```
X-auth-token: {API_TOKEN}
```

**Request Body**:
```json
{
  "token": "reset_token_xyz",
  "newPassword": "newSecurePassword789"
}
```

---

## Security Architecture

### Authentication Layers

1. **API Token Layer**
   - All endpoints require `X-auth-token` header
   - Validates against `API_TOKEN` environment variable
   - First line of defense

2. **JWT Authentication Layer**
   - Most endpoints require `Authorization: Bearer {token}` header
   - JWT contains user ID, email, and role
   - Validated on each request by `JwtRequestFilterUser`

3. **OAuth2 Integration**
   - Google OAuth2: `/oauth2/authorization/google`
   - GitHub OAuth2: `/oauth2/authorization/github`
   - Handles redirect flow and token exchange

### JWT Token Structure

**Access Token** (1-hour expiry):
```json
{
  "sub": "user@example.com",
  "userId": "user_123",
  "role": "USER",
  "iat": 1701604800,
  "exp": 1701608400
}
```

**Refresh Token** (30-day expiry):
```json
{
  "sub": "user@example.com",
  "type": "refresh",
  "iat": 1701604800,
  "exp": 1704196800
}
```

### Security Features

- **Stateless Authentication**: No server-side session storage
- **Password Hashing**: BCrypt with salt (strength 12)
- **Token Expiration**: Access tokens expire in 1 hour
- **Refresh Mechanism**: Refresh tokens for seamless re-authentication
- **Failed Login Protection**: Account locked after 5 failed attempts
- **Email Verification**: Required before account activation
- **Password Reset**: Time-limited tokens (1 hour)
- **CORS Protection**: Configurable allowed origins
- **CloudFront Signed URLs**: Time-limited media access (12 hours)

### Public Endpoints (No Authentication Required)

- `POST /api/user/v1/authenticate`
- `POST /api/user/v1/register`
- `POST /api/user/v1/refresh_jwt`
- `POST /api/user/v1/forgot_password`
- `POST /api/user/v1/create_new_password`
- `GET /oauth2/**` (OAuth2 flow)
- `GET /swagger-ui/**` (API documentation)

---

## Deployment

### Prerequisites
