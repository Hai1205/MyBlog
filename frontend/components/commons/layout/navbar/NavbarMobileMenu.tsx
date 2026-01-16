import Link from "next/link";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { FileText, Menu } from "lucide-react";
import {
  Sheet,
  SheetContent,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

interface NavbarMobileMenuProps {
  pathname: string;
  links: { href: string; label: string; onClick?: () => void }[];
  userAuth: IUser | null;
  isAdmin: boolean;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onLogout: () => void;
}

export const NavbarMobileMenu = ({
  pathname,
  links,
  userAuth,
  isAdmin,
  open,
  onOpenChange,
  onLogout,
}: NavbarMobileMenuProps) => (
  <Sheet open={open} onOpenChange={onOpenChange}>
    <SheetTrigger asChild className="md:hidden">
      <Button variant="ghost" size="icon" className="hover:bg-primary/10">
        <Menu className="h-5 w-5" />
      </Button>
    </SheetTrigger>

    <SheetContent className="w-75 sm:w-100 bg-linear-to-br from-card to-card/80 backdrop-blur-xl border-border/50">
      <SheetTitle className="sr-only">Navigation Menu</SheetTitle>

      <div className="flex items-center gap-2 mb-8 pb-6 border-b border-border/50">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-linear-to-br from-primary to-secondary shadow-lg">
          <FileText className="h-5 w-5 text-primary-foreground" />
        </div>
        <span className="text-xl font-bold bg-linear-to-br from-primary to-secondary bg-clip-text text-transparent">
          MyBlog
        </span>
      </div>

      {userAuth && (
        <div className="mb-6 p-4 rounded-xl bg-linear-to-br from-primary/10 to-secondary/10 border border-border/50">
          <Link href="/settings" onClick={() => onOpenChange(false)}>
            <div className="flex items-center gap-3">
              <Avatar className="h-12 w-12 border-2 border-primary/20 shadow-lg">
                {userAuth.avatarUrl && (
                  <AvatarImage
                    src={userAuth.avatarUrl}
                    alt={userAuth.fullname || "User"}
                    className="object-cover"
                  />
                )}
                <AvatarFallback className="bg-linear-to-br from-primary to-secondary text-primary-foreground font-bold text-lg">
                  {userAuth.fullname?.charAt(0).toUpperCase() || "U"}
                </AvatarFallback>
              </Avatar>
              <div className="flex-1">
                <p className="text-sm font-semibold text-foreground">
                  {userAuth.fullname}
                </p>
                <p className="text-xs text-muted-foreground line-clamp-1">
                  {userAuth.email}
                </p>
                {isAdmin && (
                  <div className="inline-flex items-center gap-1 mt-1 px-2 py-0.5 rounded-md bg-primary/20 text-primary text-xs font-medium">
                    Admin
                  </div>
                )}
              </div>
            </div>
          </Link>
        </div>
      )}

      <div className="flex flex-col gap-2 mb-6">
        {links.map((link) => (
          <Link
            key={`${link.href}-${link.label}`}
            href={link.href}
            onClick={(e) => {
              if (link.onClick) {
                e.preventDefault();
                link.onClick();
              }
              onOpenChange(false);
            }}
            className={cn(
              "flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-all duration-200",
              pathname === link.href
                ? "bg-linear-to-br from-primary to-secondary text-primary-foreground shadow-lg shadow-primary/30"
                : "text-muted-foreground hover:text-foreground hover:bg-accent/50"
            )}
          >
            {pathname === link.href && (
              <div className="h-2 w-2 rounded-full bg-primary-foreground animate-pulse" />
            )}
            {link.label}
          </Link>
        ))}
      </div>

      {userAuth ? (
        <div className="mt-auto pt-6 border-t border-border/50">
          <Button
            variant="outline"
            onClick={onLogout}
            className="w-full bg-linear-to-br from-destructive/10 to-destructive/5 hover:from-destructive/20 hover:to-destructive/10 text-destructive hover:text-destructive border-destructive/30 hover:border-destructive/50 transition-all duration-200"
          >
            Đăng xuất
          </Button>
        </div>
      ) : (
        <div className="mt-auto pt-6 border-t border-border/50 flex flex-col gap-3">
          <Link href="/auth/login" onClick={() => onOpenChange(false)}>
            <Button
              variant="outline"
              className="w-full hover:bg-accent/50 transition-all duration-200"
            >
              Đăng nhập
            </Button>
          </Link>
          <Link href="/auth/register" onClick={() => onOpenChange(false)}>
            <Button className="w-full bg-linear-to-br from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 shadow-lg shadow-primary/30 hover:shadow-xl hover:shadow-primary/40 transition-all duration-200">
              Đăng ký
            </Button>
          </Link>
        </div>
      )}
    </SheetContent>
  </Sheet>
);
