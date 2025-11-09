"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { getAuthStatus } from "@/lib/authClient";

import { getMovies, searchMovies, filterMoviesByGenre } from "@/lib/data";
import type { Movie } from "@/types/cinema";
import MovieCard from "@/components/MovieCard";
import SearchFilterBar from "@/components/SearchFilterBar";
import Trailer from "@/components/Trailer";

export default function AdminPage() {
  const router = useRouter();
  const sp = useSearchParams();
  const q = sp.get("q");
  const genre = sp.get("genre");

  const [checkingAuth, setCheckingAuth] = useState(true);
  const [allowed, setAllowed] = useState(false);

  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const status = await getAuthStatus();
        const role = String(status?.user?.role ?? "").toLowerCase();
        const ok = !!status?.loggedIn && role === "admin";
        setAllowed(ok);
        if (!status?.loggedIn) {
          router.replace(`/login?redirect=${encodeURIComponent("/admin")}`);
          return;
        }
        if (!ok) {
          router.replace("/account");
          return;
        }
      } finally {
        setCheckingAuth(false);
      }
    })();
  }, [router]);

  useEffect(() => {
    if (!allowed) return;
    (async () => {
      setLoading(true);
      setError(null);
      try {
        let list: Movie[] = [];
        if (q && q.trim().length > 0) {
          list = await searchMovies(q.trim());
        } else if (genre && genre.trim().length > 0) {
          list = await filterMoviesByGenre(genre.trim());
        } else {
          list = await getMovies();
        }
        setMovies(list ?? []);
      } catch (e: any) {
        setError(e?.message || "Failed to load movies.");
      } finally {
        setLoading(false);
      }
    })();
  }, [allowed, q, genre]);

  if (checkingAuth) {
    return (
        <main className="mx-auto max-w-5xl px-4 py-6">
          <p>Checking permissions…</p>
        </main>
    );
  }

  if (!allowed) {
    return (
        <main className="mx-auto max-w-5xl px-4 py-6">
          <h1 className="text-2xl font-semibold">Admin</h1>
          <p className="text-red-600 mt-2">You are not authorized to view this page.</p>
        </main>
    );
  }

  return (
      <main className="mx-auto max-w-6xl px-4 py-6 space-y-6">
        <header className="flex items-center gap-3">
          <h1 className="text-2xl font-semibold">Admin Portal</h1>
          <div className="ml-auto w-full sm:w-auto">
            <SearchFilterBar />
          </div>
        </header>

        {/* Main menu as grouped cards. "Add Movie" and "Schedule a Movie" live under Manage Movies. */}
        <nav className="grid gap-4 sm:grid-cols-2">
          {/* Manage Movies Group */}
          <section className="rounded-2xl border p-4">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold">Manage Movies</h2>
              <button
                  className="rounded-xl border px-3 py-1 hover:bg-gray-50"
                  onClick={() => router.push("/admin/movies")}
                  aria-label="Open Manage Movies"
                  title="Open Manage Movies"
              >
                Open
              </button>
            </div>
            <div className="mt-3 grid gap-2 sm:grid-cols-2">
              <button
                  className="rounded-xl border px-3 py-2 text-left hover:bg-gray-50"
                  onClick={() => router.push("/admin/movies/new")}
              >
                ➕ Add Movie
              </button>
              <button
                  className="rounded-xl border px-3 py-2 text-left hover:bg-gray-50"
                  onClick={() => router.push("/admin/showtimes/new")}
              >
                🗓️ Schedule a Movie
              </button>
            </div>
          </section>

          {/* Manage Promotions */}
          <section className="rounded-2xl border p-4">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold">Manage Promotions</h2>
              <button
                  className="rounded-xl border px-3 py-1 hover:bg-gray-50"
                  onClick={() => router.push("/admin/promotions")}
                  aria-label="Open Manage Promotions"
                  title="Open Manage Promotions"
              >
                Open
              </button>
            </div>
            <p className="text-sm text-gray-600 mt-2">
              Create promotion codes and (optionally) email subscribed users.
            </p>
          </section>

          {/* Manage Users */}
          <section className="rounded-2xl border p-4">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold">Manage Users</h2>
              <button
                  className="rounded-xl border px-3 py-1 hover:bg-gray-50"
                  onClick={() => router.push("/admin/users")}
                  aria-label="Open Manage Users"
                  title="Open Manage Users"
              >
                Open
              </button>
            </div>
            <p className="text-sm text-gray-600 mt-2">
              View users and subscription status for promotions.
            </p>
          </section>
        </nav>

        <ActionBanners />

        {/* Movies list (unchanged) */}
        <section className="space-y-3">
          <h2 className="text-xl font-semibold">
            {q ? `Results for “${q}”` : genre ? `Genre: ${genre}` : "All Movies"}
          </h2>
          {error && <p className="text-red-600">{error}</p>}
          {loading ? (
              <p>Loading…</p>
          ) : movies.length === 0 ? (
              <p className="text-gray-600">No movies found.</p>
          ) : (
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {movies.map((m) => (
                    <MovieCard key={m.id} movie={m} />
                ))}
              </div>
          )}
        </section>

        <Trailer />
      </main>
  );
}

function ActionBanners() {
  const sp = useSearchParams();
  const created = sp.get("created");
  const showtimeCreated = sp.get("showtimeCreated");
  const promoCreated = sp.get("promoCreated");

  if (!created && !showtimeCreated && !promoCreated) return null;

  return (
      <div className="space-y-2">
        {created && (
            <div className="rounded-xl border border-green-200 bg-green-50 px-4 py-2 text-sm">
              Movie created successfully.
            </div>
        )}
        {showtimeCreated && (
            <div className="rounded-xl border border-green-200 bg-green-50 px-4 py-2 text-sm">
              Showtime scheduled successfully.
            </div>
        )}
        {promoCreated && (
            <div className="rounded-xl border border-green-200 bg-green-50 px-4 py-2 text-sm">
              Promotion created successfully.
            </div>
        )}
      </div>
  );
}


