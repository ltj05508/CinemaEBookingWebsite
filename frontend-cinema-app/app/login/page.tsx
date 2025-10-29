"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { login } from "@/lib/authClient";

export default function LoginPage() {
  const router = useRouter();
  const sp = useSearchParams();
  const redirect = sp.get("redirect") ?? "/";

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [remember, setRemember] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (loading) return; // avoid double submits
    setError(null);

    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError("Please enter a valid email address.");
      return;
    }
    if (!password || password.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }

    setLoading(true);
    try {
      const res = await login({ email, password, remember });
      //const role = res?.user?.role ?? "user";
      const rawRole = res?.user?.role;
      const role = rawRole != null ? String(rawRole).toLowerCase() : "user";

      try {
        if (remember) localStorage.setItem("lastLoginEmail", email);
        else localStorage.removeItem("lastLoginEmail");
      } catch {}

      // Navigate based on role or redirect param
      if (role === "admin") router.replace("/admin");
      else if (redirect && redirect !== "/") router.replace(redirect);
      else router.replace("/account");

      // Notify the app and force revalidation so Navbar updates immediately
      if (typeof window !== "undefined") {
        window.dispatchEvent(new Event("auth:changed"));
      }
      router.refresh();
    } catch (err: any) {
      setError(err?.message || "Login failed. Check credentials and try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="mx-auto max-w-md w-full px-4 py-8">
      <h1 className="text-3xl font-semibold">Log in</h1>
      <p className="mt-2 text-sm opacity-80">Welcome back. Please enter your details.</p>

      <form onSubmit={onSubmit} className="mt-8 space-y-4">
        <div className="space-y-2">
          <label htmlFor="email" className="text-sm font-medium">Email</label>
          <input
            id="email"
            type="email"
            className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-indigo-500"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
            required
          />
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <label htmlFor="password" className="text-sm font-medium">Password</label>
            <Link href="/forgot-password" className="text-sm text-indigo-600 hover:underline">
              Forgot password?
            </Link>
          </div>
          <div className="relative">
            <input
              id="password"
              type={showPassword ? "text" : "password"}
              className="w-full rounded-xl border px-3 py-2 pr-12 outline-none focus:ring-2 focus:ring-indigo-500"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-2 top-1/2 -translate-y-1/2 text-sm px-2 py-1 rounded-md border hover:bg-gray-50"
            >
              {showPassword ? "Hide" : "Show"}
            </button>
          </div>
        </div>

        <div className="flex items-center justify-between">
          <label className="inline-flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={remember}
              onChange={(e) => setRemember(e.target.checked)}
              className="size-4"
            />
            <span className="text-sm">Remember me</span>
          </label>
          <Link href="/signup" className="text-sm text-indigo-600 hover:underline">
            Need an account? Sign up
          </Link>
        </div>

        {error && (
          <div className="rounded-xl border border-red-300 bg-red-50 px-3 py-2 text-red-700 text-sm">
            {error}
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-xl bg-indigo-600 px-4 py-2 font-medium text-white disabled:opacity-60"
        >
          {loading ? "Signing in…" : "Sign in"}
        </button>
      </form>
    </main>
  );
}

