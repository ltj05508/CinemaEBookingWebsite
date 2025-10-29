"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthStatus, logout } from "@/lib/authClient";

export default function Navbar() {
  const router = useRouter();
  const [auth, setAuth] = useState<{ loggedIn: boolean; user?: any } | null>(null);
  const [busy, setBusy] = useState(false);

  async function loadStatus() {
    try {
      const data = await getAuthStatus();
      setAuth(data);
    } catch {
      setAuth({ loggedIn: false });
    }
  }

  useEffect(() => {
    let mounted = true;
    (async () => {
      if (mounted) await loadStatus();
    })();

    const onAuthChanged = () => loadStatus();
    const onVisible = () => {
      if (!document.hidden) loadStatus();
    };

    window.addEventListener("auth:changed", onAuthChanged);
    document.addEventListener("visibilitychange", onVisible);

    return () => {
      mounted = false;
      window.removeEventListener("auth:changed", onAuthChanged);
      document.removeEventListener("visibilitychange", onVisible);
    };
  }, []);

  async function onLogout() {
    if (busy) return;
    setBusy(true);
    try {
      await logout();
      setAuth({ loggedIn: false });
      if (typeof window !== "undefined") {
        window.dispatchEvent(new Event("auth:changed"));
      }
      router.push("/login");
      router.refresh();
    } finally {
      setBusy(false);
    }

    router.push("/login");
    router.refresh();
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

