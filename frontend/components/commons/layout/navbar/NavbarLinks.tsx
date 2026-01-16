import Link from "next/link";
import { cn } from "@/lib/utils";

interface NavbarLinksProps {
  pathname: string;
  links: { href: string; label: string; onClick?: () => void }[];
}

export const NavbarLinks = ({ pathname, links }: NavbarLinksProps) => (
  <div className="hidden md:flex items-center gap-6">
    {links.map((link) => (
      <Link
        key={`${link.href}-${link.label}`}
        href={link.href}
        onClick={(e) => {
          if (link.onClick) {
            e.preventDefault();
            link.onClick();
          }
        }}
        className={cn(
          "text-sm font-medium transition-colors hover:text-primary",
          pathname === link.href ? "text-foreground" : "text-muted-foreground"
        )}
      >
        {link.label}
      </Link>
    ))}
  </div>
);
