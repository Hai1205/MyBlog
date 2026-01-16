import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Users, UserPlus } from "lucide-react";

interface StatsCardProps {
  title: string;
  value: number;
  subtitle: string;
  icon: React.ReactNode;
  gradient: string;
}

export function StatsCard({
  title,
  value,
  subtitle,
  icon,
  gradient,
}: StatsCardProps) {
  return (
    <Card
      className={`${gradient} text-primary-foreground border-0 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105`}
    >
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-semibold">{title}</CardTitle>
        <div className="bg-primary-foreground/20 p-2 rounded-lg backdrop-blur-sm">
          {icon}
        </div>
      </CardHeader>
      <CardContent>
        <div className="text-3xl font-bold">{value.toLocaleString()}</div>
        <p className="text-xs opacity-90 mt-1">{subtitle}</p>
      </CardContent>
    </Card>
  );
}
