import Link from "next/link";
import { FileText } from "lucide-react";

export const NavbarLogo = () => (
  <Link href="/" className="flex items-center gap-2 font-bold text-xl">
    <FileText className="h-6 w-6 text-primary" />
    {/* <img
      src="/images/logo1.png"
      alt="MyBlog Logo"
      className="h-6 w-6 object-contain"
    /> */}
    <span className="bg-linear-to-br from-primary to-secondary bg-clip-text text-transparent">
      MyBlog
    </span>
  </Link>
);
