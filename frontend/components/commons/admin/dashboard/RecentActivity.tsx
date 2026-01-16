import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Clock, UserPlus, FileText } from "lucide-react";

interface ActivityDto {
  id: string;
  type: string;
  description: string;
  timestamp: string;
}

interface RecentActivityProps {
  activities: ActivityDto[];
}

export function RecentActivity({ activities }: RecentActivityProps) {
  return (
    <Card className="border-border/50 shadow-lg bg-linear-to-br from-card to-card/80 backdrop-blur-sm">
      <CardHeader>
        <CardTitle className="text-xl font-bold bg-linear-to-br from-primary to-secondary bg-clip-text text-transparent">
          Recent Activity
        </CardTitle>
        <CardDescription>Latest actions on your platform</CardDescription>
      </CardHeader>
      <CardContent>
        {activities.length > 0 ? (
          <div className="space-y-4">
            {activities.map((activity) => {
              const isUserActivity = activity.type === "user_registered";
              const isCVActivity = activity.type === "cv_created";

              return (
                <div
                  key={activity.id}
                  className="flex items-start group hover:bg-linear-to-br hover:from-primary/5 hover:to-secondary/5 p-3 rounded-lg transition-all duration-200"
                >
                  <div className="bg-linear-to-br from-primary/20 to-secondary/20 p-3 rounded-xl mr-3 group-hover:scale-110 group-hover:shadow-lg group-hover:shadow-primary/20 transition-all duration-200">
                    {isUserActivity && (
                      <UserPlus className="h-4 w-4 text-primary" />
                    )}
                    {isCVActivity && (
                      <FileText className="h-4 w-4 text-primary" />
                    )}
                    {!isUserActivity && !isCVActivity && (
                      <Clock className="h-4 w-4 text-primary" />
                    )}
                  </div>
                  <div className="flex-1">
                    <p className="font-semibold text-foreground group-hover:text-primary transition-colors">
                      {activity.description}
                    </p>
                    <p className="text-xs text-muted-foreground font-medium mt-1">
                      {new Date(activity.timestamp).toLocaleString()}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <p className="text-center text-muted-foreground py-8">
            No recent activities
          </p>
        )}
      </CardContent>
    </Card>
  );
}
