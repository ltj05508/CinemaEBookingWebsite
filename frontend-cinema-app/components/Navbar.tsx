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




/*
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
  */

/*
"use client";

import Link from "next/link";
import { useEffect, useRef, useState, useTransition } from "react"; // NEW: useRef, useTransition
import { useRouter } from "next/navigation";
import { getAuthStatus, logout } from "@/lib/authClient";

type AuthState = { loggedIn: boolean; user?: { role?: string } | any } | null;

export default function Navbar() {
  const router = useRouter();
  const [auth, setAuth] = useState<AuthState>(null);
  const [busy, setBusy] = useState(false);

  // NEW: prevent race conditions between auth status calls & logout
  const reqIdRef = useRef(0);          // NEW: tracks latest status request
  const loggingOutRef = useRef(false); // NEW: flag to suppress status during logout
  const [_, startTransition] = useTransition(); // NEW: fallback nav path without window

  async function loadStatus() {
    if (loggingOutRef.current) return; // NEW: ignore while logging out
    const myReqId = ++reqIdRef.current; // NEW: increment request id for this call
    try {
      const data = await getAuthStatus();
      // NEW: ignore stale responses or if logout started mid-flight
      if (loggingOutRef.current || myReqId !== reqIdRef.current) return;
      setAuth(data);
    } catch {
      if (loggingOutRef.current || myReqId !== reqIdRef.current) return; // NEW
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
      // NEW: bump req id so any late responses are ignored after unmount
      reqIdRef.current++;
      window.removeEventListener("auth:changed", onAuthChanged);
      document.removeEventListener("visibilitychange", onVisible);
    };
  }, []);

  async function onLogout() {
    if (busy) return;
    setBusy(true);
    loggingOutRef.current = true; // NEW: stop status updates during logout
    reqIdRef.current++;           // NEW: invalidate any in-flight status calls

    try {
      await logout();
      setAuth({ loggedIn: false });
      window.dispatchEvent(new Event("auth:changed"));

      // NEW: hard redirect to ensure a clean post-logout state with no stale caches
      if (typeof window !== "undefined") {
        window.location.replace("/login");
      } else {
        // Fallback (very unlikely in client)
        startTransition(() => {
          router.replace("/login");
          router.refresh();
        });
      }
    } finally {
      // NEW: intentionally not resetting busy to avoid flicker before navigation
      // setBusy(false);
      router.replace("/login");
      router.refresh();
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
  */

"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthStatus, logout } from "@/lib/authClient";

type AuthState = { loggedIn: boolean; user?: { role?: string } | any } | null;

export default function Navbar() {
  const router = useRouter();
  const [auth, setAuth] = useState<AuthState>(null);
  const [busy, setBusy] = useState(false);

  // Prevent stale auth updates racing with logout
  const reqIdRef = useRef(0);
  const loggingOutRef = useRef(false);

  async function loadStatus() {
    if (loggingOutRef.current) return; // suppress while logging out
    const myReqId = ++reqIdRef.current;
    try {
      const data = await getAuthStatus(); // cache-busted in authClient
      if (loggingOutRef.current || myReqId !== reqIdRef.current) return;
      setAuth(data);
    } catch {
      if (loggingOutRef.current || myReqId !== reqIdRef.current) return;
      setAuth({ loggedIn: false });
    }
  }

  useEffect(() => {
    let mounted = true;
    (async () => {
      if (mounted) await loadStatus();
    })();

    const onAuthChanged = () => loadStatus();
    const onVisible = () => { if (!document.hidden) loadStatus(); };

    window.addEventListener("auth:changed", onAuthChanged);
    document.addEventListener("visibilitychange", onVisible);

    return () => {
      mounted = false;
      // invalidate any in-flight requests
      reqIdRef.current++;
      window.removeEventListener("auth:changed", onAuthChanged);
      document.removeEventListener("visibilitychange", onVisible);
    };
  }, []);

  async function onLogout() { //Propagating error probably
    if (busy) return;
    setBusy(true);
    loggingOutRef.current = true;   // stop status updates during logout
    reqIdRef.current++;             // invalidate any in-flight status calls

    try {
      await logout();               // server: invalidate session 

      // 🔹 Immediately flip UI to logged-out
      setAuth({ loggedIn: false });

      // 🔹 Notify any listeners
      window.dispatchEvent(new Event("auth:changed"));

      // 🔹 Try client navigation first
      router.replace("/login");
      router.refresh();

      // 🔹 Hard fallback to guarantee fresh state (in case router caching interferes)
      setTimeout(() => {
        if (typeof window !== "undefined" && !window.location.pathname.startsWith("/login")) {
          window.location.replace("/login");
        }
      }, 50);
    } catch {
      // If server call throws, allow user to retry and re-check status
      loggingOutRef.current = false;
      setBusy(false);
      await loadStatus();
    } finally {
      // Final safety: if for any reason we're still on the same page after a moment,
      // clear busy and refresh status so the button doesn't stay "Logging out..."
      setTimeout(async () => {
        if (typeof window !== "undefined" && !window.location.pathname.startsWith("/login")) {
          loggingOutRef.current = false;
          setBusy(false);
          await loadStatus();
        }
      }, 800);
      router.replace("/login");
      router.refresh();
    }
  }

  const isLoadingInitial = auth === null;

  return (
    <header className="sticky top-0 z-50 bg-[#BA0C2F] backdrop-blur border-b">
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


