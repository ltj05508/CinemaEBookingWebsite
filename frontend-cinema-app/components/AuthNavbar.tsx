"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthStatus, logout } from "@/lib/authClient";

export default function Navbar() {
  const router = useRouter();
  const [auth, setAuth] = useState<{ loggedIn: boolean; user?: any } | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const data = await getAuthStatus();
        if (mounted) setAuth(data);
      } catch {
        if (mounted) setAuth({ loggedIn: false });
      }
    })();
    return () => {
      mounted = false;
    };
  }, []);

  async function onLogout() {
    if (busy) return;
    setBusy(true);
    try {
      await logout();
      setAuth({ loggedIn: false });
      router.push("/login");
    } finally {
      setBusy(false);
    }
  }

  return (
    <header className="sticky top-0 z-50 bg-white/80 backdrop-blur border-b">
      <nav className="mx-auto max-w-6xl px-4 h-14 flex items-center justify-between">
        <Link href="/" className="font-semibold text-lg">
          🎬 CineBook
        </Link>

        <div className="flex items-center gap-5 text-sm">
          <Link href="/movies" className="hover:underline">
            Movies
          </Link>
          <Link href="/about" className="hover:underline">
            About
          </Link>

          {auth?.loggedIn ? (
            <>
              <Link href="/account" className="hover:underline">
                Profile
              </Link>
              <button
                onClick={onLogout}
                disabled={busy}
                className="rounded-md border px-3 py-1 hover:bg-gray-50 disabled:opacity-60"
              >
                {busy ? "Logging out…" : "Log out"}
              </button>
            </>
          ) : (
            <Link href="/login" className="hover:underline">
              Login
            </Link>
          )}
        </div>
      </nav>
    </header>
  );
}
