"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { registerUser } from "@/lib/authClient";

export default function SignupPage() {
    const router = useRouter();
    const sp = useSearchParams();
    // Where to go after the user verifies email and logs in later:
    const redirect = sp.get("redirect") ?? "/account";

    const [firstName, setFirstName] = useState("");
    const [lastName,  setLastName]  = useState("");
    const [email,     setEmail]     = useState("");
    const [password,  setPassword]  = useState("");
    const [confirm,   setConfirm]   = useState("");
    const [acceptTos, setAcceptTos] = useState(false);

    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading]   = useState(false);
    const [error,   setError]     = useState<string | null>(null);
    const [done,    setDone]      = useState<null | { email: string }>(null);

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    function validate(): string | null {
        if (!firstName.trim()) return "Please enter your first name.";
        if (!lastName.trim())  return "Please enter your last name.";
        if (!email || !emailRegex.test(email)) return "Please enter a valid email address.";
        if (!password || password.length < 8) return "Password must be at least 8 characters.";
        if (password !== confirm) return "Passwords do not match.";
        if (!acceptTos) return "You must accept the Terms to continue.";
        return null;
    }

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);

        const v = validate();
        if (v) { setError(v); return; }

        setLoading(true);
        try {
            // Backend returns a verification code and sends an email (per your service).
            // We don't need to show the code; we just send the user to /verify-email.
            await registerUser({ firstName, lastName, email, password });
            setDone({ email });
            // send them to verify page and pass redirect destination
            router.push(`/verify-email?email=${encodeURIComponent(email)}&redirect=${encodeURIComponent(redirect)}`);
        } catch (err: any) {
            setError(err?.message || "Signup failed. Please try again.");
        } finally {
            setLoading(false);
        }
    }

    // Fallback success view if routing is blocked; normally we navigate immediately.
    if (done) {
        return (
            <main className="mx-auto max-w-md w-full px-4 py-8">
                <h1 className="text-2xl font-semibold">Check your email</h1>
                <p className="mt-2 text-sm opacity-80">
                    We sent a 6-digit code to <span className="font-medium">{done.email}</span>. Enter it to activate your account.
                </p>
                <div className="mt-6 flex gap-3">
                    <Link
                        href={`/verify-email?email=${encodeURIComponent(done.email)}&redirect=${encodeURIComponent(redirect)}`}
                        className="rounded-xl bg-indigo-600 px-4 py-2 font-medium text-white"
                    >
                        Verify email
                    </Link>
                    <Link href="/" className="rounded-xl border px-4 py-2">Back to home</Link>
                </div>
            </main>
        );
    }

    return (
        <main className="mx-auto max-w-md w-full px-4 py-8">
            <h1 className="text-3xl font-semibold tracking-tight">Create your account</h1>
            <p className="mt-2 text-sm opacity-80">We’ll email you a code to verify before your first login.</p>

            <form onSubmit={onSubmit} className="mt-8 space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div className="space-y-2">
                        <label htmlFor="firstName" className="text-sm font-medium">First name</label>
                        <input
                            id="firstName"
                            type="text"
                            className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-indigo-500"
                            value={firstName}
                            onChange={(e) => setFirstName(e.target.value)}
                            placeholder="Jane"
                            required
                        />
                    </div>
                    <div className="space-y-2">
                        <label htmlFor="lastName" className="text-sm font-medium">Last name</label>
                        <input
                            id="lastName"
                            type="text"
                            className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-indigo-500"
                            value={lastName}
                            onChange={(e) => setLastName(e.target.value)}
                            placeholder="Doe"
                            required
                        />
                    </div>
                </div>

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
                    <label htmlFor="password" className="text-sm font-medium">Password</label>
                    <div className="relative">
                        <input
                            id="password"
                            type={showPassword ? "text" : "password"}
                            className="w-full rounded-xl border px-3 py-2 pr-12 outline-none focus:ring-2 focus:ring-indigo-500"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="At least 8 characters"
                            required
                        />
                        <button
                            type="button"
                            onClick={() => setShowPassword((s) => !s)}
                            className="absolute right-2 top-1/2 -translate-y-1/2 text-sm px-2 py-1 rounded-md border hover:bg-gray-50"
                            aria-label={showPassword ? "Hide password" : "Show password"}
                        >
                            {showPassword ? "Hide" : "Show"}
                        </button>
                    </div>
                </div>

                <div className="space-y-2">
                    <label htmlFor="confirm" className="text-sm font-medium">Confirm password</label>
                    <input
                        id="confirm"
                        type={showPassword ? "text" : "password"}
                        className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-indigo-500"
                        value={confirm}
                        onChange={(e) => setConfirm(e.target.value)}
                        placeholder="Re-enter your password"
                        required
                    />
                </div>

                <label className="inline-flex items-start gap-3 text-sm select-none cursor-pointer">
                    <input
                        type="checkbox"
                        className="size-4 mt-0.5"
                        checked={acceptTos}
                        onChange={(e) => setAcceptTos(e.target.checked)}
                    />
                    <span>
            I agree to the{" "}
                        <Link href="/terms" className="text-indigo-600 hover:underline">Terms of Service</Link>{" "}
                        and{" "}
                        <Link href="/privacy" className="text-indigo-600 hover:underline">Privacy Policy</Link>.
          </span>
                </label>

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
                    {loading ? "Creating account…" : "Create account"}
                </button>

                <p className="text-sm opacity-80 text-center">
                    Already have an account?{" "}
                    <Link href={`/login?redirect=${encodeURIComponent(redirect)}`} className="text-indigo-600 hover:underline">
                        Log in
                    </Link>
                </p>
            </form>
        </main>
    );
}
