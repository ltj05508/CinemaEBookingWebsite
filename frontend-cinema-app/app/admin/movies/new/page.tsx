"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthStatus } from "@/lib/authClient";

export default function NewMoviePage() {
    const router = useRouter();
    const [submitting, setSubmitting] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    useEffect(() => {
        (async () => {
            const status = await getAuthStatus();
            const role = String(status?.user?.role ?? "").toLowerCase();
            if (!status?.loggedIn) router.replace(`/login?redirect=${encodeURIComponent("/admin/movies/new")}`);
            if (role !== "admin") router.replace("/account");
        })();
    }, [router]);

    async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setErr(null);
        setSubmitting(true);

        const form = e.currentTarget as HTMLFormElement;
        const data = Object.fromEntries(new FormData(form)) as any;

        const payload = {
            title: String(data.title).trim(),
            description: String(data.description).trim(),
            genres: String(data.genres).split(",").map((g: string) => g.trim()).filter(Boolean),
            durationMinutes: Number(data.durationMinutes),
            rating: String(data.rating).trim(),
            status: String(data.status),
            posterUrl: String(data.posterUrl).trim(),
            trailerUrl: String(data.trailerUrl).trim(),
            releaseDate: String(data.releaseDate), // YYYY-MM-DD
        };

        if (!payload.title || !payload.description || !payload.genres.length || !payload.durationMinutes || !payload.status || !payload.releaseDate) {
            setSubmitting(false);
            setErr("Please fill in all required fields.");
            return;
        }

        try {
            const res = await fetch("/api/movies", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });
            if (!res.ok) {
                const j = await res.json().catch(() => ({}));
                throw new Error(j?.message || `Create failed (${res.status})`);
            }
            router.push("/admin?created=1");
        } catch (e: any) {
            setErr(e.message || "Failed to create movie.");
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="relative z-10 mx-auto max-w-2xl px-4 py-8 space-y-6">
            <h1 className="text-2xl font-semibold">Add Movie</h1>

            {err && <p className="text-red-600">{err}</p>}

            <form onSubmit={onSubmit} className="space-y-4">
                <div>
                    <label className="block text-sm mb-1">Title *</label>
                    <input name="title" required className="w-full border rounded-xl px-3 py-2" />
                </div>

                <div>
                    <label className="block text-sm mb-1">Description *</label>
                    <textarea name="description" required className="w-full border rounded-xl px-3 py-2" rows={4} />
                </div>

                <div className="grid sm:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm mb-1">Genres (comma separated) *</label>
                        <input name="genres" required placeholder="Action, Adventure" className="w-full border rounded-xl px-3 py-2" />
                    </div>
                    <div>
                        <label className="block text-sm mb-1">Duration (minutes) *</label>
                        <input name="durationMinutes" type="number" min={1} required className="w-full border rounded-xl px-3 py-2" />
                    </div>
                </div>

                <div className="grid sm:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm mb-1">Rating *</label>
                        <input name="rating" required placeholder="G, PG, PG-13, R" className="w-full border rounded-xl px-3 py-2" />
                    </div>
                    <div>
                        <label className="block text-sm mb-1">Status *</label>
                        <select name="status" required className="w-full border rounded-xl px-3 py-2">
                            <option value="RUNNING">RUNNING</option>
                            <option value="COMING_SOON">COMING_SOON</option>
                        </select>
                    </div>
                </div>

                <div className="grid sm:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm mb-1">Poster URL *</label>
                        <input name="posterUrl" required className="w-full border rounded-xl px-3 py-2" />
                    </div>
                    <div>
                        <label className="block text-sm mb-1">Trailer URL</label>
                        <input name="trailerUrl" className="w-full border rounded-xl px-3 py-2" />
                    </div>
                </div>

                <div>
                    <label className="block text-sm mb-1">Release Date *</label>
                    <input
                        name="releaseDate"
                        type="date"
                        required
                        className="w-full border rounded-xl px-3 py-2 bg-white"
                        inputMode="numeric"
                        placeholder="YYYY-MM-DD"
                        pattern="\d{4}-\d{2}-\d{2}"
                    />
                </div>

                <div className="flex gap-3">
                    <button disabled={submitting} className="rounded-xl border px-4 py-2 hover:bg-gray-50">
                        {submitting ? "Saving…" : "Save Movie"}
                    </button>
                    <button type="button" onClick={() => router.back()} className="rounded-xl border px-4 py-2">
                        Cancel
                    </button>
                </div>
            </form>
        </main>
    );
}
