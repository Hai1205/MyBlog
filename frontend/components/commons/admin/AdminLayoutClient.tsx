"use client";

import { useState, useEffect, useRef } from "react";
import AdminSidebar from "./AdminSidebar";

interface AdminLayoutClientProps {
  children: React.ReactNode;
}

export default function AdminLayoutClient({
  children,
}: AdminLayoutClientProps) {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [sidebarWidth, setSidebarWidth] = useState(280);
  const [mounted, setMounted] = useState(false);
  const isResizing = useRef(false);

  // Load sidebar state from localStorage on mount
  useEffect(() => {
    setMounted(true);
    const savedCollapsed = localStorage.getItem("adminSidebarCollapsed");
    const savedWidth = localStorage.getItem("adminSidebarWidth");

    if (savedCollapsed !== null) {
      setSidebarCollapsed(JSON.parse(savedCollapsed));
    }

    if (savedWidth !== null) {
      setSidebarWidth(Math.max(80, Math.min(400, parseInt(savedWidth))));
    }
  }, []);

  // Save sidebar state to localStorage when it changes
  useEffect(() => {
    localStorage.setItem(
      "adminSidebarCollapsed",
      JSON.stringify(sidebarCollapsed),
    );
  }, [sidebarCollapsed]);

  useEffect(() => {
    if (sidebarWidth) {
      localStorage.setItem("adminSidebarWidth", sidebarWidth.toString());
    }
  }, [sidebarWidth]);

  const startResizing = (e: React.MouseEvent) => {
    e.preventDefault();
    isResizing.current = true;
  };

  const stopResizing = () => {
    isResizing.current = false;
  };

  const resize = (e: MouseEvent) => {
    if (isResizing.current) {
      const newWidth = Math.max(80, Math.min(320, e.clientX));
      setSidebarWidth(newWidth);
      setSidebarCollapsed(newWidth < 120);
    }
  };

  useEffect(() => {
    window.addEventListener("mousemove", resize);
    window.addEventListener("mouseup", stopResizing);
    return () => {
      window.removeEventListener("mousemove", resize);
      window.removeEventListener("mouseup", stopResizing);
    };
  }, []);

  return (
    <div className="bg-card min-h-screen">
      <div className="flex relative">
        {mounted && (
          <div className="sticky top-0 h-screen">
            <AdminSidebar
              collapsed={sidebarCollapsed}
              width={sidebarWidth}
              onToggle={() => setSidebarCollapsed(!sidebarCollapsed)}
              onStartResizing={startResizing}
            />
          </div>
        )}
        <div
          className="flex-1 transition-all duration-300"
          style={{
            marginLeft: mounted ? "8px" : "0px",
          }}
        >
          <main className="p-4 md:p-6 bg-card">{children}</main>
        </div>
      </div>
    </div>
  );
}
