"use client";

import { Home, Users, FileText, Menu, X, LogOut } from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { cn } from "@/lib/utils";
import { useLogoutMutation } from "@/hooks/api/mutations/useAuthMutations";

interface AdminSidebarProps {
  collapsed: boolean;
  width: number;
  onToggle: () => void;
  onStartResizing: (e: React.MouseEvent) => void;
}

export const AdminSidebar = ({
  collapsed,
  width,
  onToggle,
  onStartResizing,
}: AdminSidebarProps) => {
  const { mutate: logout } = useLogoutMutation();

  const pathname = usePathname();
  const router = useRouter();

  const handleLogout = async () => {
    logout(undefined, {
      onSuccess: () => {
        router.push("/auth/login");
      },
    });
  };

  const menuItems = [
    { icon: Home, label: "Dashboard", href: "/admin" },
    { icon: FileText, label: "Blog Dashboard", href: "/admin/blog-dashboard" },
    { icon: Users, label: "User Dashboard", href: "/admin/user-dashboard" },
  ];

  return (
    <aside
      className={cn(
        "bg-linear-to-b from-card via-card to-muted/30 dark:from-card dark:via-card dark:to-card/80 border border-border/50 flex flex-col transition-all duration-300 ease-in-out z-30 rounded-tr-2xl rounded-br-2xl backdrop-blur-sm",
        collapsed
          ? "shadow-lg shadow-primary/5"
          : "shadow-xl shadow-primary/10",
      )}
      style={{ width: collapsed ? 80 : width, height: "100%" }}
    >
      {/* Sidebar Header */}
      <div className="flex items-center justify-between p-4 border-b border-border/50 bg-linear-to-br from-primary/5 to-secondary/5 dark:from-primary/10 dark:to-secondary/10">
        {!collapsed && (
          <div className="flex items-center space-x-3">
            <div className="bg-linear-to-br from-primary to-secondary w-10 h-10 rounded-xl flex items-center justify-center shadow-lg shadow-primary/20">
              <Home className="h-5 w-5 text-primary-foreground" />
            </div>
            <div>
              <span className="text-xl font-bold bg-linear-to-br from-primary to-secondary bg-clip-text text-transparent">
                Admin
              </span>
              <p className="text-[10px] text-muted-foreground font-medium">
                Dashboard
              </p>
            </div>
          </div>
        )}
        <button
          onClick={onToggle}
          className="ml-auto p-2 rounded-lg hover:bg-primary/10 dark:hover:bg-primary/20 transition-colors duration-200 group"
        >
          {collapsed ? (
            <Menu className="h-5 w-5 text-foreground group-hover:text-primary transition-colors" />
          ) : (
            <X className="h-5 w-5 text-foreground group-hover:text-primary transition-colors" />
          )}
        </button>
      </div>

      {/* Sidebar Menu */}
      <nav className="flex-1 overflow-y-auto py-6 scrollbar-thin scrollbar-thumb-primary/20 scrollbar-track-transparent">
        <ul className="space-y-2 px-3">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = pathname === item.href;

            return (
              <li key={item.href}>
                <Link
                  href={item.href}
                  className={cn(
                    "flex items-center rounded-xl px-4 py-3 text-sm font-semibold transition-all duration-200 group relative overflow-hidden",
                    isActive
                      ? "bg-linear-to-br from-primary to-secondary text-primary-foreground shadow-lg shadow-primary/30"
                      : "text-foreground/70 hover:text-foreground hover:bg-accent/50 dark:hover:bg-accent/30",
                  )}
                >
                  {isActive && (
                    <div className="absolute inset-0 bg-linear-to-br from-primary/10 to-secondary/10 animate-pulse" />
                  )}
                  <Icon
                    className={cn(
                      "h-5 w-5 relative z-10 transition-transform duration-200",
                      isActive ? "scale-110" : "group-hover:scale-110",
                    )}
                  />
                  {!collapsed && (
                    <span className="ml-3 relative z-10">{item.label}</span>
                  )}
                  {isActive && !collapsed && (
                    <div className="ml-auto h-2 w-2 rounded-full bg-primary-foreground animate-ping" />
                  )}
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>

      {/* Logout Button */}
      <div className="mt-auto px-3 pb-4 border-t border-border/50 pt-4">
        <button
          onClick={handleLogout}
          className={cn(
            "flex w-full items-center rounded-xl px-4 py-3 text-sm font-semibold transition-all duration-200 group",
            "text-destructive hover:bg-destructive/10 dark:hover:bg-destructive/20 hover:shadow-lg hover:shadow-destructive/20",
          )}
        >
          <LogOut className="h-5 w-5 transition-transform duration-200 group-hover:scale-110 group-hover:-translate-x-1" />
          {!collapsed && <span className="ml-3">Đăng xuất</span>}
        </button>
      </div>

      {/* Resize Handle */}
      <div
        className="absolute top-0 right-0 h-full w-1 cursor-col-resize hover:bg-linear-to-b hover:from-primary hover:to-secondary transition-all duration-200 hover:w-1.5"
        onMouseDown={onStartResizing}
      />
    </aside>
  );
};
