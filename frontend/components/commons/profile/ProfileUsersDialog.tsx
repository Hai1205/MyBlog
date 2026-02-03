"use client";

import { Dialog } from "@headlessui/react";
import { Users } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import Link from "next/link";

interface ProfileUsersDialogProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  users: IUser[];
}

export const ProfileUsersDialog = ({
  isOpen,
  onClose,
  title,
  users,
}: ProfileUsersDialogProps) => {
  const handleUserClick = () => {
    onClose();
  };

  return (
    <Dialog open={isOpen} onClose={onClose} className="relative z-50">
      <div className="fixed inset-0 bg-black/30" aria-hidden="true" />

      <div className="fixed inset-0 flex items-center justify-center p-4">
        <Dialog.Panel className="mx-auto max-w-sm rounded-lg bg-zinc-900 p-6 w-full">
          <Dialog.Title className="text-xl font-bold mb-4">
            {title}
          </Dialog.Title>

          <div className="space-y-4 max-h-96 overflow-y-auto">
            {users.length > 0 ? (
              users.map((user) => (
                <Link
                  key={user.id}
                  className="flex items-center gap-3 hover:bg-zinc-800 p-2 rounded-lg transition-colors"
                  href={`/profile/${user?.id}`}
                  onClick={handleUserClick}
                >
                  <Avatar className="h-10 w-10">
                    <AvatarImage src={user.avatarUrl} />
                    <AvatarFallback>
                      {user.username.substring(0, 2)}
                    </AvatarFallback>
                  </Avatar>

                  <div>
                    <p className="font-medium hover:underline">{user.username}</p>
                    <p className="text-sm text-gray-400 hover:underline">@{user.email}</p>
                  </div>
                </Link>
              ))
            ) : (
              <div className="text-center py-8 text-gray-400">
                <Users className="h-10 w-10 mx-auto mb-2 opacity-50" />
                {title === "Followers"
                  ? "This user has no followers"
                  : "This user has not followed anyone"}
              </div>
            )}
          </div>
        </Dialog.Panel>
      </div>
    </Dialog>
  );
};