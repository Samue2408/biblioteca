import { RequireAuth } from "@/components/auth/RequireAuth";
import { AppSidebar } from "@/components/layout/AppSidebar";

export default function AppLayout({ children }: LayoutProps<"/">) {
  return (
    <RequireAuth>
      <div className="flex min-h-screen flex-1 flex-col md:pl-56">
        <AppSidebar />
        <main className="flex min-w-0 flex-1 flex-col p-6">{children}</main>
      </div>
    </RequireAuth>
  );
}
