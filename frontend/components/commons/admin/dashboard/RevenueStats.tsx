"use client";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { DollarSign, TrendingUp, TrendingDown, CreditCard } from "lucide-react";
import {
  AreaChart,
  Area,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";

interface RevenueStatsProps {
  revenueStats: IRevenueStats | null;
}

export function RevenueStats({ revenueStats }: RevenueStatsProps) {
  // Always show component, use default values if no data
  const stats = revenueStats || {
    totalRevenue: 0,
    thisMonthRevenue: 0,
    growthRate: 0,
    successfulTransactions: 0,
    pendingTransactions: 0,
    failedTransactions: 0,
    dailyRevenue: [],
    revenueByPaymentMethod: [],
  };

  const isPositiveGrowth = stats.growthRate >= 0;

  // Transform daily revenue data for chart
  const dailyRevenueData =
    stats.dailyRevenue.length > 0
      ? stats.dailyRevenue.map((d) => ({
          date: new Date(d.date).toLocaleDateString("en-US", {
            month: "short",
            day: "numeric",
          }),
          revenue: d.revenue,
          transactions: d.transactions,
        }))
      : [];

  // Transform payment method data for pie chart
  const paymentMethodData = Object.entries(stats.revenueByPaymentMethod).map(
    ([name, value]) => ({
      name,
      value,
    })
  );

  const COLORS = ["#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6"];

  return (
    <div className="space-y-6">
      {/* Revenue Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="bg-linear-to-br from-blue-500 to-blue-600 text-primary-foreground border-0 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-semibold">
              Total Revenue
            </CardTitle>
            <div className="bg-primary-foreground/20 p-2 rounded-lg backdrop-blur-sm">
              <DollarSign className="h-4 w-4" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              ${stats.totalRevenue.toLocaleString()}
            </div>
            <p className="text-xs opacity-90 mt-1">All time revenue</p>
          </CardContent>
        </Card>

        <Card className="bg-linear-to-br from-green-500 to-green-600 text-primary-foreground border-0 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-semibold">This Month</CardTitle>
            <div className="bg-primary-foreground/20 p-2 rounded-lg backdrop-blur-sm">
              {isPositiveGrowth ? (
                <TrendingUp className="h-4 w-4" />
              ) : (
                <TrendingDown className="h-4 w-4" />
              )}
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              ${stats.thisMonthRevenue.toLocaleString()}
            </div>
            <p className="text-xs opacity-90 mt-1 flex items-center gap-1">
              {isPositiveGrowth ? "+" : ""}
              {stats.growthRate.toFixed(1)}% vs last month
            </p>
          </CardContent>
        </Card>

        <Card className="bg-linear-to-br from-purple-500 to-purple-600 text-primary-foreground border-0 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-semibold">Successful</CardTitle>
            <div className="bg-primary-foreground/20 p-2 rounded-lg backdrop-blur-sm">
              <CreditCard className="h-4 w-4" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {stats.successfulTransactions.toLocaleString()}
            </div>
            <p className="text-xs opacity-90 mt-1">Completed transactions</p>
          </CardContent>
        </Card>

        <Card className="bg-linear-to-br from-orange-500 to-orange-600 text-primary-foreground border-0 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-semibold">Pending</CardTitle>
            <div className="bg-primary-foreground/20 p-2 rounded-lg backdrop-blur-sm">
              <CreditCard className="h-4 w-4" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {stats.pendingTransactions.toLocaleString()}
            </div>
            <p className="text-xs opacity-90 mt-1">
              {stats.failedTransactions} failed
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Daily Revenue Chart */}
        <Card>
          <CardHeader>
            <CardTitle>Daily Revenue (Last 30 Days)</CardTitle>
            <CardDescription>Revenue trend over the past month</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={dailyRevenueData}>
                <defs>
                  <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8} />
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip
                  formatter={(value: number) => `$${value.toFixed(2)}`}
                  labelStyle={{ color: "#000" }}
                />
                <Area
                  type="monotone"
                  dataKey="revenue"
                  stroke="#3b82f6"
                  fillOpacity={1}
                  fill="url(#colorRevenue)"
                />
              </AreaChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Payment Methods Chart */}
        <Card>
          <CardHeader>
            <CardTitle>Revenue by Payment Method</CardTitle>
            <CardDescription>Distribution of payment methods</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={paymentMethodData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) =>
                    `${name}: ${(percent * 100).toFixed(0)}%`
                  }
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {paymentMethodData.map((entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={COLORS[index % COLORS.length]}
                    />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(value: number) => `$${value.toFixed(2)}`}
                />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
