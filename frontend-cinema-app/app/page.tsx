"use client";

import { MOVIES } from "@/lib/data";
import type { Movie } from "@/types/cinema";
import MovieCard from "@/components/MovieCard";
import SearchFilterBar from "@/components/SearchFilterBar";
import Trailer from "@/components/Trailer";
import { useSearchParams } from "next/navigation";
import { useMemo } from "react";

function filterMovies(all: Movie[], q?: string | null, genre?: string | null) {
  let out = all;
  if (q && q.trim()) {
    const s = q.trim().toLowerCase();
    out = out.filter((m) => m.title.toLowerCase().includes(s));
  }
  if (genre && genre !== "ALL") {
    out = out.filter((m) => m.genres.includes(genre));
  }
  return out;
}

export default function Home() {
  const sp = useSearchParams();
  const q = sp.get("q");
  const genre = sp.get("genre");

  const filtered = useMemo(() => filterMovies(MOVIES, q, genre), [q, genre]);
  const running = filtered.filter((m) => m.status === "RUNNING");
  const coming = filtered.filter((m) => m.status === "COMING_SOON");

  // pick first running trailer to feature on home (optional)
  const featuredTrailer = running[0]?.trailerUrl;

  return (
    <div className="space-y-8">
      <section className="space-y-3">
        <h1 className="text-2xl font-semibold">Movies</h1>
        <SearchFilterBar />
      </section>

      {featuredTrailer ? (
        <section className="space-y-3">
          <h2 className="text-lg font-medium">Featured Trailer</h2>
          <Trailer url={featuredTrailer} />
        </section>
      ) : null}

      <section className="space-y-4">
        <h2 className="text-xl font-semibold">Currently Running</h2>
        {running.length ? (
          <div className="grid sm:grid-cols-2 md:grid-cols-3 gap-6">
            {running.map((m) => (
              <MovieCard key={m.id} movie={m} />
            ))}
          </div>
        ) : (
          <p className="opacity-70">No matches found.</p>
        )}
      </section>

      <section className="space-y-4">
        <h2 className="text-xl font-semibold">Coming Soon</h2>
        {coming.length ? (
          <div className="grid sm:grid-cols-2 md:grid-cols-3 gap-6">
            {coming.map((m) => (
              <MovieCard key={m.id} movie={m} />
            ))}
          </div>
        ) : (
          <p className="opacity-70">No matches found.</p>
        )}
      </section>
    </div>
  );
}
