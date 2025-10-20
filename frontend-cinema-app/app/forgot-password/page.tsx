"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { requestPasswordReset } from "@/lib/authClient";

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState("");
    const [submitted, setSubmitted] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        try {
            const last = localStorage.getItem("lastLoginEmail");
            if (last) setEmail(last);
        } catch {}
    }, []);

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);

        if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            setError("Please enter a valid email address.");
            return;
        }

        setLoading(true);
        try {
            await requestPasswordReset({ email });
            setSubmitted(true);
        } catch (err: any) {
            setError(err?.message || "Could not start reset process. Try again.");
        } finally {
            setLoading(false);
        }
    }

    if (submitted) {
        return (
            <main className="mx-auto max-w-md px-4 py-8">
                <h1 className="text-2xl font-semibold">Check your email</h1>
                <p className="mt-2 text-sm opacity-80">
                    If an account exists for <span className="font-medium">{email}</span>, we sent a secure link to reset your password.
                </p>
                <div className="mt-6">
                    <Link href="/login" className="text-indigo-600 hover:underline">
                        Return to login
                    </Link>
                </div>
            </main>
        );
    }

    return (
        <main className="mx-auto max-w-md px-4 py-8">
            <h1 className="text-3xl font-semibold">Forgot password</h1>
            <p className="mt-2 text-sm opacity-80">Enter your email and we'll send you a reset link.</p>

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
                    {loading ? "Sending…" : "Send reset link"}
                </button>
            </form>

            <div className="mt-6 text-sm">
                <Link href="/login" className="text-indigo-600 hover:underline">
                    Back to login
                </Link>
            </div>
        </main>
    );
}
