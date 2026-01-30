import AdminLayoutClient from "@/components/commons/admin/AdminLayoutClient";
import { ReactNode } from "react";

export default function AdminLayout({ children }: { children: ReactNode }) {
  return <AdminLayoutClient>{children}</AdminLayoutClient>;
}
