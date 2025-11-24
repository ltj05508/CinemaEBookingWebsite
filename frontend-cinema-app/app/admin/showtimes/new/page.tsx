"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthStatus } from "@/lib/authClient";
import { getMovies } from "@/lib/data";

type MovieLite = { id: string; title: string };

const API_BASE = process.env.NEXT_PUBLIC_API_BASE?.replace(/\/$/, "") || "";

export default function NewShowtimePage() {
    const router = useRouter();
    const [movies, setMovies] = useState<MovieLite[]>([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    const showrooms = useMemo(
        () => [
            { id: "1", name: "Showroom 1" },
            { id: "2", name: "Showroom 2" },
            { id: "3", name: "Showroom 3" },
        ],
        []
    );

    useEffect(() => {
        (async () => {
            const status = await getAuthStatus();
            const role = String(status?.user?.role ?? "").toLowerCase();
            if (!status?.loggedIn) router.replace(`/login?redirect=${encodeURIComponent("/auth/showtimes/new")}`);
            if (role !== "admin") router.replace("/account");

            try {
                const all = await getMovies();
                setMovies(all.map((m: any) => ({ id: String(m.id), title: m.title })));
            } catch (e: any) {
                setErr("Failed to load movies.");
            } finally {
                setLoading(false);
            }
        })();
    }, [router]);

    async function hasConflict(showroomId: string, startIso: string) {
        const dateOnly = startIso.slice(0, 10);
        const res = await fetch(`${API_BASE}/api/showtimes?showroomId=${encodeURIComponent(showroomId)}&date=${encodeURIComponent(dateOnly)}`);
        if (!res.ok) return false;
        const list = await res.json();
        return list.some((s: any) => String(s.showroomId) === showroomId && String(s.startTimeIso) === startIso);
    }

    async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setErr(null);
        setSubmitting(true);
        const fd = new FormData(e.currentTarget);
        const movieId = String(fd.get("movieId") || "");
        const showroomId = String(fd.get("showroomId") || "");
        const showtime = String(fd.get("showtime") || "");

        if (!movieId || !showroomId || !showtime) {
            setErr("All fields are required.");
            setSubmitting(false);
            return;
        }

        // Convert local datetime to ISO string
        const startTimeIso = new Date(startLocal).toISOString();

        try {
            const conflict = await hasConflict(showroomId, startTimeIso);
            if (conflict) {
                setErr("Conflict: Another movie is already scheduled at that date/time in this showroom.");
                setSubmitting(false);
                return;
            }

            const payload = { movieId, showroomId, startTimeIso };
            const res = await fetch("/api/admin/showtimes", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });
            if (!res.ok) {
                const j = await res.json().catch(() => ({}));
                throw new Error(j?.message || `Create failed (${res.status})`);
            }
            router.push("/admin?showtimeCreated=1");
        } catch (e: any) {
            setErr(e.message || "Failed to schedule showtime.");
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="relative z-10 mx-auto max-w-2xl px-4 py-8 space-y-6">
            <h1 className="text-2xl font-semibold">Schedule a Movie</h1>

            {err && <p className="text-red-600">{err}</p>}
            {loading ? (
                <p>Loading…</p>
            ) : (
                <form onSubmit={onSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm mb-1">Movie *</label>
                        <select name="movieId" required className="w-full border rounded-xl px-3 py-2" defaultValue="">
                            <option value="" disabled>
                                Select a movie
                            </option>
                            {movies.map((m) => (
                                <option key={m.id} value={m.id}>
                                    {m.title}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm mb-1">Showroom *</label>
                        <select name="showroomId" required className="w-full border rounded-xl px-3 py-2" defaultValue="">
                            <option value="" disabled>
                                Select a showroom
                            </option>
                            {showrooms.map((r) => (
                                <option key={r.id} value={r.id}>
                                    {r.name}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm mb-1">Showtime (Military Time)*</label>         
                        <input name="showtime" required placeholder="14:00:00" className="w-full border rounded-xl px-3 py-2" />
                    </div>

                    <div className="flex gap-3">
                        <button disabled={submitting} className="rounded-xl border px-4 py-2 hover:bg-gray-50">
                            {submitting ? "Saving…" : "Save Showtime"}
                        </button>
                        <button type="button" onClick={() => router.back()} className="rounded-xl border px-4 py-2">
                            Cancel
                        </button>
                    </div>
                </form>
            )}
        </main>
    );
}
