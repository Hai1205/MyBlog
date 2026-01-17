# MyBlog - Modern Blog Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Next.js](https://img.shields.io/badge/Next.js-15.3.3-black)](https://nextjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)](https://spring.io/projects/spring-boot)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue)](https://www.typescriptlang.org/)

A modern, full-stack blog platform built with Next.js 15 and Spring Boot microservices. Features a beautiful, responsive UI, robust authentication, and powerful admin dashboard for content management.

## 🌟 Overview

MyBlog combines modern web technologies to deliver a seamless blogging experience. Built with a microservices architecture on the backend and a responsive Next.js frontend, it provides everything needed to create, manage, and share blog content.

### Key Highlights

- **Modern Frontend**: Next.js 15 with React 18, TypeScript, and Tailwind CSS
- **Microservices Backend**: Spring Boot 3.2.0 with multiple specialized services
- **Secure Authentication**: JWT-based auth with role-based access control
- **Rich Content Management**: Full-featured blog editor with categories and tags
- **Admin Dashboard**: Comprehensive tools for user and content management
- **Responsive Design**: Mobile-first UI that works on all devices
- **Dark Mode Support**: Seamless theme switching
- **Real-time Updates**: Live notifications and status updates

## 🚀 Quick Start

### Prerequisites

- **Node.js** 18 or higher
- **Java** 21 JDK
- **Maven** 3.6+
- **MySQL** 8.0+
- **Docker** (optional, for containerized deployment)

### Clone Repository

```bash
git clone https://github.com/Hai1205/MyBlog.git
cd MyBlog
```

### Backend Setup

1. **Configure MySQL Database**

```bash
# Create database
mysql -u root -p
CREATE DATABASE myblog_db;
CREATE DATABASE myblog_user_db;
CREATE DATABASE myblog_blog_db;
```

2. **Configure Application Properties**

Create `application.properties` in each service's `src/main/resources/`:

```properties
# Example for user-service
spring.datasource.url=jdbc:mysql://localhost:3306/myblog_user_db
spring.datasource.username=root
spring.datasource.password=your_password
```

3. **Generate RSA Keys for JWT**

```bash
cd backend/security-common/src/main/resources
# Generate private key
openssl genrsa -out private_key.pem 2048
# Generate public key
openssl rsa -in private_key.pem -pubout -out public_key.pem
```

4. **Build and Run Backend Services**

```bash
cd backend

# Build all services
mvn clean install

# Run services (in separate terminals)
cd gateway-service && mvn spring-boot:run &
cd auth-service && mvn spring-boot:run &
cd user-service && mvn spring-boot:run &
cd blog-service && mvn spring-boot:run &
cd ai-service && mvn spring-boot:run &
cd mail-service && mvn spring-boot:run &
cd stats-service && mvn spring-boot:run &
```

### Frontend Setup

1. **Install Dependencies**

```bash
cd frontend
npm install
```

2. **Configure Environment**

Create `.env.local`:

```env
NEXT_PUBLIC_SERVER_URL=http://localhost:8080/api/v1
```

3. **Run Development Server**

```bash
npm run dev
```

4. **Access Application**

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080

## 📁 Project Structure

```
MyBlog/
├── frontend/                    # Next.js Frontend Application
│   ├── app/                    # Next.js App Router
│   │   ├── page.tsx           # Home page
│   │   ├── blogs/             # Blog routes
│   │   ├── auth/              # Authentication pages
│   │   ├── admin/             # Admin dashboard
│   │   ├── profile/           # User profiles
│   │   └── settings/          # User settings
│   ├── components/            # React components
│   │   ├── commons/          # Shared components
│   │   └── ui/               # shadcn/ui components
│   ├── stores/               # Zustand state management
│   ├── lib/                  # Utilities and helpers
│   ├── hooks/                # Custom React hooks
│   ├── types/                # TypeScript definitions
│   └── README.md            # Frontend documentation
│
├── backend/                     # Spring Boot Backend Services
│   ├── gateway-service/        # API Gateway (Port 8080)
│   ├── auth-service/           # Authentication (Port 8081)
│   ├── user-service/           # User management (Port 8082)
│   ├── blog-service/           # Blog operations (Port 8083)
│   ├── ai-service/             # AI features (Port 8084)
│   ├── mail-service/           # Email notifications (Port 8085)
│   ├── stats-service/          # Analytics (Port 8086)
│   ├── security-common/        # Shared security utilities
│   ├── redis-common/           # Redis configuration
│   ├── rabbit-common/          # RabbitMQ configuration
│   ├── cloudinary-common/      # Cloudinary integration
│   └── README.md              # Backend documentation
│
├── docker-compose.yml          # Docker services configuration
├── LICENSE                     # MIT License
└── README.md                  # This file
```

## 🛠️ Technology Stack

### Frontend
- **Framework**: Next.js 15.3.3 (React 18, TypeScript 5)
- **Styling**: Tailwind CSS 4, shadcn/ui, Framer Motion
- **State Management**: Zustand 5
- **HTTP Client**: Axios 1.10
- **Form Handling**: React Hook Form 7, Zod validation
- **Icons**: Lucide React
- **Notifications**: React Toastify

### Backend
- **Framework**: Spring Boot 3.2.0, Spring Cloud 2023.0.0
- **Security**: Spring Security 6+, JWT (JJWT 0.12.3)
- **Databases**: MySQL 8.0, PostgreSQL (optional)
- **Messaging**: RabbitMQ 3.11
- **Cache**: Redis 7.0
- **File Storage**: Cloudinary
- **Build Tool**: Maven 3.6+

### DevOps
- **Containerization**: Docker, Docker Compose
- **API Gateway**: Spring Cloud Gateway
- **Service Discovery**: Eureka (optional)

## ✨ Features

### 🎨 User Features

#### Blog Management
- ✅ Create and publish blog posts with rich text editor
- ✅ Edit and delete your own blogs
- ✅ Add categories and tags to blogs
- ✅ Upload and manage blog images
- ✅ Save favorite blogs for later reading
- ✅ View blog statistics and engagement

#### Content Discovery
- ✅ Browse all published blogs
- ✅ Filter blogs by category (Technology, Health, Finance, etc.)
- ✅ Search blogs by title and description
- ✅ Paginated blog listing
- ✅ Responsive card-based layout

#### User Profile
- ✅ Customizable user profiles
- ✅ Upload profile avatar
- ✅ Add bio and social media links
- ✅ View personal blog collection
- ✅ Manage saved blogs

#### Authentication
- ✅ Secure JWT-based authentication
- ✅ User registration with email verification
- ✅ Login with email and password
- ✅ Password reset functionality
- ✅ Remember me option

### 🔐 Admin Features

#### User Management
- ✅ View all registered users
- ✅ Filter users by status (Active, Pending, Banned)
- ✅ Search users by name or email
- ✅ Change user status (Activate, Ban, Delete)
- ✅ View user statistics

#### Blog Dashboard
- ✅ Monitor all published blogs
- ✅ Review and moderate content
- ✅ Delete inappropriate blogs
- ✅ View blog engagement metrics
- ✅ Manage blog categories

#### Analytics
- ✅ View platform statistics
- ✅ Track user registrations
- ✅ Monitor blog publications
- ✅ Analyze user activity

### 🎨 UI/UX Features
- ✅ Dark/Light theme toggle
- ✅ Responsive mobile-first design
- ✅ Smooth page transitions
- ✅ Toast notifications
- ✅ Loading states and skeletons
- ✅ Error handling with user-friendly messages
- ✅ Accessible components (WCAG compliant)

## 📖 API Documentation

### Authentication Endpoints

```bash
POST /api/v1/auth/register       # Register new user
POST /api/v1/auth/login          # User login
POST /api/v1/auth/refresh-token  # Refresh JWT token
POST /api/v1/auth/logout         # User logout
POST /api/v1/auth/forgot-password # Request password reset
POST /api/v1/auth/reset-password # Reset password
```

### Blog Endpoints

```bash
GET    /api/v1/blogs              # Get all blogs (paginated)
GET    /api/v1/blogs/{id}         # Get blog by ID
POST   /api/v1/blogs              # Create new blog (auth required)
PUT    /api/v1/blogs/{id}         # Update blog (auth required)
DELETE /api/v1/blogs/{id}         # Delete blog (auth required)
GET    /api/v1/blogs/user/{userId} # Get user's blogs
```

### User Endpoints

```bash
GET    /api/v1/users/{id}         # Get user profile
PUT    /api/v1/users/{id}         # Update user profile (auth required)
GET    /api/v1/users              # Get all users (admin only)
PUT    /api/v1/users/{id}/status  # Update user status (admin only)
```

For complete API documentation, see [Backend README](backend/README.md).

## 🔒 Security

- **JWT Authentication**: Secure token-based authentication
- **Password Hashing**: BCrypt password encryption
- **CORS Configuration**: Controlled cross-origin requests
- **SQL Injection Protection**: Parameterized queries
- **XSS Prevention**: Input sanitization
- **CSRF Protection**: Token-based CSRF protection
- **Role-Based Access**: USER and ADMIN roles

## 🐳 Docker Deployment

### Development with Docker Compose

```bash
docker-compose up -d
```

This starts:
- MySQL database
- Redis cache
- RabbitMQ message broker
- All backend services
- Frontend application

### Production Deployment

1. **Build Docker Images**

```bash
# Backend
cd backend
docker build -t myblog-backend .

# Frontend
cd frontend
docker build -t myblog-frontend .
```

2. **Run Containers**

```bash
docker run -d -p 8080:8080 myblog-backend
docker run -d -p 3000:3000 myblog-frontend
```

## 🧪 Testing

### Frontend Tests

```bash
cd frontend
npm run test
```

### Backend Tests

```bash
cd backend
mvn test
```

## 🐛 Troubleshooting

### Common Issues

**Database Connection Failed**
- Check MySQL is running: `systemctl status mysql`
- Verify credentials in `application.properties`
- Ensure database exists: `SHOW DATABASES;`

**Port Already in Use**
- Check running processes: `lsof -i :8080`
- Kill process: `kill -9 <PID>`
- Or change port in configuration

**JWT Token Invalid**
- Verify RSA keys are generated correctly
- Check token expiration time
- Ensure keys are in correct location

**Frontend Build Errors**
- Clear cache: `rm -rf .next node_modules`
- Reinstall dependencies: `npm install`
- Check Node.js version: `node -v` (should be 18+)

## 📚 Documentation

- **[Frontend Guide](frontend/README.md)** - Detailed frontend documentation
- **[Backend Guide](backend/README.md)** - Detailed backend documentation
- **[API Reference](docs/API.md)** - Complete API documentation (if available)
- **[Deployment Guide](docs/DEPLOYMENT.md)** - Production deployment guide (if available)

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Coding Standards

- **Frontend**: Follow React/TypeScript best practices, use ESLint
- **Backend**: Follow Java/Spring Boot conventions, use Checkstyle
- **Commits**: Use conventional commit messages
- **Tests**: Add tests for new features

## 📝 Changelog

### Version 1.0.0 (Current)
- ✨ Initial release
- 🎨 Modern UI with Next.js 15 and Tailwind CSS
- 🔐 JWT authentication with role-based access
- 📝 Full blog CRUD operations
- 👥 User management and profiles
- 🛠️ Admin dashboard
- 📊 Basic analytics

## 🗺️ Roadmap

- [ ] Social media authentication (Google, Facebook, GitHub)
- [ ] Rich text editor with markdown support
- [ ] Blog comments and likes
- [ ] Email notifications
- [ ] Advanced search and filters
- [ ] Blog categories and tags
- [ ] SEO optimization
- [ ] Multi-language support
- [ ] Mobile app (React Native)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Your Name** - *Initial work* - [YourGitHub](https://github.com/yourusername)

## 🙏 Acknowledgments

- [Next.js](https://nextjs.org/) - React framework
- [Spring Boot](https://spring.io/projects/spring-boot) - Java framework
- [shadcn/ui](https://ui.shadcn.com/) - UI components
- [Tailwind CSS](https://tailwindcss.com/) - CSS framework
- [Lucide](https://lucide.dev/) - Icon library

## 📞 Support

For support and questions:
- 📧 Email: support@myblog.com
- 🐛 Issues: [GitHub Issues](https://github.com/Hai1205/MyBlog/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/Hai1205/MyBlog/discussions)

---

**Made with ❤️ by MyBlog Team**

⭐ Star us on GitHub if you find this project helpful!
