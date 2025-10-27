// app/admin/page.tsx
"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthStatus } from "@/lib/authClient";

export default function AdminPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [allowed, setAllowed] = useState(false);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const data = await getAuthStatus();
        if (!data?.loggedIn) {
          router.replace(`/login?redirect=${encodeURIComponent("/admin")}`);
          return;
        }
        const role = data?.user?.role ?? "user";
        if (role !== "admin") {
          router.replace("/account");
          return;
        }
        if (mounted) setAllowed(true);
      } catch {
        router.replace(`/login?redirect=${encodeURIComponent("/admin")}`);
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => {
      mounted = false;
    };
  }, [router]);

  if (loading) {
    return (
      <main className="mx-auto max-w-3xl w-full px-4 py-8">
        <h1 className="text-2xl font-semibold">Checking access…</h1>
      </main>
    );
  }

  if (!allowed) return null;

  return (
    <main className="mx-auto max-w-5xl w-full px-4 py-8">
      <h1 className="text-3xl font-semibold">Admin Dashboard</h1>
      <p className="mt-2 opacity-80">You’re signed in with admin privileges.</p>

      <div className="mt-8 grid gap-4">
        <div className="rounded-xl border p-4">
          Admin panel placeholder — implement later
        </div>
      </div>
    </main>
  );
}
