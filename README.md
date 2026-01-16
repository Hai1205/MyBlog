# MyBlog - AI-Powered CV Builder Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

MyBlog là một nền tảng xây dựng CV thông minh với tích hợp trí tuệ nhân tạo, giúp người dùng tạo và tối ưu hóa CV chuyên nghiệp một cách dễ dàng thông qua giao diện web hiện đại và kiến trúc microservices backend.

## ✨ Tổng quan

MyBlog kết hợp sức mạnh của **AI** và **microservices architecture** để cung cấp giải pháp toàn diện cho việc tạo và cải thiện CV. Hệ thống bao gồm:

- **AI-Powered CV Processing**: Phân tích CV thông minh với OpenRouter API và Llama-3.2-3b-instruct model
- **Job Matching**: So sánh CV với job description để tối ưu hóa cơ hội ứng tuyển
- **Modern Web Interface**: Giao diện người dùng đẹp với Next.js 15, React 18 và TypeScript
- **Secure Authentication**: JWT authentication với OAuth2 social login (Google, Facebook, GitHub)
- **Scalable Backend**: Microservices với Spring Boot 3.2.0 và Kubernetes deployment

## 🏗️ Kiến trúc hệ thống

```
Frontend (Next.js) ─── API Gateway ─── Microservices Backend
       │                      │
       └────────────► Auth Service
                             │
                             ├── User Service (MySQL)
                             ├── CV Service (AI + PostgreSQL)
                             ├── AI Service (Vector DB)
                             ├── Mail Service
                             └── Stats Service
```

**Backend Services**: Gateway, Auth, User, CV, AI, Mail, Stats (7 services)
**Frontend**: Next.js với TypeScript, Tailwind CSS, shadcn/ui, Zustand
**Infrastructure**: MySQL, PostgreSQL (pgvector), RabbitMQ, Redis, Docker, Kubernetes

## Quick Start

### Yêu cầu hệ thống

- Java 21, Node.js 18+, Docker, Maven 3.6+
- OpenRouter API Key (cho AI features)
- Kubernetes cluster (v1.24+) cho production

### 1. Clone & Setup

```bash
git clone https://github.com/Hai1205/MyBlog.git
cd MyBlog
```

### 2. Development với Docker Compose

```bash
cd server && docker-compose up -d
cd ../server/config/keys && javac KeyGenerator.java && java KeyGenerator
mvn clean package -DskipTests
mvn spring-boot:run -pl gateway-service &
mvn spring-boot:run -pl auth-service &
mvn spring-boot:run -pl user-service &
mvn spring-boot:run -pl cv-service &
mvn spring-boot:run -pl ai-service &
mvn spring-boot:run -pl mail-service &
mvn spring-boot:run -pl stats-service &
cd ../client && npm install && npm run dev
```

### 3. Production với Kubernetes

```bash
cd server/k8s
kubectl apply -k overlays/dev/  # hoặc overlays/prod/
kubectl get pods -n MyBlog -w
```

### 4. Truy cập ứng dụng

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080

## Tính năng chính

### AI-Powered Features

- **Smart CV Import**: Upload và tự động phân tích CV từ PDF, DOCX, TXT
- **AI Analyze**: Phân tích CV và đưa ra suggestions cải thiện chi tiết
- **Job Matching**: So sánh CV với job description để tối ưu hóa ứng tuyển
- **Intelligent Suggestions**: AI-generated recommendations cho từng phần của CV
- **Real-time Improvements**: Cải thiện CV theo thời gian thực với AI guidance

### Authentication & Security

- **JWT Authentication**: Bảo mật với RSA 2048-bit keys
- **OAuth2 Integration**: Đăng nhập với Google, Facebook, GitHub
- **Role-based Access Control**: Phân quyền ADMIN và USER với middleware
- **Token Refresh**: Automatic token refresh và secure storage

### 🎨 Modern UI/UX

- **Responsive Design**: Mobile-first với Tailwind CSS và shadcn/ui
- **Real-time Updates**: Cập nhật UI theo thời gian thực với Zustand
- **Dark/Light Mode**: Theme switching với next-themes
- **Intuitive Navigation**: Wizard flow cho CV creation
- **Toast Notifications**: Real-time feedback với react-toastify

## 📁 Cấu trúc Project

```
MyBlog/
├── client/                 # Next.js Frontend Application
│   ├── app/               # Next.js App Router (pages, API routes)
│   ├── components/        # UI components (shadcn/ui + custom)
│   ├── stores/            # Zustand state management
│   ├── hooks/             # Custom React hooks
│   ├── lib/               # Utilities (axios, parsers, validators)
│   └── README.md          # Frontend documentation chi tiết
├── server/                # Spring Boot Backend Services
│   ├── gateway-service/   # API Gateway với Spring Cloud Gateway
│   ├── auth-service/      # JWT authentication & OAuth2
│   ├── user-service/      # User management (MySQL)
│   ├── cv-service/        # AI-powered CV processing
│   ├── ai-service/        # AI features & vector embeddings
│   ├── mail-service/      # Email notifications
│   ├── stats-service/     # Analytics & statistics
│   ├── k8s/              # Kubernetes deployment configs
│   └── README.md         # Backend documentation chi tiết
├── docker-compose.yml    # Development infrastructure
├── LICENSE               # MIT License
└── README.md            # File này
```

## 📚 Tech Stack

### Backend (Spring Boot Microservices)

- **Java 21** + **Spring Boot 3.2.0** + **Spring Cloud 2023.0.0**
- **Spring Security 6+** + **JWT (JJWT 0.12.3)** + **OAuth2**
- **MySQL 8.0** + **PostgreSQL (pgvector)** + **RabbitMQ 3.11**
- **Apache PDFBox 2.0.29** + **Apache POI 5.2.5** (file parsing)
- **OpenRouter API** + **Llama-3.2-3b-instruct** (AI model)
- **Docker** + **Kubernetes v1.24+** (container orchestration)

### Frontend (Next.js Application)

- **Next.js 15.3.3** + **React 18** + **TypeScript 5**
- **Tailwind CSS 4** + **shadcn/ui** (50+ components)
- **Zustand 5** + **Axios 1.10** + **React Hook Form 7**
- **Framer Motion** + **React Toastify** + **Next Themes**
- **Puppeteer** + **Mammoth** + **PDF.js** (file processing)

### Infrastructure & DevOps

- **Docker Compose** (development)
- **Kubernetes** (production deployment)
- **Helm Charts** (infrastructure provisioning)
- **Kustomize** (environment overlays)

## 📖 Documentation

Để biết chi tiết về cách setup, API endpoints, và sử dụng:

- **[📘 Backend Documentation](./server/README.md)** - Hướng dẫn chi tiết về microservices, Kubernetes deployment, API endpoints
- **[🎨 Frontend Documentation](./client/README.md)** - Hướng dẫn setup frontend, components, state management, UI/UX

## Development

### Prerequisites

- Java 21 JDK
- Node.js 18+
- Maven 3.6+
- Docker & Docker Compose
- OpenRouter API Key
- kubectl (cho Kubernetes deployment)

### Environment Setup

```bash
# Backend environment variables
cd server
# Configure .env file (see server/README.md)

# Frontend environment variables
cd client
# Configure .env.local file (see client/README.md)
```

### Running Tests

```bash
# Backend tests (132 test cases)
cd server && mvn test

# Frontend tests
cd client && npm run test
```

## 🚨 Troubleshooting

### Backend Issues

- **Service discovery failed**: Kiểm tra Kubernetes DNS hoặc Docker network
- **JWT validation failed**: Verify RSA key pair generation
- **Database connection**: Check MySQL/PostgreSQL services
- **AI API errors**: Verify OpenRouter API key và network

### Frontend Issues

- **API connection failed**: Check backend services status
- **Authentication failed**: Verify JWT token validity
- **Build errors**: `npm install` hoặc check TypeScript errors

### Infrastructure Issues

- **Pods not starting**: Check Docker images và Kubernetes secrets
- **Service communication**: Verify service names và ports
- **Database migration**: Check pgvector extension cho PostgreSQL

## 🤝 Contributing

Chúng tôi hoan nghênh mọi đóng góp! Vui lòng đọc documentation chi tiết trong từng thư mục trước khi contribute.

1. Fork repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Test thoroughly (backend: `mvn test`, frontend: `npm run test`)
5. Submit Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

- **GitHub Issues**: [Report bugs & request features](https://github.com/Hai1205/MyBlog/issues)
- **Documentation**:
  - [Backend Guide](./server/README.md)
  - [Frontend Guide](./client/README.md)

---

**Made with ❤️ by MyBlog Team**
