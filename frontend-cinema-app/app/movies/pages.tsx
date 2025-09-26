import { MOVIES } from "@/lib/data";
import MovieCard from "@/components/MovieCard";
import SearchFilterBar from "@/components/SearchFilterBar";

export default function MoviesPage() {
  // Later you could use search params here (or just rely on Home page search)
  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-semibold">All Movies</h1>
      <SearchFilterBar />
      <div className="grid sm:grid-cols-2 md:grid-cols-3 gap-6">
        {MOVIES.map((movie) => (
          <MovieCard key={movie.id} movie={movie} />
        ))}
      </div>
    </section>
  );
}
