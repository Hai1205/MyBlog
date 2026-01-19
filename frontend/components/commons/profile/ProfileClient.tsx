"use client";

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Facebook, Instagram, Linkedin } from "lucide-react";
import { useParams } from "next/navigation";
import { useUserQuery } from "@/hooks/api/queries/useUserQueries";
import { ProfileSkeleton } from "./ProfileSkeleton";

const ProfileClient = () => {
  const { id } = useParams();

  const { data: userResponse, isLoading } = useUserQuery(id as string);
  const user = userResponse?.data?.user;

  if (isLoading) {
    return <ProfileSkeleton />;
  }

  if (!user) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        User not found
      </div>
    );
  }
  return (
    <div className="flex justify-center items-center min-h-screen p-4">
      <Card className="w-full max-w-xl shadow-lg border rounded-2xl p-6">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-semibold">Profile</CardTitle>

          <CardContent className="flex flex-col items-center space-y-4">
            <Avatar className="w-28 h-28 border-4 border-primary/20 shadow-md">
              {user?.avatarUrl && (
                <AvatarImage
                  src={user?.avatarUrl}
                  alt={user?.username || "User"}
                  className="object-cover"
                />
              )}
              <AvatarFallback className="bg-linear-to-br from-primary to-secondary text-primary-foreground font-bold text-sm">
                {user?.username?.charAt(0).toUpperCase() || "U"}
              </AvatarFallback>
            </Avatar>

            <div className="w-full space-y-2 text-center">
              <p>{user?.username}</p>
            </div>

            {user?.summary && (
              <div className="w-full space-y-2 text-center">
                <label className="font-medium">Summary</label>
                <p>{user.summary}</p>
              </div>
            )}

            <div className="flex gap-4 mt-3">
              {user?.instagram && (
                <a
                  href={user.instagram}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <Instagram className="text-pink-500 text-2xl" />
                </a>
              )}

              {user?.facebook && (
                <a
                  href={user.facebook}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <Facebook className="text-blue-500 text-2xl" />
                </a>
              )}

              {user?.linkedin && (
                <a
                  href={user.linkedin}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <Linkedin className="text-blue-700 text-2xl" />
                </a>
              )}
            </div>
          </CardContent>
        </CardHeader>
      </Card>
    </div>
  );
};

export default ProfileClient;
