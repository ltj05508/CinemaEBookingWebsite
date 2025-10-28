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




/*
import Link from "next/link";

export default function Navbar() {
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
          <Link href="/login" className="hover:underline">
            Login
          </Link>
        </div>
      </nav>
    </header>
  );
}
  */
