"use client";

import { getMovies, searchMovies, filterMoviesByGenre } from "@/lib/data";
import type { Movie } from "@/types/cinema";
import MovieCard from "@/components/MovieCard";
import SearchFilterBar from "@/components/SearchFilterBar";
import Trailer from "@/components/Trailer";
import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function Home() {
  const sp = useSearchParams();
  const q = sp.get("q");
  const genre = sp.get("genre");
  
  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch movies based on search parameters
  useEffect(() => {
    async function fetchMovies() {
      setLoading(true);
      setError(null);
      
      try {
        let results: Movie[];
        
        if (q && q.trim()) {
          // Search by title
          results = await searchMovies(q.trim());
        } else if (genre && genre !== "ALL") {
          // Filter by genre
          results = await filterMoviesByGenre(genre);
        } else {
          // Get all movies
          results = await getMovies();
        }
        
        setMovies(results);
      } catch (err) {
        setError('Failed to load movies. Please try again.');
        console.error('Error loading movies:', err);
      } finally {
        setLoading(false);
      }
    }

    fetchMovies();
  }, [q, genre]);

  const running = movies.filter((m: Movie) => m.status === "RUNNING");
  const coming = movies.filter((m: Movie) => m.status === "COMING_SOON");

  // pick first running trailer to feature on home (optional)
  const featuredTrailer = running[0]?.trailerUrl;

  if (loading) {
    return (
      <div className="space-y-8">
        <section className="space-y-3">
          <h1 className="text-2xl font-semibold">Movies</h1>
          <SearchFilterBar />
        </section>
        <div className="flex justify-center items-center h-64">
          <p className="text-lg opacity-70">Loading movies...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-8">
        <section className="space-y-3">
          <h1 className="text-2xl font-semibold">Movies</h1>
          <SearchFilterBar />
        </section>
        <div className="flex justify-center items-center h-64">
          <p className="text-lg text-red-600">{error}</p>
        </div>
      </div>
    );
  }

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
            {running.map((m: Movie) => (
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
            {coming.map((m: Movie) => (
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
