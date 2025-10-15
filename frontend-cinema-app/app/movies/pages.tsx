"use client";

import { getMovies, searchMovies, filterMoviesByGenre } from "@/lib/data";
import type { Movie } from "@/types/cinema";
import MovieCard from "@/components/MovieCard";
import SearchFilterBar from "@/components/SearchFilterBar";
import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function MoviesPage() {
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
        
        if (q && q.trim() && genre && genre !== "ALL") {
          // Search by title and filter by genre
          let searchResults = await searchMovies(q.trim());
          results = searchResults.filter(movie => movie.genres.includes(genre));
        } else if (q && q.trim()) {
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
