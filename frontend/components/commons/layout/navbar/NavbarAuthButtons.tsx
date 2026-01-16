import Link from "next/link";
import { Button } from "@/components/ui/button";

export const NavbarAuthButtons = () => (
  <div className="hidden md:flex items-center gap-2">
    <Link href="/auth/login">
      <Button
        variant="ghost"
        size="sm"
        className="hover:bg-primary/10 hover:text-primary transition-all duration-200"
      >
        Login
      </Button>
    </Link>
    <Link href="/auth/register">
      <Button
        size="sm"
        className="bg-linear-to-br from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 shadow-md shadow-primary/20 hover:shadow-lg hover:shadow-primary/30 transition-all duration-200"
      >
        Sign Up
      </Button>
    </Link>
  </div>
);
