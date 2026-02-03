"use client";

import {
  UserPlus,
  Loader2,
  UserCheck,
  Instagram,
  Facebook,
  Linkedin,
  Settings,
} from "lucide-react";

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import { useState, useEffect, useMemo } from "react";
import { toast } from "react-toastify";
import { formatNumberStyle } from "@/lib/utils";
import { ProfileUsersDialog } from "./ProfileUsersDialog";
import {
  useFollowUserMutation,
  useUnfollowUserMutation,
} from "@/hooks/api/mutations/useUserMutations";

interface ProfileHeaderProps {
  user: IUser;
  userAuth: IUser;
  userLoading: boolean;
  isMyProfile: boolean;
}

export const ProfileHeader = ({
  user,
  userAuth,
  userLoading,
  isMyProfile,
}: ProfileHeaderProps) => {
  const [showFollowersDialog, setShowFollowersDialog] = useState(false);
  const [showFollowingDialog, setShowFollowingDialog] = useState(false);
  const [followed, setFollowed] = useState(false);

  const { mutateAsync: followUserMutateAsync } = useFollowUserMutation();
  const { mutateAsync: unfollowUserMutateAsync } = useUnfollowUserMutation();

  const followers = useMemo(
    () => (user?.followers || []) as IUser[],
    [user?.followers],
  );
  const followings = useMemo(
    () => (user?.followings || []) as IUser[],
    [user?.followings],
  );

  const [amIFollowing, setAmIFollowing] = useState<boolean>(false);
  const [followersCount, setFollowersCount] = useState(
    user?.followers?.length || 0,
  );

  useEffect(() => {
    if (!user || !userAuth) return;

    const amIFollowed = userAuth
      ? followers.some((follower: IUser) => follower.id === userAuth.id)
      : false;

    setAmIFollowing(amIFollowed);
    setFollowersCount(followers.length);
  }, [user, followers, userAuth]);

  const follow = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();

    if (!user.id || !userAuth?.id) {
      toast.error("User must be logged in to follow");
      return;
    }

    if (!followed) {
      setFollowed(true);
      followUserMutateAsync({ followerId: userAuth.id, followingId: user.id });
    } else {
      setFollowed(false);
      unfollowUserMutateAsync({
        followerId: userAuth.id,
        followingId: user.id,
      });
    }
  };

  return (
    <>
      <div className="bg-linear-to-b from-primary to-black p-6 relative">
        {isMyProfile && (
          <Link
            href="/settings"
            className="absolute top-4 right-4 bg-zinc-800 hover:bg-zinc-700 p-2 rounded-full transition-colors"
          >
            <Settings className="h-5 w-5" />
          </Link>
        )}

        <div className="flex flex-col md:flex-row items-center gap-6">
          <Avatar className="w-32 h-32 md:w-40 md:h-40 rounded-full object-cover border-4 border-white/10">
            <AvatarImage src={user.avatarUrl} alt={user.username} />

            <AvatarFallback className="text-8xl font-bold">
              {user.username.substring(0, 2)}
            </AvatarFallback>
          </Avatar>

          <div className="flex-1 text-center md:text-left">
            <h1 className="text-3xl font-bold">{user.username}</h1>

            <p className="text-gray-400 mb-2">@{user.email}</p>

            {user.summary && (
              <p className="text-gray-300 mb-4 max-w-2xl">{user.summary}</p>
            )}

            <div className="flex justify-center md:justify-start gap-4 mb-4">
              <div
                className="cursor-pointer hover:opacity-80"
                onClick={() => setShowFollowersDialog(true)}
              >
                <span className="font-bold">
                  {formatNumberStyle(followersCount)}
                </span>
                <span className="text-gray-400 ml-1 hover:underline">
                  Followers
                </span>
              </div>

              <div
                className="cursor-pointer hover:opacity-80"
                onClick={() => setShowFollowingDialog(true)}
              >
                <span className="font-bold">
                  {formatNumberStyle(user?.followings?.length || 0)}
                </span>
                <span className="text-gray-400 ml-1 hover:underline">
                  Following
                </span>
              </div>
            </div>

            {/* Social Media Links */}
            <div className="flex gap-2 justify-center md:justify-start mb-4">
              {user.instagram && (
                <a
                  href={user.instagram}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="bg-zinc-800 hover:bg-red-600 p-2 rounded-full transition-colors"
                >
                  <Instagram className="h-4 w-4" />
                </a>
              )}

              {user.linkedin && (
                <a
                  href={user.linkedin}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="bg-zinc-800 hover:bg-red-600 p-2 rounded-full transition-colors"
                >
                  <Linkedin className="h-4 w-4" />
                </a>
              )}

              {user.facebook && (
                <a
                  href={user.facebook}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="bg-zinc-800 hover:bg-red-600 p-2 rounded-full transition-colors"
                >
                  <Facebook className="h-4 w-4" />
                </a>
              )}
            </div>

            {!isMyProfile && (
              <div className="flex gap-3 justify-center md:justify-start">
                <Button
                  onClick={follow}
                  className="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-full flex items-center gap-2 transition-colors"
                  disabled={userLoading}
                >
                  {userLoading ? (
                    <>
                      <Loader2 size={18} className="animate-spin" />
                      <span>Loading...</span>
                    </>
                  ) : amIFollowing ? (
                    <>
                      <UserCheck size={18} />
                      <span>Following</span>
                    </>
                  ) : (
                    <>
                      <UserPlus size={18} />
                      <span>Follow</span>
                    </>
                  )}
                </Button>

                {/* {amIFollowing && (
                  <Link
                    to={`/chat?userId=${user.id}`}
                    className="bg-transparent border border-gray-400 hover:border-white text-white px-4 py-2 rounded-full flex items-center gap-2 transition-colors"
                  >
                    <MessageSquare size={18} />
                  </Link>
                )} */}
              </div>
            )}
          </div>
        </div>
      </div>

      <ProfileUsersDialog
        isOpen={showFollowersDialog}
        onClose={() => setShowFollowersDialog(false)}
        title="Followers"
        users={followers}
      />

      <ProfileUsersDialog
        isOpen={showFollowingDialog}
        onClose={() => setShowFollowingDialog(false)}
        title="Following"
        users={followings}
      />
    </>
  );
};
