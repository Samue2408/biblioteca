import { AdminNavbar } from "@/components/admin/AdminNavbar";
import { RequireRole } from "@/components/auth/RequireRole";

export default function AdminLayout({ children }: LayoutProps<"/admin">) {
  return (    
    <RequireRole role="ADMIN">
      <AdminNavbar/>
      <main className="mx-auto w-full max-w-6xl mt-8">{children}</main>
    </RequireRole>
  );
}
