"use client";

import { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { resetPassword } from "@/lib/authClient";

export default function ResetPasswordPage() {
    const { token } = useParams<{ token: string }>();
    const router = useRouter();

    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);

        if (!password || password.length < 8) {
            setError("Password must be at least 8 characters.");
            return;
        }
        if (password !== confirm) {
            setError("Passwords do not match.");
            return;
        }

        setLoading(true);
        try {
            await resetPassword({ token: String(token), newPassword: password });
            setSuccess(true);
        } catch (err: any) {
            setError(err?.message || "Reset failed. Link may be invalid or expired.");
        } finally {
            setLoading(false);
        }
    }

    if (success) {
        return (
            <main className="mx-auto max-w-md px-4 py-8">
                <h1 className="text-2xl font-semibold">Password updated</h1>
                <p className="mt-2 text-sm opacity-80">You can now sign in with your new password.</p>
                <button
                    onClick={() => router.push("/login")}
                    className="mt-6 rounded-xl bg-indigo-600 px-4 py-2 font-medium text-white"
                >
                    Go to login
                </button>
            </main>
        );
    }

    return (
        <main className="mx-auto max-w-md px-4 py-8">
            <h1 className="text-3xl font-semibold">Set a new password</h1>
            <form onSubmit={onSubmit} className="mt-8 space-y-4">
                <div className="space-y-2">
                    <label htmlFor="password" className="text-sm font-medium">New password</label>
                    <input
                        id="password"
                        type="password"
                        className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-indigo-500"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="••••••••"
                        required
                    />
                </div>

                <div className="space-y-2">
                    <label htmlFor="confirm" className="text-sm font-medium">Confirm password</label>
                    <input
                        id="confirm"
                        type="password"
                        className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-indigo-500"
                        value={confirm}
                        onChange={(e) => setConfirm(e.target.value)}
                        placeholder="••••••••"
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
                    {loading ? "Updating…" : "Update password"}
                </button>
            </form>
        </main>
    );
}
