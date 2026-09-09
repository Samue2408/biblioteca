import { RequireAuth } from "@/components/auth/RequireAuth";
import { AppSidebar } from "@/components/layout/AppSidebar";

export default function AppLayout({ children }: LayoutProps<"/">) {
  return (
    <RequireAuth>
      <div className="flex min-h-full flex-1 flex-col md:flex-row">
        <AppSidebar />
        <div className="flex flex-1 flex-col p-6">{children}</div>
      </div>
    </RequireAuth>
  );
}
