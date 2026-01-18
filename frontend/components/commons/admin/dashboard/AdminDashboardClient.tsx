"use client";

import { useEffect } from "react";
import { DashboardHeader } from "@/components/commons/admin/dashboard/DashboardHeader";
import { StatsCard } from "@/components/commons/admin/dashboard/StatsCard";
import { RecentActivity } from "@/components/commons/admin/dashboard/RecentActivity";
import { Button } from "@/components/ui/button";
import { Eye, Users, FileText } from "lucide-react";
import { useStatsStore } from "@/stores/statsStore";

export default function AdminDashboardClient() {
  const { dashboardStats, fetchDashboardStatsInBackground } = useStatsStore();

  useEffect(() => {
    // Fetch in background to update cache
    fetchDashboardStatsInBackground();
  }, []);

  if (!dashboardStats) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <p className="text-muted-foreground mb-4">
            Failed to load dashboard statistics.
          </p>
          <Button onClick={() => window.location.reload()}>Retry</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <DashboardHeader title="Dashboard">
        <Button
          className="gap-2 bg-linear-to-br from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 shadow-lg shadow-blue-500/30 transition-all duration-200 hover:shadow-xl hover:shadow-blue-500/40 hover:scale-105"
          size="sm"
        >
          <Eye className="h-4 w-4" />
          View Report
        </Button>
      </DashboardHeader>

      {/* User & Blog Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatsCard
          title="Total Users"
          value={dashboardStats.totalUsers ?? 0}
          subtitle={`${dashboardStats.usersCreatedThisMonth ?? 0} this month`}
          icon={<Users className="h-4 w-4" />}
          gradient="bg-linear-to-br from-primary to-primary/80 shadow-primary/30 hover:shadow-primary/40"
        />

        <StatsCard
          title="Active Users"
          value={dashboardStats.activeUsers ?? 0}
          subtitle={`${dashboardStats.pendingUsers ?? 0} pending, ${dashboardStats.bannedUsers ?? 0} banned`}
          icon={<Users className="h-4 w-4" />}
          gradient="bg-linear-to-br from-green-500 to-green-600 shadow-green-500/30 hover:shadow-green-500/40"
        />

        <StatsCard
          title="Total Blogs"
          value={dashboardStats.totalBlogs ?? 0}
          subtitle={`${dashboardStats.blogsCreatedThisMonth ?? 0} this month`}
          icon={<FileText className="h-4 w-4" />}
          gradient="bg-linear-to-br from-blue-500 to-blue-600 shadow-blue-500/30 hover:shadow-blue-500/40"
        />

        <StatsCard
          title="Public Blogs"
          value={dashboardStats.publicBlogs ?? 0}
          subtitle={`${dashboardStats.privateBlogs ?? 0} private`}
          icon={<Eye className="h-4 w-4" />}
          gradient="bg-linear-to-br from-purple-500 to-purple-600 shadow-purple-500/30 hover:shadow-purple-500/40"
        />
      </div>

      {/* Recent Activity */}
      <RecentActivity activities={dashboardStats.recentActivities} />
    </div>
  );
}
