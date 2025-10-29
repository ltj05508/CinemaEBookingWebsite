// app/admin/page.tsx
"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { getAuthStatus } from "@/lib/authClient";

import Navbar from "@/components/Navbar"; // ✅ use the unified auth-aware navbar
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

  // ---- auth state ----
  const [checkingAuth, setCheckingAuth] = useState(true);
  const [allowed, setAllowed] = useState(false);

  // ---- movies state (mirrors Home) ----
  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // ---- Admin auth gate ----
  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const status = await getAuthStatus(); // cache: "no-store" in client
        if (!status?.loggedIn) {
          router.replace(`/login?redirect=${encodeURIComponent("/admin")}`);
          return;
        }
        //const role = status?.user?.role ?? "user";
        const rawRole = status?.user?.role;
        const role = rawRole != null ? String(rawRole).toLowerCase() : "user";
        if (role !== "admin") {
          router.replace("/account");
          return;
        }
        if (mounted) setAllowed(true);
      } catch {
        router.replace(`/login?redirect=${encodeURIComponent("/admin")}`);
      } finally {
        if (mounted) setCheckingAuth(false);
      }
    })();
    return () => {
      mounted = false;
    };
  }, [router]);

  // ---- Fetch movies (same logic as Home) ----
  useEffect(() => {
    if (!allowed) return;
    async function fetchMovies() {
      setLoading(true);
      setError(null);
      try {
        let results: Movie[];
        if (q && q.trim() && genre && genre !== "ALL") {
          const searchResults = await searchMovies(q.trim());
          results = searchResults.filter((m) => m.genres.includes(genre));
        } else if (q && q.trim()) {
          results = await searchMovies(q.trim());
        } else if (genre && genre !== "ALL") {
          results = await filterMoviesByGenre(genre);
        } else {
          results = await getMovies();
        }
        setMovies(results);
      } catch (err) {
        console.error("Error loading movies:", err);
        setError("Failed to load movies. Please try again.");
      } finally {
        setLoading(false);
      }
    }
    fetchMovies();
  }, [allowed, q, genre]);

  const running = movies.filter((m) => m.status === "RUNNING");
  const coming = movies.filter((m) => m.status === "COMING_SOON");
  const featuredTrailer = running[0]?.trailerUrl;

  if (checkingAuth) {
    return (
      <>
        <Navbar />
        <main className="mx-auto max-w-6xl w-full px-4 py-8">
          <p className="opacity-80">Checking access…</p>
        </main>
      </>
    );
  }

  if (!allowed) return null;

  return (
    <>
      <Navbar />

      <main className="mx-auto max-w-6xl w-full px-4 py-8 space-y-8">
        {/* Toolbar (non-functional) + search/filter */}
        <section className="space-y-3">
          <div className="flex flex-wrap items-center gap-3">
            <button className="rounded-xl border px-4 py-2 hover:bg-gray-50">Add Movie</button>
            <button className="rounded-xl border px-4 py-2 hover:bg-gray-50">Delete Movie</button>
            <button className="rounded-xl border px-4 py-2 hover:bg-gray-50">Update Movie Information</button>
            <button className="rounded-xl border px-4 py-2 hover:bg-gray-50">Add Promotions</button>

            <div className="ml-auto w-full sm:w-auto">
              <SearchFilterBar />
            </div>
          </div>
        </section>

        {/* Featured trailer (optional) */}
        {featuredTrailer ? (
          <section className="space-y-3">
            <h2 className="text-lg font-medium">Featured Trailer</h2>
            <Trailer url={featuredTrailer} />
          </section>
        ) : null}

        {/* Running */}
        <section className="space-y-4">
          <h2 className="text-xl font-semibold">Currently Running</h2>
          {loading ? (
            <div className="flex justify-center items-center h-64">
              <p className="text-lg opacity-70">Loading movies...</p>
            </div>
          ) : error ? (
            <div className="flex justify-center items-center h-64">
              <p className="text-lg text-red-600">{error}</p>
            </div>
          ) : running.length ? (
            <div className="grid sm:grid-cols-2 md:grid-cols-3 gap-6">
              {running.map((m) => (
                <MovieCard key={m.id} movie={m} />
              ))}
            </div>
          ) : (
            <p className="opacity-70">No matches found.</p>
          )}
        </section>

        {/* Coming soon */}
        <section className="space-y-4">
          <h2 className="text-xl font-semibold">Coming Soon</h2>
          {loading ? (
            <div className="flex justify-center items-center h-64">
              <p className="text-lg opacity-70">Loading movies...</p>
            </div>
          ) : error ? (
            <div className="flex justify-center items-center h-64">
              <p className="text-lg text-red-600">{error}</p>
            </div>
          ) : coming.length ? (
            <div className="grid sm:grid-cols-2 md:grid-cols-3 gap-6">
              {coming.map((m) => (
                <MovieCard key={m.id} movie={m} />
              ))}
            </div>
          ) : (
            <p className="opacity-70">No matches found.</p>
          )}
        </section>
      </main>
    </>
  );
}


