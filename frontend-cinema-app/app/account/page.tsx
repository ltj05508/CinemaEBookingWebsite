// app/account/page.tsx
"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthStatus } from "@/lib/authClient";
import Link from "next/link";

export default function AccountPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState<any | null>(null);

 
  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const data = await getAuthStatus();
        if (!data?.loggedIn) {
          router.replace(`/login?redirect=${encodeURIComponent("/account")}`);
          return;
        }
        if (mounted) setUser(data.user ?? null);
      } catch {
        router.replace(`/login?redirect=${encodeURIComponent("/account")}`);
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
        <h1 className="text-2xl font-semibold">Loading…</h1>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-3xl w-full px-4 py-8">
      <h1 className="text-3xl font-semibold">
        Welcome{user?.firstName ? `, ${user.firstName}` : ""}!
      </h1>
      <p className="mt-2 opacity-80">
        You’re logged in{user?.email ? ` as ${user.email}` : ""}.
      </p>

      <div className="mt-8 grid gap-4">
        <Link href="/accountEdit" className="rounded-xl border px-4 py-3 hover:bg-gray-50">
          Manage profile 
        </Link>
        <Link href="/orders" className="rounded-xl border px-4 py-3 hover:bg-gray-50">
          Your orders (Check On Orders)
        </Link>
        <Link href="/accountEdit" className="rounded-xl border px-4 py-3 hover:bg-gray-50">
          Saved Cards (Edit Payment Information)
        </Link>
        <Link href="/accountEdit" className="rounded-xl border px-4 py-3 hover:bg-gray-50">
          Adress Information (Edit Shipping and Billing Info)
        </Link>
        <Link href="/forgot-password" className="rounded-xl border px-4 py-3 hover:bg-gray-50">
          Change Password 
        </Link>
      </div>
    </main>
  );
}
