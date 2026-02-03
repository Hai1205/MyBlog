"use client";

import { ScrollArea } from "@/components/ui/scroll-area";
import { useEffect, useMemo } from "react";
import { ProfileSkeleton } from "./ProfileSkeleton";
import { ProfileHeader } from "./ProfileHeader";
import { useParams } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { useUserProfileQuery } from "@/hooks/api/queries/useUserQueries";
import { useUserStore } from "@/stores/userStore";
import { ProfileBlogs } from "./ProfileBlogs";

const ProfileClient = () => {
  const { userAuth, handleSetUserAuth } = useAuthStore();
  const { handleSetCurrentUser, currentUser } = useUserStore();

  const { id } = useParams();

  const { data: userResponse, isLoading: userLoading } = useUserProfileQuery(
    id as string,
    {
      enabled: !!id,
    },
  );

  const isMyProfile = useMemo(
    () => userResponse?.data?.user.id === userAuth?.id,
    [userResponse, userAuth],
  );

  useEffect(() => {
    const user = userResponse?.data?.user;
    if (!user) {
      return;
    }

    handleSetCurrentUser(user);

    if(isMyProfile) {
      handleSetUserAuth(user);
    }
  }, [userResponse, handleSetCurrentUser]);

  if (userLoading || !currentUser) {
    return <ProfileSkeleton />;
  }

  return (
    <ScrollArea className="flex-1 h-full">
      <div className="relative pb-20">
        <ProfileHeader
          user={currentUser}
          userAuth={userAuth as IUser}
          userLoading={userLoading}
          isMyProfile={isMyProfile}
        />

        <ProfileBlogs user={currentUser} userAuth={userAuth as IUser} />
      </div>
    </ScrollArea>
  );
};

export default ProfileClient;
