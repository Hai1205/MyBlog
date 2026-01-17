# MyBlog Frontend

Modern blog platform frontend built with Next.js 15, React 18, and TypeScript, featuring a beautiful UI with Tailwind CSS and shadcn/ui components.

## ✨ Features

### 🎨 User Interface

- **Modern Design**: Clean and responsive UI with Tailwind CSS
- **shadcn/ui Components**: 50+ pre-built, accessible components
- **Dark/Light Mode**: Seamless theme switching with next-themes
- **Responsive Layout**: Mobile-first design that works on all devices
- **Smooth Animations**: Powered by Framer Motion
- **Toast Notifications**: Real-time feedback with React Toastify

### 📝 Blog Management

- **Create & Edit Blogs**: Rich text editor for creating engaging content
- **Category Filtering**: Filter blogs by technology, health, finance, travel, education, entertainment, and study
- **Search Functionality**: Search blogs by title and description
- **Pagination**: Efficient browsing with customizable page sizes
- **Blog Preview**: Real-time preview while editing
- **Save Blogs**: Bookmark favorite blogs for later reading

### 👤 User Features

- **Authentication**: Secure JWT-based authentication
- **User Profiles**: Customizable profiles with avatar, bio, and social links
- **My Blogs**: Manage your published blogs
- **Saved Blogs**: Access your bookmarked content
- **Settings**: Update profile information and preferences

### 🔐 Admin Dashboard

- **User Management**: View and manage user accounts
- **Blog Dashboard**: Monitor all published blogs
- **Statistics**: View platform analytics and metrics
- **User Status Control**: Activate, suspend, or ban users

## 🏗️ Tech Stack

### Core

- **Next.js 15.3.3** - React framework with App Router
- **React 18** - UI library
- **TypeScript 5** - Type-safe JavaScript
- **Tailwind CSS 4** - Utility-first CSS framework

### UI Components

- **shadcn/ui** - Component library based on Radix UI
- **Lucide Icons** - Beautiful icon set
- **Framer Motion** - Animation library
- **React Toastify** - Toast notifications
- **Headless UI** - Unstyled, accessible components

### State Management & Data Fetching

- **Zustand 5** - Lightweight state management
- **Axios 1.10** - HTTP client with interceptors
- **React Hook Form 7** - Form handling with validation
- **Zod** - Schema validation

### Utilities

- **clsx** - Conditional className utility
- **date-fns** - Date utility library
- **class-variance-authority** - Component variant styling

## 📁 Project Structure

```
frontend/
├── app/                          # Next.js App Router
│   ├── (routes)/                # Route groups
│   │   ├── page.tsx            # Home page
│   │   ├── blogs/              # Blog routes
│   │   │   ├── page.tsx        # Blog list
│   │   │   ├── [id]/          # Blog detail
│   │   │   ├── new/           # Create blog
│   │   │   ├── edit/[id]/     # Edit blog
│   │   │   ├── my-blogs/      # User's blogs
│   │   │   └── saved/         # Saved blogs
│   │   ├── auth/              # Authentication routes
│   │   │   ├── login/
│   │   │   ├── register/
│   │   │   ├── forgot-password/
│   │   │   ├── reset-password/
│   │   │   ├── verification/
│   │   │   └── banned/
│   │   ├── profile/[id]/      # User profile
│   │   ├── settings/          # User settings
│   │   └── admin/             # Admin dashboard
│   │       ├── page.tsx       # Admin home
│   │       ├── user-dashboard/
│   │       └── blog-dashboard/
│   ├── privacy-policy/         # Privacy policy page
│   ├── terms-of-service/       # Terms of service page
│   ├── layout.tsx             # Root layout
│   ├── globals.css            # Global styles
│   └── not-found.tsx          # 404 page
│
├── components/                 # React components
│   ├── commons/               # Shared components
│   │   ├── blogs/            # Blog components
│   │   │   ├── BlogCard.tsx
│   │   │   ├── BlogsClient.tsx
│   │   │   └── Loading.tsx
│   │   └── layout/           # Layout components
│   │       ├── Header.tsx
│   │       ├── Footer.tsx
│   │       ├── Sidebar.tsx
│   │       └── pagination/
│   └── ui/                    # shadcn/ui components
│       ├── button.tsx
│       ├── card.tsx
│       ├── dialog.tsx
│       ├── input.tsx
│       └── ...
│
├── stores/                     # Zustand state stores
│   ├── authStore.ts           # Authentication state
│   ├── blogStore.ts           # Blog state
│   ├── userStore.ts           # User state
│   └── statsStore.ts          # Statistics state
│
├── services/                   # API services
│   ├── mockData.ts            # Mock data for development
│   └── constants.ts           # API constants
│
├── lib/                        # Utilities
│   ├── axiosInstance.ts       # Axios configuration
│   ├── initialStore.ts        # Store initialization
│   └── utils.ts              # Utility functions
│
├── hooks/                      # Custom React hooks
│   ├── use-mobile.ts          # Mobile detection hook
│   ├── use-pagination.ts      # Pagination hook
│   └── use-toast.ts           # Toast notification hook
│
├── types/                      # TypeScript types
│   ├── interface.ts           # Interface definitions
│   └── enum.ts               # Enum definitions
│
├── styles/                     # Additional styles
│   └── globals.css
│
├── middleware.ts              # Next.js middleware for auth
├── next.config.mjs            # Next.js configuration
├── tailwind.config.ts         # Tailwind CSS configuration
├── components.json            # shadcn/ui configuration
├── tsconfig.json             # TypeScript configuration
└── package.json              # Dependencies

```

## 🚀 Getting Started

### Prerequisites

- Node.js 18 or higher
- npm or yarn
- Backend server running (default: http://localhost:8080)

### Installation

1. **Clone the repository**

```bash
git clone https://github.com/yourusername/MyBlog.git
cd MyBlog/frontend
```

2. **Install dependencies**

```bash
npm install
# or
yarn install
```

3. **Configure environment variables**

Create a `.env.local` file in the root directory:

```env
# API Configuration
NEXT_PUBLIC_SERVER_URL=http://localhost:8080/api/v1

# Optional: Analytics
NEXT_PUBLIC_ANALYTICS_ID=your-analytics-id
```

4. **Run the development server**

```bash
npm run dev
# or
yarn dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Build for Production

```bash
npm run build
npm start
```

## 🎯 Available Routes

### Public Routes

- `/` - Home page with featured blogs
- `/blogs` - Browse all blogs with filters and search
- `/blogs/[id]` - View blog details
- `/auth/login` - User login
- `/auth/register` - User registration
- `/privacy-policy` - Privacy policy
- `/terms-of-service` - Terms of service

### Protected Routes (Requires Authentication)

- `/blogs/new` - Create new blog
- `/blogs/edit/[id]` - Edit blog
- `/blogs/my-blogs` - User's published blogs
- `/blogs/saved` - User's saved blogs
- `/profile/[id]` - User profile
- `/settings` - User settings

### Admin Routes (Requires Admin Role)

- `/admin` - Admin dashboard
- `/admin/user-dashboard` - Manage users
- `/admin/blog-dashboard` - Manage blogs

## 🔧 Configuration

### Tailwind CSS

Customize theme in `tailwind.config.ts`:

```typescript
export default {
  theme: {
    extend: {
      colors: {
        // Add custom colors
      },
    },
  },
};
```

### shadcn/ui Components

Add new components:

```bash
npx shadcn@latest add button
npx shadcn@latest add card
```

### State Management

Zustand stores are located in `/stores`:

```typescript
// Example: authStore.ts
import { create } from "zustand";

interface AuthState {
  user: IUser | null;
  setUser: (user: IUser) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  setUser: (user) => set({ user }),
}));
```

## 📝 Development Guidelines

### Component Structure

```typescript
// components/MyComponent.tsx
interface MyComponentProps {
  title: string;
  description?: string;
}

export function MyComponent({ title, description }: MyComponentProps) {
  return (
    <div>
      <h1>{title}</h1>
      {description && <p>{description}</p>}
    </div>
  );
}
```

### API Calls

Use Axios instance from `/lib/axiosInstance.ts`:

```typescript
import axiosInstance from "@/lib/axiosInstance";

const fetchBlogs = async () => {
  const response = await axiosInstance.get("/blogs");
  return response.data;
};
```

### Form Handling

Use React Hook Form with Zod validation:

```typescript
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";

const schema = z.object({
  title: z.string().min(1, "Title is required"),
});

const { register, handleSubmit } = useForm({
  resolver: zodResolver(schema),
});
```

## 🧪 Testing

```bash
npm run test
```

## 📦 Building

### Development Build

```bash
npm run build
```

### Production Build with Docker

```bash
docker build -t myblog-frontend .
docker run -p 3000:3000 myblog-frontend
```

## 🐛 Troubleshooting

### Common Issues

**Port Already in Use**

```bash
# Kill process on port 3000
npx kill-port 3000
```

**Module Not Found**

```bash
# Clear cache and reinstall
rm -rf node_modules .next
npm install
```

**Type Errors**

```bash
# Regenerate TypeScript types
npm run build
```

## 📚 Learn More

- [Next.js Documentation](https://nextjs.org/docs)
- [React Documentation](https://react.dev)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [shadcn/ui Documentation](https://ui.shadcn.com)
- [Zustand Documentation](https://zustand-demo.pmnd.rs)

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.

## 💬 Support

For issues and questions:

- Open an issue on [GitHub](https://github.com/yourusername/MyBlog/issues)
- Check existing documentation in `/docs`

---

**Made with ❤️ by MyBlog Team**
