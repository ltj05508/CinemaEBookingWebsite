"use client";

import Link from "next/link";

export default function SignupPage() {
    return (
        <main className="mx-auto max-w-md px-4 py-8">
            <h1 className="text-3xl font-semibold">Create your account</h1>
            <p className="mt-2 text-sm opacity-80">
                This is a placeholder for signup. Implement the form when backend is ready.
            </p>
            <div className="mt-6">
                <Link href="/login" className="text-indigo-600 hover:underline">
                    Back to login
                </Link>
            </div>
        </main>
    );
}
