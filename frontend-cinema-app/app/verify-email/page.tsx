"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { verifyEmail } from "@/lib/authClient";

export default function VerifyEmailPage() {
    const router = useRouter();
    const sp = useSearchParams();
    const email = sp.get("email") || "";
    const redirect = sp.get("redirect") || "/account";

    const [code, setCode] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    // Auto-focus or pre-fill helpers could go here
    useEffect(() => {
        // noop
    }, []);

    function clean(input: string) {
        // keep digits only; backend expects 6-digit code
        return input.replace(/\D/g, "").slice(0, 6);
    }

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);

        const c = clean(code);
        if (c.length !== 6) {
            setError("Enter the 6-digit code from your email.");
            return;
        }

        setLoading(true);
        try {
            await verifyEmail(c);
            setSuccess(true);
        } catch (err: any) {
            setError(err?.message || "Invalid or expired code. Please try again.");
        } finally {
            setLoading(false);
        }
    }

    if (success) {
        return (
            <main className="mx-auto max-w-md w-full px-4 py-8">
                <h1 className="text-2xl font-semibold">Email verified</h1>
                <p className="mt-2 text-sm opacity-80">
                    Your account is now active. You can sign in.
                </p>
                <div className="mt-6 flex gap-3">
                    <Link
                        href={`/login?redirect=${encodeURIComponent(redirect)}`}
                        className="rounded-xl bg-indigo-600 px-4 py-2 font-medium text-white"
                    >
                        Go to login
                    </Link>
                    <Link href="/" className="rounded-xl border px-4 py-2">Back to home</Link>
                </div>
            </main>
        );
    }

    return (
        <main className="mx-auto max-w-md w-full px-4 py-8">
            <h1 className="text-3xl font-semibold tracking-tight">Verify your email</h1>
            <p className="mt-2 text-sm opacity-80">
                Enter the 6-digit code we sent to {email ? <span className="font-medium">{email}</span> : "your email"}.
            </p>

            <form onSubmit={onSubmit} className="mt-8 space-y-4">
                <div className="space-y-2">
                    <label htmlFor="code" className="text-sm font-medium">Verification code</label>
                    <input
                        id="code"
                        inputMode="numeric"
                        pattern="[0-9]*"
                        maxLength={6}
                        placeholder="123456"
                        className="w-full rounded-xl border px-3 py-2 tracking-widest text-center text-lg outline-none focus:ring-2 focus:ring-indigo-500"
                        value={code}
                        onChange={(e) => setCode(e.target.value)}
                        required
                    />
                    <p className="text-xs opacity-60">Codes expire after a short time. You can request another from the signup page.</p>
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
                    {loading ? "Verifying…" : "Verify"}
                </button>
            </form>

            <div className="mt-6 text-sm">
                <Link href="/signup" className="text-indigo-600 hover:underline">Back to signup</Link>
            </div>
        </main>
    );
}
