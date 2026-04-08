# Pinlio - Pinterest Clone

**A Pinterest-like social platform built with Java Spring Boot microservices.**

---

## 🚀 MVP Phase (Week 1-2) - START HERE

### What We're Building

| Service | Purpose | Database | Week |
|---------|---------|----------|------|
| **User Service** | Auth, profiles, follow relationships | PostgreSQL | 1 |
| **Pin Service** | Create, read, update, delete pins | PostgreSQL | 1 |
| **Board Service** | Collections/boards of pins | PostgreSQL | 2 |
| **Interaction Service** | Likes & saves on pins | PostgreSQL | 2 |

### MVP Tech Stack

- **Backend:** Spring Boot (Java 21)
- **Database:** PostgreSQL (single source of truth)
- **Cache:** Redis (for counters, sessions)
- **Local Dev:** Docker Compose
- **CI/CD:** GitHub Actions (build → test → docker → deploy)
- **Deploy:** AWS ECS / Heroku / DigitalOcean

### Local Development Setup

```bash
# Start services
docker-compose up -d

# Services available:
# PostgreSQL: localhost:5432
# Redis: localhost:6379
# API: localhost:8080
```

**Docker Compose for MVP:**
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: pinlio
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

### MVP API Endpoints

```
USER SERVICE
POST   /api/users/register           - User registration
POST   /api/users/login              - User login (JWT)
GET    /api/users/{userId}           - Get user profile
PUT    /api/users/{userId}           - Update profile
POST   /api/users/{userId}/follow    - Follow a user
DELETE /api/users/{userId}/unfollow  - Unfollow a user
GET    /api/users/{userId}/followers - Get followers
GET    /api/users/{userId}/following - Get following

PIN SERVICE
POST   /api/pins                     - Create pin
GET    /api/pins/{pinId}             - Get pin details
PUT    /api/pins/{pinId}             - Update pin
DELETE /api/pins/{pinId}             - Delete pin
GET    /api/pins/user/{userId}       - Get user's pins

BOARD SERVICE
POST   /api/boards                   - Create board
GET    /api/boards/{boardId}         - Get board
PUT    /api/boards/{boardId}         - Update board
DELETE /api/boards/{boardId}         - Delete board
GET    /api/boards/user/{userId}     - Get user's boards
POST   /api/boards/{boardId}/pins/{pinId}    - Add pin to board
DELETE /api/boards/{boardId}/pins/{pinId}   - Remove pin from board

INTERACTION SERVICE
POST   /api/interactions/pins/{pinId}/like    - Like a pin
DELETE /api/interactions/pins/{pinId}/like/{userId}    - Unlike a pin
POST   /api/interactions/pins/{pinId}/save    - Save a pin
DELETE /api/interactions/pins/{pinId}/save/{userId}   - Unsave a pin
GET    /api/interactions/pins/{pinId}/likes-count    - Get likes count
GET    /api/interactions/pins/{pinId}/saves-count    - Get saves count
```

### MVP Database Schema

```sql
CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  username VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  bio TEXT,
  profile_image_url VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_follows (
  id UUID PRIMARY KEY,
  follower_id UUID NOT NULL REFERENCES users(id),
  following_id UUID NOT NULL REFERENCES users(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(follower_id, following_id)
);

CREATE TABLE boards (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  name VARCHAR(255) NOT NULL,
  description TEXT,
  visibility VARCHAR(50) DEFAULT 'PUBLIC',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pins (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  board_id UUID REFERENCES boards(id),
  title VARCHAR(255) NOT NULL,
  description TEXT,
  image_url VARCHAR(255) NOT NULL,
  source_url VARCHAR(255),
  tags TEXT[],
  visibility VARCHAR(50) DEFAULT 'PUBLIC',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE board_pins (
  id UUID PRIMARY KEY,
  board_id UUID NOT NULL REFERENCES boards(id),
  pin_id UUID NOT NULL REFERENCES pins(id),
  added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(board_id, pin_id)
);

CREATE TABLE pin_likes (
  id UUID PRIMARY KEY,
  pin_id UUID NOT NULL REFERENCES pins(id),
  user_id UUID NOT NULL REFERENCES users(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(pin_id, user_id)
);

CREATE TABLE pin_saves (
  id UUID PRIMARY KEY,
  pin_id UUID NOT NULL REFERENCES pins(id),
  user_id UUID NOT NULL REFERENCES users(id),
  saved_to_board UUID REFERENCES boards(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(pin_id, user_id)
);
```

### MVP CI/CD Pipeline

**GitHub Actions (Simple):**
```yaml
name: Build & Deploy

on:
  push:
    branches: [main, develop]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      
      - name: Build with Maven
        run: mvn clean package
      
      - name: Run Tests
        run: mvn test
      
      - name: Build Docker image
        run: docker build -t pinlio:${{ github.sha }} .
      
      - name: Push to registry
        run: docker push pinlio:${{ github.sha }}
```

### MVP Deliverables (By End of Week 2)

- [x] User Service (register, login, follow)
- [x] Pin Service (CRUD)
- [x] Board Service (CRUD)
- [x] Interaction Service (likes, saves)
- [x] PostgreSQL schema
- [x] JWT authentication
- [x] Docker Compose setup
- [x] GitHub Actions pipeline
- [x] Deployed to staging
- [x] API documentation

---

## 📈 Phase 2 (Week 3-4)

**Add when MVP is stable:**

- Comment Service (comments on pins)
- Feed Service (personalized feed with simple SQL)
- PostgreSQL full-text search for pins
- Notifications (Redis Pub/Sub polling)
- Rate limiting
- Advanced caching

---

## 🏢 Full Enterprise Architecture (Month 2+)

**Long-term vision when scaling:**

## Full Enterprise Architecture (Month 2+)

**Long-term vision when scaling:**

### Complete Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway (Spring Cloud Gateway)         │
└─────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────┬───────────┬───┼───────┬───────────┬─────────┐
        │           │           │   │       │           │         │
    ┌───▼───┐  ┌───▼────┐ ┌───▼──┐ │  ┌───▼────┐ ┌───▼────┐ ┌──▼───┐
    │ User  │  │  Pin   │ │Board │ │  │ Search │ │  Feed  │ │Image │
    │Service│  │Service │ │Service│ │  │Service │ │Service │ │Service│
    └───┬───┘  └───┬────┘ └───┬──┘ │  └───┬────┘ └───┬────┘ └──┬───┘
        │          │          │    │      │          │         │
    ┌───▼───┐  ┌───▼────┐ ┌──▼──┐ ├─ ┌───▼────┐ ┌──▼───┐ ┌───▼──┐
    │Comment│  │Interact│ │  DB │ │  │ Search │ │Cache │ │Storage│
    │Service│  │ Service│ │     │ │  │  Index │ │Redis │ │(S3)   │
    └───────┘  └────────┘ └─────┘ │  └────────┘ └──────┘ └───────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │  Notification Service       │
                    └─────────────────────────────┘
                                   │
        ┌──────────────────────────┼──────────────────────────┐
        │                          │                          │
   ┌────▼──────┐          ┌────────▼────────┐       ┌────────▼────┐
   │ RabbitMQ  │          │    Kafka        │       │   Redis     │
   │Message    │          │Event Streaming  │       │  Pub/Sub    │
   │Queue      │          │(Event Log)      │       │ (Fallback)  │
   └───────────┘          └─────────────────┘       └─────────────┘
```

### All Services (Enterprise)


### 1. **User Service** (PostgreSQL)
Manages user accounts, profiles, authentication, and social relationships.

**Entities:**
```
User
├── id (UUID)
├── email (String, unique)
├── username (String, unique)
├── passwordHash (String)
├── profileImage (String - URL)
├── bio (String)
├── followersCount (Integer)
├── followingCount (Integer)
├── createdAt (LocalDateTime)
├── updatedAt (LocalDateTime)
└── isActive (Boolean)

UserFollow
├── id (UUID)
├── followerId (UUID, FK)
├── followingId (UUID, FK)
├── createdAt (LocalDateTime)
└── isActive (Boolean)
```

**Database:** PostgreSQL
**APIs:**
- POST /api/users/register
- POST /api/users/login
- GET /api/users/{userId}
- PUT /api/users/{userId}
- POST /api/users/{userId}/follow
- DELETE /api/users/{userId}/follow/{followingId}
- GET /api/users/{userId}/followers
- GET /api/users/{userId}/following

---

### 2. **Pin Service** (PostgreSQL)
Core service for creating and managing pins.

**Entities:**
```
Pin
├── id (UUID)
├── userId (UUID, FK to User Service)
├── boardId (UUID, FK)
├── title (String)
├── description (String)
├── imageUrl (String)
├── sourceUrl (String)
├── imageWidth (Integer)
├── imageHeight (Integer)
├── tags (List<String>)
├── visibility (Enum: PUBLIC, PRIVATE, SECRET)
├── createdAt (LocalDateTime)
├── updatedAt (LocalDateTime)
└── isDeleted (Boolean)

PinMedia
├── id (UUID)
├── pinId (UUID, FK)
├── mediaUrl (String)
├── mediaType (Enum: IMAGE, VIDEO)
├── order (Integer)
└── uploadedAt (LocalDateTime)
```

**Database:** PostgreSQL (Primary) + Redis (Cache)
**APIs:**
- POST /api/pins (create)
- GET /api/pins/{pinId}
- PUT /api/pins/{pinId}
- DELETE /api/pins/{pinId}
- GET /api/pins/user/{userId}
- POST /api/pins/{pinId}/upload-image

**Events Published (to RabbitMQ/Kafka):**
- `pin.created`
- `pin.updated`
- `pin.deleted`

---

### 3. **Board Service** (PostgreSQL)
Manages collections/boards of pins.

**Entities:**
```
Board
├── id (UUID)
├── userId (UUID, FK)
├── name (String)
├── description (String)
├── coverImage (String)
├── visibility (Enum: PUBLIC, PRIVATE, SECRET)
├── collaborators (List<UUID>)
├── pinsCount (Integer)
├── createdAt (LocalDateTime)
├── updatedAt (LocalDateTime)
└── isDeleted (Boolean)

BoardPin
├── id (UUID)
├── boardId (UUID, FK)
├── pinId (UUID, FK)
├── addedAt (LocalDateTime)
└── order (Integer)

BoardCollaborator
├── id (UUID)
├── boardId (UUID, FK)
├── userId (UUID, FK)
├── role (Enum: OWNER, EDITOR, VIEWER)
└── addedAt (LocalDateTime)
```

**Database:** PostgreSQL
**APIs:**
- POST /api/boards (create)
- GET /api/boards/{boardId}
- PUT /api/boards/{boardId}
- DELETE /api/boards/{boardId}
- GET /api/boards/user/{userId}
- POST /api/boards/{boardId}/pins/{pinId}
- DELETE /api/boards/{boardId}/pins/{pinId}
- POST /api/boards/{boardId}/collaborators

---

### 4. **Interaction Service** (PostgreSQL + Redis)
Handles likes and saves on pins.

**Entities:**
```
PinLike
├── id (UUID)
├── pinId (UUID, FK)
├── userId (UUID, FK)
├── createdAt (LocalDateTime)
└── unique constraint (pinId, userId)

PinSave
├── id (UUID)
├── pinId (UUID, FK)
├── userId (UUID, FK)
├── savedToBoard (UUID, FK to Board)
├── createdAt (LocalDateTime)
└── unique constraint (pinId, userId)
```

**Database:** PostgreSQL + Redis for counters
**APIs:**
- POST /api/interactions/pins/{pinId}/like
- DELETE /api/interactions/pins/{pinId}/like/{userId}
- POST /api/interactions/pins/{pinId}/save
- DELETE /api/interactions/pins/{pinId}/save/{userId}
- GET /api/interactions/pins/{pinId}/likes-count
- GET /api/interactions/pins/{pinId}/saves-count
- GET /api/interactions/users/{userId}/saved-pins

**Events Published:**
- `pin.liked`
- `pin.unliked`
- `pin.saved`
- `pin.unsaved`

---

### 5. **Comment Service** (PostgreSQL)
Manages comments on pins.

**Entities:**
```
Comment
├── id (UUID)
├── pinId (UUID, FK)
├── userId (UUID, FK)
├── text (String)
├── createdAt (LocalDateTime)
├── updatedAt (LocalDateTime)
├── isDeleted (Boolean)
└── repliesCount (Integer)

CommentReply
├── id (UUID)
├── commentId (UUID, FK)
├── userId (UUID, FK)
├── text (String)
├── createdAt (LocalDateTime)
├── updatedAt (LocalDateTime)
└── isDeleted (Boolean)
```

**Database:** PostgreSQL
**APIs:**
- POST /api/comments (create)
- GET /api/comments/{commentId}
- PUT /api/comments/{commentId}
- DELETE /api/comments/{commentId}
- GET /api/pins/{pinId}/comments
- POST /api/comments/{commentId}/reply
- GET /api/comments/{commentId}/replies

**Events Published:**
- `comment.created`
- `comment.deleted`

---

### 6. **Search Service** (Elasticsearch + PostgreSQL)
Provides advanced search and discovery across pins and boards.

**Indexed Documents:**
```
PinIndex
├── id (UUID)
├── userId (UUID)
├── title (String, analyzed)
├── description (String, analyzed)
├── tags (List<String>)
├── likes (Integer)
├── saves (Integer)
├── createdAt (DateTime)
├── visibility (String)

BoardIndex
├── id (UUID)
├── userId (UUID)
├── name (String, analyzed)
├── description (String, analyzed)
├── pinsCount (Integer)
├── createdAt (DateTime)
├── visibility (String)

UserIndex
├── id (UUID)
├── username (String, analyzed)
├── bio (String, analyzed)
├── followersCount (Integer)
```

**Database:** Elasticsearch (primary) + PostgreSQL (fallback)
**APIs:**
- GET /api/search/pins?q=query&tags=tag1,tag2&sort=trending
- GET /api/search/boards?q=query
- GET /api/search/users?q=query
- GET /api/search/trending?category=pins|boards|users
- GET /api/search/suggestions?q=partial

**Events Consumed:**
- `pin.created`, `pin.updated`, `pin.deleted` (to update indices)
- `board.created`, `board.updated`, `board.deleted`
- `user.profile.updated`

---

### 7. **Feed Service** (PostgreSQL + Redis)
Generates personalized feeds for users based on their follows.

**Entities:**
```
Feed
├── id (UUID)
├── userId (UUID, FK)
├── pins (List<UUID>)
├── generatedAt (LocalDateTime)
└── expiresAt (LocalDateTime)

FeedAlgorithm (Configuration)
├── id (UUID)
├── userId (UUID)
├── preferences (JSON: tags, categories)
├── followedUsersWeight (Double)
├── trendingWeight (Double)
├── savedPinsWeight (Double)
└── updatedAt (LocalDateTime)
```

**Database:** PostgreSQL + Redis Cache
**APIs:**
- GET /api/feed?page=0&size=20
- GET /api/feed/personalized?limit=50
- PUT /api/feed/preferences
- GET /api/feed/trending

**Events Consumed:**
- `pin.created` (add to follower feeds)
- `user.followed` (update feed generation)
- `pin.liked` (boost similar pins)

---

### 8. **Notification Service** (MongoDB + PostgreSQL)
Handles real-time notifications for user activities.

**Entities:**
```
Notification (MongoDB)
├── _id (ObjectId)
├── userId (UUID)
├── type (Enum: PIN_LIKED, PIN_SAVED, COMMENT_ADDED, USER_FOLLOWED, etc.)
├── actorId (UUID - user who triggered)
├── relatedEntityId (UUID)
├── title (String)
├── message (String)
├── isRead (Boolean)
├── createdAt (DateTime)
└── expiresAt (DateTime)

NotificationPreference (PostgreSQL)
├── id (UUID)
├── userId (UUID)
├── emailOnLike (Boolean)
├── emailOnComment (Boolean)
├── emailOnFollow (Boolean)
├── emailOnSave (Boolean)
├── pushNotifications (Boolean)
└── updatedAt (LocalDateTime)
```

**Database:** MongoDB (Notifications) + PostgreSQL (Preferences)
**APIs:**
- GET /api/notifications?page=0&size=20
- PUT /api/notifications/{notificationId}/read
- PUT /api/notifications/read-all
- PUT /api/notifications/preferences
- GET /api/notifications/preferences
- WebSocket: /ws/notifications (real-time)

**Events Consumed:**
- All activity events (likes, comments, follows, etc.)

---

### 9. **Image Service** (PostgreSQL + AWS S3)
Manages image uploads, processing, and storage.

**Entities:**
```
ImageUpload
├── id (UUID)
├── userId (UUID, FK)
├── s3Key (String)
├── s3Bucket (String)
├── originalFilename (String)
├── mimeType (String)
├── fileSize (Long)
├── width (Integer)
├── height (Integer)
├── status (Enum: UPLOADING, PROCESSING, READY, FAILED)
├── thumbnails (List<ImageThumbnail>)
├── createdAt (LocalDateTime)
└── expiresAt (LocalDateTime)

ImageThumbnail
├── id (UUID)
├── imageUploadId (UUID)
├── size (Enum: SMALL_100, MEDIUM_300, LARGE_600)
├── s3Key (String)
└── url (String)
```

**Database:** PostgreSQL + AWS S3
**APIs:**
- POST /api/images/upload (multipart)
- GET /api/images/{imageId}
- DELETE /api/images/{imageId}
- POST /api/images/{imageId}/process
- GET /api/images/{imageId}/thumbnails

---

## Communication Patterns

### Synchronous (REST)
```
API Gateway → Service → Database
  ✓ User authentication
  ✓ CRUD operations
  ✓ Direct data retrieval
```

### Asynchronous (RabbitMQ/Kafka)

**Event Flow:**
```
Event Producer → Message Broker → Event Consumers

Example: When a pin is created:
  1. Pin Service publishes: "pin.created" event
  2. RabbitMQ routes to:
     - Search Service (index the pin)
     - Feed Service (add to follower feeds)
     - Notification Service (notify followers)
     - Board Service (update pin count if added to board)

Example: When a user is followed:
  1. User Service publishes: "user.followed" event
  2. Consumers:
     - Feed Service (rebuild personalized feed)
     - Notification Service (notify the followed user)
```

### Message Queue Events

**Pin Events:**
```
pin.created {id, userId, title, tags, visibility, createdAt}
pin.updated {id, userId, title, description}
pin.deleted {id, userId}
pin.liked {pinId, userId, likesCount}
pin.unliked {pinId, userId, likesCount}
pin.saved {pinId, userId, boardId}
pin.unsaved {pinId, userId}
```

**User Events:**
```
user.registered {userId, email, username, createdAt}
user.updated {userId, username, bio}
user.followed {followerId, followingId}
user.unfollowed {followerId, followingId}
```

**Board Events:**
```
board.created {id, userId, name, visibility}
board.updated {id, name}
board.deleted {id}
board.pin.added {boardId, pinId}
board.pin.removed {boardId, pinId}
```

**Comment Events:**
```
comment.created {id, pinId, userId, text}
comment.deleted {id, pinId}
```

---

## Database Schema per Service

### PostgreSQL Services (User, Pin, Board, Interaction, Comment, Feed)

**Indexes:**
```sql
-- User Service
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);

-- Pin Service
CREATE INDEX idx_pin_user ON pins(user_id);
CREATE INDEX idx_pin_board ON pins(board_id);
CREATE INDEX idx_pin_created ON pins(created_at DESC);
CREATE INDEX idx_pin_tags ON pins USING GIN(tags);

-- Board Service
CREATE INDEX idx_board_user ON boards(user_id);
CREATE INDEX idx_board_pin ON board_pins(board_id);

-- Interaction Service
CREATE UNIQUE INDEX idx_like_unique ON pin_likes(pin_id, user_id);
CREATE UNIQUE INDEX idx_save_unique ON pin_saves(pin_id, user_id);
```

### MongoDB (Notification Service)

**Collections:**
```
db.notifications
  ├── Indexes:
  │   ├── {userId: 1, createdAt: -1}
  │   ├── {userId: 1, isRead: 1}
  │   └── {expiresAt: 1} (TTL: 30 days)
  └── Indexes for searches

db.notification_preferences
  ├── Indexes:
  │   └── {userId: 1}
```

### Elasticsearch (Search Service)

**Indexes:**
```
pins_v1
├── Mapping: title(text), description(text), tags(keyword), 
│            visibility(keyword), likes(long), createdAt(date)
├── Analyzer: Standard English analyzer
└── Shards: 3, Replicas: 1

boards_v1
├── Mapping: name(text), description(text), visibility(keyword)

users_v1
├── Mapping: username(text), bio(text), followersCount(long)
```

### Redis (Caching & Session)

**Keys:**
```
user:{userId}:profile -> User profile cache (TTL: 1 hour)
pin:{pinId}:likes:count -> Pin likes count (Real-time)
pin:{pinId}:saves:count -> Pin saves count (Real-time)
user:{userId}:feed -> Cached feed (TTL: 30 minutes)
user:{userId}:session -> User session (TTL: 7 days)
trending:pins -> Top trending pins (TTL: 1 hour)
search:suggestions -> Auto-complete suggestions (TTL: 1 day)
```

---

## API Gateway Configuration

```yaml
Routes:
  /api/users/** → User Service (8001)
  /api/pins/** → Pin Service (8002)
  /api/boards/** → Board Service (8003)
  /api/interactions/** → Interaction Service (8004)
  /api/comments/** → Comment Service (8005)
  /api/search/** → Search Service (8006)
  /api/feed/** → Feed Service (8007)
  /api/notifications/** → Notification Service (8008)
  /api/images/** → Image Service (8009)

Features:
  - Rate limiting per user
  - JWT token validation
  - Request/Response logging
  - Circuit breaker patterns
  - Load balancing
```

---

# Full Enterprise Architecture (Month 2+)

## Overview

```
Developer Push → GitHub → GitHub Actions → Build → Test → 
  Security Scan → Docker Build → Registry → Dev Deploy → 
  Integration Tests → Staging Deploy → E2E Tests → 
  Production Deploy (Manual Approval)
```

## GitHub Actions Workflow

### 1. **Build & Test Pipeline**

**File: `.github/workflows/build-test.yml`**
```yaml
name: Build and Test

on:
  push:
    branches: [main, develop, feature/**]
  pull_request:
    branches: [main, develop]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  detect-changes:
    runs-on: ubuntu-latest
    outputs:
      services: ${{ steps.detect.outputs.services }}
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      
      - name: Detect changed services
        id: detect
        run: |
          if [[ "${{ github.event_name }}" == "pull_request" ]]; then
            CHANGED_FILES=$(git diff --name-only origin/${{ github.base_ref }}...HEAD)
          else
            CHANGED_FILES=$(git diff --name-only HEAD~1 HEAD)
          fi
          
          SERVICES=()
          [[ $CHANGED_FILES == *"services/user-service"* ]] && SERVICES+=(user-service)
          [[ $CHANGED_FILES == *"services/pin-service"* ]] && SERVICES+=(pin-service)
          [[ $CHANGED_FILES == *"services/board-service"* ]] && SERVICES+=(board-service)
          [[ $CHANGED_FILES == *"services/interaction-service"* ]] && SERVICES+=(interaction-service)
          [[ $CHANGED_FILES == *"services/comment-service"* ]] && SERVICES+=(comment-service)
          [[ $CHANGED_FILES == *"services/search-service"* ]] && SERVICES+=(search-service)
          [[ $CHANGED_FILES == *"services/feed-service"* ]] && SERVICES+=(feed-service)
          [[ $CHANGED_FILES == *"services/notification-service"* ]] && SERVICES+=(notification-service)
          [[ $CHANGED_FILES == *"services/image-service"* ]] && SERVICES+=(image-service)
          
          echo "services=$(echo ${SERVICES[@]} | jq -R 'split(" ")')" >> $GITHUB_OUTPUT

  build-and-test:
    needs: detect-changes
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: ${{ fromJson(needs.detect-changes.outputs.services) }}
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Build with Maven
        run: |
          cd services/${{ matrix.service }}
          mvn clean package -DskipTests=true
      
      - name: Run Unit Tests
        run: |
          cd services/${{ matrix.service }}
          mvn test
      
      - name: SonarQube Code Quality Scan
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
        run: |
          cd services/${{ matrix.service }}
          mvn sonar:sonar \
            -Dsonar.projectKey=${{ matrix.service }} \
            -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }} \
            -Dsonar.login=${{ secrets.SONAR_TOKEN }}
      
      - name: Run Integration Tests
        run: |
          cd services/${{ matrix.service }}
          mvn verify -Dgroups=integration
      
      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results-${{ matrix.service }}
          path: services/${{ matrix.service }}/target/surefire-reports/
      
      - name: Publish Test Report
        if: always()
        uses: dorny/test-reporter@v1
        with:
          name: Test Results - ${{ matrix.service }}
          path: 'services/${{ matrix.service }}/target/surefire-reports/*.xml'
          reporter: 'java-junit'
          fail-on-error: true
```

### 2. **Security Scanning Pipeline**

**File: `.github/workflows/security.yml`**
```yaml
name: Security Scanning

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM

jobs:
  dependency-check:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [user-service, pin-service, board-service, interaction-service, 
                  comment-service, search-service, feed-service, notification-service, image-service]
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Run OWASP Dependency-Check
        uses: dependency-check/Dependency-Check_Action@main
        with:
          path: 'services/${{ matrix.service }}'
          format: 'SARIF'
          args: >
            --enableExperimental
      
      - name: Upload to GitHub Security
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: ./reports/dependency-check-report.sarif
  
  sast-analysis:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Run Semgrep
        uses: returntocorp/semgrep-action@v1
        with:
          config: >
            p/security-audit
            p/owasp-top-ten
            p/cwe-top-25
  
  container-scan:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [user-service, pin-service, board-service, interaction-service,
                  comment-service, search-service, feed-service, notification-service, image-service]
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Build Docker image
        run: |
          cd services/${{ matrix.service }}
          docker build -t ${{ matrix.service }}:${{ github.sha }} .
      
      - name: Run Trivy vulnerability scan
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: ${{ matrix.service }}:${{ github.sha }}
          format: 'sarif'
          output: 'trivy-results.sarif'
      
      - name: Upload Trivy results to GitHub Security
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'
```

### 3. **Docker Build & Push Pipeline**

**File: `.github/workflows/docker-build.yml`**
```yaml
name: Docker Build and Push

on:
  push:
    branches: [main, develop]
  workflow_run:
    workflows: ["Build and Test"]
    types: [completed]
    branches: [main, develop]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  build-and-push:
    if: ${{ github.event.workflow_run.conclusion == 'success' || github.event_name == 'push' }}
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [user-service, pin-service, board-service, interaction-service,
                  comment-service, search-service, feed-service, notification-service, image-service]
    
    permissions:
      contents: read
      packages: write
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2
      
      - name: Log in to Container Registry
        uses: docker/login-action@v2
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      
      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v4
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/${{ matrix.service }}
          tags: |
            type=ref,event=branch
            type=semver,pattern={{version}}
            type=semver,pattern={{major}}.{{minor}}
            type=sha,prefix={{branch}}-
            type=raw,value=latest,enable={{is_default_branch}}
      
      - name: Build and push Docker image
        uses: docker/build-push-action@v4
        with:
          context: services/${{ matrix.service }}
          file: services/${{ matrix.service }}/Dockerfile
          push: ${{ github.event_name != 'pull_request' }}
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
```

### 4. **Deployment Pipeline**

**File: `.github/workflows/deploy.yml`**
```yaml
name: Deploy to Kubernetes

on:
  push:
    branches: [main, develop]
  workflow_run:
    workflows: ["Docker Build and Push"]
    types: [completed]
    branches: [main, develop]
  workflow_dispatch:
    inputs:
      environment:
        description: 'Target environment'
        required: true
        default: 'staging'
        type: choice
        options:
          - dev
          - staging
          - production

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  deploy:
    runs-on: ubuntu-latest
    environment:
      name: ${{ github.ref == 'refs/heads/main' && 'production' || 'staging' }}
    
    strategy:
      matrix:
        service: [user-service, pin-service, board-service, interaction-service,
                  comment-service, search-service, feed-service, notification-service, image-service]
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set environment
        id: env
        run: |
          if [[ "${{ github.ref }}" == "refs/heads/main" ]]; then
            echo "environment=production" >> $GITHUB_OUTPUT
            echo "k8s_cluster=${{ secrets.PROD_K8S_CLUSTER }}" >> $GITHUB_OUTPUT
            echo "k8s_namespace=pinterest-prod" >> $GITHUB_OUTPUT
          else
            echo "environment=staging" >> $GITHUB_OUTPUT
            echo "k8s_cluster=${{ secrets.STAGING_K8S_CLUSTER }}" >> $GITHUB_OUTPUT
            echo "k8s_namespace=pinterest-staging" >> $GITHUB_OUTPUT
          fi
      
      - name: Configure kubectl
        run: |
          mkdir -p $HOME/.kube
          echo "${{ secrets.KUBE_CONFIG }}" | base64 -d > $HOME/.kube/config
          chmod 600 $HOME/.kube/config
      
      - name: Update Helm Chart Values
        run: |
          IMAGE_TAG=${{ github.sha }}
          SERVICE_NAME=${{ matrix.service }}
          ENV=${{ steps.env.outputs.environment }}
          
          sed -i "s|IMAGE_TAG|${IMAGE_TAG}|g" k8s/helm/values-${ENV}.yaml
          sed -i "s|SERVICE_NAME|${SERVICE_NAME}|g" k8s/helm/values-${ENV}.yaml
      
      - name: Deploy with Helm
        run: |
          helm upgrade --install ${{ matrix.service }} \
            k8s/helm \
            --namespace ${{ steps.env.outputs.k8s_namespace }} \
            --values k8s/helm/values-${{ steps.env.outputs.environment }}.yaml \
            --set image.tag=${{ github.sha }} \
            --wait \
            --timeout 5m
      
      - name: Verify Deployment
        run: |
          kubectl rollout status deployment/${{ matrix.service }} \
            -n ${{ steps.env.outputs.k8s_namespace }} \
            --timeout=5m
      
      - name: Run Health Checks
        run: |
          SERVICE_URL="http://${{ matrix.service }}.${{ steps.env.outputs.k8s_namespace }}.svc.cluster.local:8080"
          
          for i in {1..30}; do
            if curl -f ${SERVICE_URL}/actuator/health; then
              echo "Health check passed"
              exit 0
            fi
            sleep 2
          done
          echo "Health check failed"
          exit 1
      
      - name: Run Smoke Tests
        if: steps.env.outputs.environment == 'staging'
        run: |
          npm install -g newman
          newman run tests/postman/pinterest-api.json \
            --environment tests/postman/${{ steps.env.outputs.environment }}-env.json \
            --reporters cli,json
      
      - name: Rollback on Failure
        if: failure()
        run: |
          helm rollback ${{ matrix.service }} \
            -n ${{ steps.env.outputs.k8s_namespace }}
      
      - name: Slack Notification
        if: always()
        uses: slackapi/slack-github-action@v1
        with:
          webhook-url: ${{ secrets.SLACK_WEBHOOK }}
          payload: |
            {
              "text": "Deployment ${{ job.status }}",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*Deployment Status:* ${{ job.status }}\n*Service:* ${{ matrix.service }}\n*Environment:* ${{ steps.env.outputs.environment }}\n*Commit:* <${{ github.server_url }}/${{ github.repository }}/commit/${{ github.sha }}|${{ github.sha }}>"
                  }
                }
              ]
            }
```

### 5. **E2E Testing Pipeline**

**File: `.github/workflows/e2e-tests.yml`**
```yaml
name: End-to-End Tests

on:
  workflow_run:
    workflows: ["Deploy to Kubernetes"]
    types: [completed]
    branches: [main, develop]

jobs:
  e2e-tests:
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
      
      - name: Install dependencies
        run: |
          cd tests/e2e
          npm install
      
      - name: Wait for services to be ready
        run: |
          for service in user-service pin-service board-service; do
            echo "Waiting for $service..."
            while ! curl -f http://${service}:8080/actuator/health; do
              sleep 5
            done
          done
      
      - name: Run Cypress E2E tests
        run: |
          cd tests/e2e
          npx cypress run --spec "cypress/e2e/**/*.cy.ts"
      
      - name: Run API Integration tests
        run: |
          cd tests/integration
          mvn test -Dgroups=e2e
      
      - name: Upload test artifacts
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: e2e-test-results
          path: |
            tests/e2e/cypress/videos/
            tests/e2e/cypress/screenshots/
      
      - name: Performance tests
        run: |
          npm install -g k6
          k6 run tests/performance/load-test.js \
            --vus 10 \
            --duration 30s \
            --out json=results.json
      
      - name: Parse performance results
        run: |
          node tests/performance/parse-results.js results.json
```

## Local Development Setup

### Docker Compose for Development

**File: `docker-compose.yml`**
```yaml
version: '3.8'

services:
  # Databases
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: pinterest
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - pinterest-network

  mongodb:
    image: mongo:7
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: password
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db
    networks:
      - pinterest-network

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    networks:
      - pinterest-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - pinterest-network

  # Message Brokers
  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    ports:
      - "5672:5672"
      - "15672:15672"
    networks:
      - pinterest-network

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper
    networks:
      - pinterest-network

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    networks:
      - pinterest-network

  # Monitoring
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    ports:
      - "9090:9090"
    networks:
      - pinterest-network

  grafana:
    image: grafana/grafana:latest
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    ports:
      - "3000:3000"
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - pinterest-network

volumes:
  postgres_data:
  mongo_data:
  elasticsearch_data:
  redis_data:
  prometheus_data:
  grafana_data:

networks:
  pinterest-network:
    driver: bridge
```

## Kubernetes Deployment Files

### Helm Chart Structure

```
k8s/helm/
├── Chart.yaml
├── values.yaml
├── values-dev.yaml
├── values-staging.yaml
├── values-production.yaml
└── templates/
    ├── deployment.yaml
    ├── service.yaml
    ├── configmap.yaml
    ├── secret.yaml
    ├── ingress.yaml
    ├── hpa.yaml
    ├── pdb.yaml
    └── serviceaccount.yaml
```

**File: `k8s/helm/values-production.yaml`**
```yaml
replicaCount: 3

image:
  repository: ghcr.io/yourorg/pinterest
  tag: latest
  pullPolicy: IfNotPresent

service:
  type: ClusterIP
  port: 8080

ingress:
  enabled: true
  className: nginx
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - host: api.pinterest.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: pinterest-tls
      hosts:
        - api.pinterest.com

resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 500m
    memory: 512Mi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80
  targetMemoryUtilizationPercentage: 80

env:
  SPRING_PROFILES_ACTIVE: production
  LOGGING_LEVEL: INFO

livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

## Testing Strategy

### Unit Tests
```
Maven: mvn test
Coverage: Jacoco (target > 80% coverage)
Framework: JUnit 5 + Mockito
```

### Integration Tests
```
Maven: mvn verify -Dgroups=integration
Database: Testcontainers (PostgreSQL, MongoDB, Elasticsearch)
Message Queue: Embedded RabbitMQ
```

### E2E Tests
```
Frontend: Cypress / Playwright
API: Newman / Postman
Performance: K6 / JMeter
Load Testing: Gatling
```

## Deployment Checklist

### Pre-Deployment
- [ ] All tests passing (unit, integration, E2E)
- [ ] Code coverage > 80%
- [ ] Security scans passing (OWASP, Trivy, Semgrep)
- [ ] Database migrations reviewed
- [ ] API documentation updated
- [ ] Performance benchmarks acceptable

### Staging Deployment
- [ ] All services deployed successfully
- [ ] Health checks passing
- [ ] Smoke tests passing
- [ ] Database migrations applied
- [ ] Configuration validated

### Production Deployment
- [ ] Manual approval from tech lead
- [ ] Backup taken
- [ ] Rollback plan reviewed
- [ ] Blue-green deployment ready
- [ ] Monitoring dashboards prepared
- [ ] On-call team notified

### Post-Deployment
- [ ] Health metrics normal
- [ ] Error rates acceptable
- [ ] Performance metrics acceptable
- [ ] User-facing features working
- [ ] Rollback plan removed if successful

## Monitoring & Observability

### Prometheus Metrics
```
spring_boot_application_ready_time_seconds
jvm_memory_used_bytes
jvm_gc_memory_allocated_bytes
http_server_requests_seconds_bucket
spring_data_repository_invocations_seconds
```

### ELK Stack (Elasticsearch + Logstash + Kibana)
```
- Centralized logging for all services
- Log levels: INFO, WARN, ERROR
- Retention: 30 days
```

### Jaeger Distributed Tracing
```
- Trace requests across services
- Sampling rate: 1% in production
- Retention: 7 days
```

### Grafana Dashboards
```
- Overview: CPU, Memory, Network, Disk
- Application: Request rates, latency, errors
- Database: Query performance, connection pools
- Message Queue: Throughput, lag, errors
```

## Rollback Strategy

### Automatic Rollback
```
- Health check failures: Immediate rollback
- Error rate > 5%: Rollback after 2 minutes
- P99 latency > 2s: Rollback after 5 minutes
```

### Manual Rollback
```
helm rollback SERVICE_NAME REVISION_NUMBER
kubectl rollout undo deployment/SERVICE_NAME
```

## Security Considerations

- SAST scanning with Semgrep
- Dependency vulnerability scanning
- Container image scanning with Trivy
- Secret management with HashiCorp Vault
- Network policies for service-to-service communication
- Pod Security Policies enforced
- RBAC for Kubernetes access
- SSL/TLS for all external communication

---

## 📋 Development Timeline

### Week 1-2: MVP Phase ✅ Focus Here
- 4 Core services
- PostgreSQL only
- Docker Compose
- Simple GitHub Actions
- Single deployment target

### Week 3-4: Phase 2
- Comments & Feed
- PostgreSQL full-text search
- Redis notifications

### Month 2+: Enterprise Phase
- Elasticsearch
- MongoDB
- Kafka/RabbitMQ
- Kubernetes
- Advanced monitoring

---

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -m 'Add feature'`
3. Push branch: `git push origin feature/your-feature`
4. Open a Pull Request

---

**Project Status:** MVP Phase (Week 1-2)  
**Last Updated:** April 8, 2026  
**Team:** All hands on MVP
