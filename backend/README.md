# MyBlog Backend - Spring Boot Microservices

Hệ thống backend MyBlog với kiến trúc microservices hoàn chỉnh, tích hợp AI-powered content generation và hệ thống authentication. Dự án sử dụng **Spring Cloud Gateway** cho API Gateway và **Kubernetes Service Discovery**.

## 🏗️ Kiến trúc tổng thể

```
Gateway (8080)
  └─> Auth (8082)
      └─> User (8083)
  └─> User (8083)
  └─> Blog (8084)
      └─> User (8083)
      └─> AI (8085)
  └─> AI (8085)
      └─> PostgreSQL (5432) [pgvector]
  └─> Mail (8086)
  └─> Stats (8087)
      └─> User (8083)
      └─> Blog (8084)
```

## 🧩 Các thành phần

### 1. **Gateway Service** (Port: 8080)

- API Gateway với Spring Cloud Gateway
- JWT Authentication Filter
- Route requests tới các microservices
- Load balancing và circuit breaker

### 2. **Auth Service** (Port: 8082)

- Xử lý authentication và authorization
- Sinh JWT token
- Validate và refresh tokens
- OAuth2 Social Login (Google, Facebook, GitHub)

### 3. **User Service** (Port: 8083)

- CRUD operations cho User entity
- Kết nối MySQL database
- User profile management
- Role-based access control

### 4. **Blog Service** (Port: 8084)

- **Blog Management**: CRUD operations cho blog posts
- **Comment System**: Quản lý comments và replies
- **File Upload**: Upload thumbnail với Cloudinary
- **AI Integration**: Tích hợp với AI service để generate content
- **Search & Filter**: Tìm kiếm và lọc blog posts

### 5. **AI Service** (Port: 8085)

- **RAG Architecture**: Retrieval-Augmented Generation
- **Vector Search**: PGVector với PostgreSQL cho semantic search
- **Content Generation**: AI-powered title, description, content generation
- **Embedding Service**: Gemini embeddings cho vector similarity
- **Prompt Engineering**: Compact prompt builder cho different content types

### 6. **Mail Service** (Port: 8086)

- Email service cho notifications
- SMTP configuration
- Template-based emails
- Async email sending

### 7. **Stats Service** (Port: 8087)

- Analytics và statistics
- Blog metrics và user engagement
- Dashboard data aggregation

### 8. **Common Modules**

- **Security Common**: Shared security utilities và JWT handling
- **Rabbit Common**: Message broker utilities
- **Redis Common**: Caching và session management
- **Cloudinary Common**: File upload và image processing

### 9. **Infrastructure Services**

- **MySQL Database** (Port: 3306) - User và Blog data
- **PostgreSQL Database** (Port: 5432) - AI service với pgvector extension
- **RabbitMQ** (Port: 5672, Management: 15672) - Message broker
- **Redis** (Port: 6379) - Caching và session storage

## 🔐 Authentication & Security

### JWT Token Authentication

- **RSA Key Pair**: 2048-bit keys cho signing và verification
- **Token Structure**: Header, Payload, Signature
- **Expiration**: Configurable token expiry
- **Refresh Tokens**: Support token refresh mechanism

### OAuth2 Social Login

- **Supported Providers**: Google, Facebook, GitHub
- **Authorization Code Grant**: Secure OAuth2 flow
- **User Mapping**: Auto-create user accounts từ social profiles

## 🤖 AI Features (RAG Architecture)

### Retrieval-Augmented Generation

MyBlog sử dụng RAG pattern để generate content chất lượng cao:

#### **RETRIEVE Phase:**

- Vector search trong knowledge base sử dụng PGVector
- Semantic similarity với Gemini embeddings
- Filter by category, section, rating

#### **AUGMENT Phase:**

- Build context-rich prompts với retrieved examples
- Domain-specific prompt engineering
- Multi-turn conversation support

#### **GENERATE Phase:**

- Gemini AI model cho content generation
- Structured output formatting
- Quality validation và refinement

### AI APIs

- **Title Generation**: Tạo SEO-friendly blog titles
- **Description Generation**: Viết meta descriptions hấp dẫn
- **Content Enhancement**: Mở rộng và cải thiện blog content

## Cách chạy

### Phương pháp chính: Docker Compose (Development)

#### 1. Chuẩn bị Environment Variables

Tạo file `.env` trong từng service directory hoặc root directory.

**Required Environment Variables:**

```bash
# Database
MYSQL_URL=jdbc:mysql://mysql:3306/myblog
MYSQL_USERNAME=root
MYSQL_PASSWORD=password

POSTGRES_URL=jdbc:postgresql://postgres:5432/ai_db
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=password

# Redis
REDIS_URL=redis://redis:6379

# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# JWT Keys (Base64 encoded)
JWT_PUBLIC_KEY=LS0tLS1CRUdJTi...
JWT_PRIVATE_KEY=LS0tLS1CRUdJTi...

# AI Service
GEMINI_API_KEY=your_gemini_api_key

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# OAuth2 (Optional)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

#### 2. Build và Run với Docker Compose

```bash
# Build tất cả services
docker-compose build

# Chạy tất cả services
docker-compose up -d

# Xem logs
docker-compose logs -f

# Dừng services
docker-compose down
```

#### 3. Kiểm tra Services

- **Gateway Health**: http://localhost:8080/actuator/health
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Frontend**: http://localhost:3000

### Phương pháp phụ: Manual Development Setup

#### 1. Build Common Modules

```bash
mvn clean install -pl rabbit-common
mvn clean install -pl security-common
mvn clean install -pl redis-common
mvn clean install -pl gateway-service
mvn clean install -pl user-service
mvn clean install -pl auth-service
mvn clean install -pl mail-service
mvn clean install -pl blog-service
mvn clean install -pl ai-service
mvn clean install -pl stats-service

mvn clean verify -DskipITs=true
```

#### 2. Chạy Infrastructure

```bash
# MySQL
docker run --name mysql -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=myblog -p 3306:3306 -d mysql:8.0

# PostgreSQL với pgvector
docker run --name postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=ai_db -p 5432:5432 -d pgvector/pgvector:pg16

# Redis
docker run --name redis -p 6379:6379 -d redis:alpine

# RabbitMQ
docker run --name rabbitmq -p 5672:5672 -p 15672:15672 -d rabbitmq:management
```

#### 3. Chạy Services (mỗi terminal riêng)

```bash
# Gateway
mvn spring-boot:run -pl gateway-service

# Auth Service
mvn spring-boot:run -pl auth-service

# User Service
mvn spring-boot:run -pl user-service

# Blog Service
mvn spring-boot:run -pl blog-service

# AI Service
mvn spring-boot:run -pl ai-service

# Mail Service
mvn spring-boot:run -pl mail-service

# Stats Service
mvn spring-boot:run -pl stats-service
```

## 📝 API Endpoints

### Authentication (Gateway: /api/v1/auth)

- `POST /login` - User login
- `POST /register` - User registration
- `POST /refresh` - Refresh JWT token
- `GET /validate` - Validate JWT token
- `GET /oauth2/authorize/{provider}` - OAuth2 login

### User Management (Gateway: /api/v1/users)

- `GET /` - Get all users
- `GET /{id}` - Get user by ID
- `POST /` - Create new user
- `PUT /{id}` - Update user
- `DELETE /{id}` - Delete user
- `GET /{id}/profile` - Get user profile

### Blog Management (Gateway: /api/v1/blogs)

- `GET /` - Get all blogs (with pagination)
- `GET /{id}` - Get blog by ID
- `POST /` - Create new blog
- `PUT /{id}` - Update blog
- `DELETE /{id}` - Delete blog
- `GET /{id}/comments` - Get blog comments
- `POST /{id}/comments` - Add comment
- `GET /search` - Search blogs
- `GET /user/{userId}` - Get user's blogs

### AI Content Generation (Gateway: /api/v1/ai)

- `POST /title` - Generate blog title
- `POST /description` - Generate blog description
- `POST /content` - Enhance blog content

**Request Format:**

```json
{
  "title": "Blog Title",
  "description": "Blog Description",
  "content": "Blog Content"
}
```

### Statistics (Gateway: /api/v1/stats)

- `GET /dashboard` - Get dashboard statistics
- `GET /blogs` - Blog statistics
- `GET /users` - User statistics
- `GET /engagement` - User engagement metrics

## 🗄️ Database Schema

### MySQL (User & Blog Data)

```sql
-- Users table
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    avatar_url VARCHAR(500),
    bio TEXT,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Blogs table
CREATE TABLE blogs (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    content LONGTEXT NOT NULL,
    thumbnail_url VARCHAR(500),
    author_id VARCHAR(36) NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id)
);
```

### PostgreSQL (AI Vector Data)

```sql
-- AI knowledge base
CREATE TABLE ai_templates (
    id VARCHAR(36) PRIMARY KEY,
    content TEXT NOT NULL,
    section VARCHAR(50),
    category VARCHAR(50),
    rating INT CHECK (rating >= 1 AND rating <= 5),
    embedding vector(768), -- Gemini embedding dimension
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create vector extension and index
CREATE EXTENSION vector;
CREATE INDEX ON ai_templates USING ivfflat (embedding vector_cosine_ops);
```

## 🔧 Configuration

### Application Properties

Mỗi service có file `application.properties` với cấu hình:

```properties
# Server
server.port=8080
spring.application.name=service-name

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/myblog
spring.datasource.username=root
spring.datasource.password=password

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# JWT
jwt.public-key=${JWT_PUBLIC_KEY}
jwt.private-key=${JWT_PRIVATE_KEY}

# AI Service
spring.ai.gemini.api-key=${GEMINI_API_KEY}
```

## 🧪 Testing

### Unit Tests

```bash
# Run all tests
mvn test

# Run specific service tests
mvn test -pl ai-service

# Run with coverage
mvn verify -DskipITs=true
```

### Integration Tests

```bash
# Run integration tests
mvn verify -DskipUTs=true
```

## 📊 Monitoring & Observability

- **Actuator Endpoints**: `/actuator/health`, `/actuator/metrics`, `/actuator/info`
- **Logs**: Centralized logging với correlation IDs
- **Metrics**: Micrometer metrics cho performance monitoring
- **Tracing**: Distributed tracing support

## 🚀 Deployment

### Docker Images

Mỗi service có Dockerfile riêng:

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Kubernetes Deployment

Dự án hỗ trợ Kubernetes deployment với:

- Service discovery qua DNS
- ConfigMaps cho configuration
- Secrets cho sensitive data
- Horizontal Pod Autoscaling
- Health checks và readiness probes

## 🔒 Security Best Practices

- JWT tokens với RSA signing
- Password hashing với BCrypt
- Input validation và sanitization
- CORS configuration
- Rate limiting
- SQL injection prevention
- XSS protection

## 📈 Performance Optimization

- Redis caching cho frequently accessed data
- Database indexing cho query optimization
- Connection pooling
- Async processing với RabbitMQ
- CDN integration cho static assets

## 🐛 Troubleshooting

### Common Issues

- **Service Communication**: Check gateway routes và service discovery
- **Database Connection**: Verify connection strings và credentials
- **AI Service**: Check Gemini API key và network connectivity
- **File Upload**: Verify Cloudinary credentials
- **JWT Validation**: Ensure RSA key pair consistency

### Debug Commands

```bash
# Check service health
curl http://localhost:8080/actuator/health

# View service logs
docker-compose logs service-name

# Check database connections
docker exec -it mysql mysql -u root -p -e "SHOW PROCESSLIST;"
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot và Spring Cloud ecosystem
- Google Gemini AI
- PostgreSQL với pgvector
- RabbitMQ message broker
- Redis caching
- Cloudinary CDN
