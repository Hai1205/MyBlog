import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

interface NavbarUserMenuProps {
  userAuth: IUser;
  isAdmin: boolean;
  onLogout: () => void;
}

export const NavbarUserMenu = ({
  userAuth,
  isAdmin,
  onLogout,
}: NavbarUserMenuProps) => (
  <div className="hidden md:flex items-center gap-3">
    <Link href="/settings">
      <div className="flex items-center gap-3 px-3 py-2 rounded-lg bg-linear-to-br from-primary/5 to-secondary/5 border border-border/50">
        <Avatar className="h-8 w-8 border-2 border-primary/20 shadow-md">
          {userAuth.avatarUrl && (
            <AvatarImage
              src={userAuth.avatarUrl}
              alt={userAuth.fullname || "User"}
              className="object-cover"
            />
          )}
          <AvatarFallback className="bg-linear-to-br from-primary to-secondary text-primary-foreground font-bold text-sm">
            {userAuth.fullname?.charAt(0).toUpperCase() || "U"}
          </AvatarFallback>
        </Avatar>
        <div className="flex flex-col items-start">
          <span className="text-sm font-semibold">{userAuth.fullname}</span>
          {isAdmin && (
            <span className="text-xs text-primary font-medium">Admin</span>
          )}
        </div>
      </div>
    </Link>
    <Button
      variant="outline"
      size="sm"
      onClick={onLogout}
      className="hover:bg-destructive/10 hover:text-destructive hover:border-destructive/50 transition-all duration-200"
    >
      Logout
    </Button>
  </div>
);
