import { useState, type ReactNode } from "react";
import { Header } from "./Header";
import { Sidebar } from "./Sidebar";

export function AppShell({ children }: { children: ReactNode }) {
  const [drawerOpen, setDrawerOpen] = useState(false);

  return (
    <div className="flex min-h-full flex-col">
      <Header onMenuClick={() => setDrawerOpen(true)} />
      <div className="flex flex-1 overflow-hidden">
        <aside className="hidden w-56 shrink-0 border-r border-border bg-background md:block lg:w-64">
          <Sidebar />
        </aside>

        {drawerOpen && (
          <>
            <div
              className="fixed inset-0 z-40 bg-black/40 md:hidden"
              onClick={() => setDrawerOpen(false)}
              aria-hidden
            />
            <aside className="fixed inset-y-0 left-0 z-50 w-64 border-r border-border bg-background md:hidden">
              <div className="flex h-14 items-center border-b border-border px-4 font-semibold">
                Menu
              </div>
              <Sidebar onNavigate={() => setDrawerOpen(false)} />
            </aside>
          </>
        )}

        <main className="flex-1 overflow-y-auto p-4 sm:p-6">{children}</main>
      </div>
    </div>
  );
}
