/*
"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getAuthStatus } from "@/lib/authClient";

export default function Navbar() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  // Check login status once when the component mounts
  useEffect(() => {
    async function checkLoginStatus() {
      try {
        const status = await getAuthStatus();
        setIsLoggedIn(status.loggedIn);
      } catch (error) {
        console.error("Error checking auth status:", error);
      }

      
    }

    window.addEventListener("auth-change", checkLoginStatus);
      return () => window.removeEventListener("auth-change", checkLoginStatus);

    checkLoginStatus();
  }, []);

  return (
    <header className="sticky top-0 z-50 bg-ugared/80 backdrop-blur border-b">
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
          <Link
            href={isLoggedIn ? "/account" : "/login"}
            className="hover:underline"
          >
            {isLoggedIn ? "Account" : "Login"}
          </Link>
        </div>
      </nav>
    </header>
  );
}
  */





"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthStatus, logout } from "@/lib/authClient";

type AuthState = { loggedIn: boolean; user?: { role?: string } | any } | null;

export default function Navbar() {
  const router = useRouter();
  const [auth, setAuth] = useState<AuthState>(null);
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

    // listen for auth change + tab focus refresh
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
      window.dispatchEvent(new Event("auth:changed"));
      router.push("/login");
      router.refresh();
    } finally {
      setBusy(false);
    }
  }

  const isLoadingInitial = auth === null;

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

          {isLoadingInitial ? (
            <span className="opacity-60">...</span>
          ) : auth?.loggedIn ? (
            <>
              {auth?.user?.role === "admin" && (
                <Link href="/admin" className="hover:underline">
                  Admin
                </Link>
              )}
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

