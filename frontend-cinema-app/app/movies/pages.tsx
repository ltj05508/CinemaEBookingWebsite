"use client";

import { getMovies } from "@/lib/data";
import type { Movie } from "@/types/cinema";
import MovieCard from "@/components/MovieCard";
import SearchFilterBar from "@/components/SearchFilterBar";
import { useEffect, useState } from "react";

export default function MoviesPage() {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchMovies() {
      setLoading(true);
      setError(null);
      
      try {
        const results = await getMovies();
        setMovies(results);
      } catch (err) {
        setError('Failed to load movies. Please try again.');
        console.error('Error loading movies:', err);
      } finally {
        setLoading(false);
      }
    }

    fetchMovies();
  }, []);

  if (loading) {
    return (
      <section className="space-y-4">
        <h1 className="text-2xl font-semibold">All Movies</h1>
        <SearchFilterBar />
        <div className="flex justify-center items-center h-64">
          <p className="text-lg opacity-70">Loading movies...</p>
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section className="space-y-4">
        <h1 className="text-2xl font-semibold">All Movies</h1>
        <SearchFilterBar />
        <div className="flex justify-center items-center h-64">
          <p className="text-lg text-red-600">{error}</p>
        </div>
      </section>
    );
  }

  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-semibold">All Movies</h1>
      <SearchFilterBar />
      <div className="grid sm:grid-cols-2 md:grid-cols-3 gap-6">
        {movies.map((movie: Movie) => (
          <MovieCard key={movie.id} movie={movie} />
        ))}
      </div>
    </section>
  );
}
