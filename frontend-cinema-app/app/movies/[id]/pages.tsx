import { getMovieById } from "@/lib/data";
import Trailer from "@/components/Trailer";
import { notFound } from "next/navigation";
import ShowtimeChips from "@/components/ShowtimeChips";

export default async function MovieDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params; // ✅ unwrap promised params
  const movie = await getMovieById(id);
  if (!movie) return notFound();

  return (
    <div className="grid md:grid-cols-3 gap-8">
      <div className="md:col-span-2 space-y-4">
        <h1 className="text-3xl font-bold">{movie.title}</h1>

        {movie.posterUrl && (
          <img
            src={movie.posterUrl}
            alt={movie.title}
            className="rounded-xl w-full max-w-md"
          />
        )}

        <div className="text-sm opacity-70">
          {movie.rating && `${movie.rating} · `}
          {movie.durationMins && `${movie.durationMins} mins · `}
          {movie.genres.join(" • ")}
        </div>

        <p className="opacity-90">{movie.description}</p>

        {/* Showtimes (client component handles clicks) */}
        <ShowtimeChips movieId={movie.id} showtimes={movie.showtimes} />

        <div className="space-y-2">
          <h2 className="font-semibold">Trailer</h2>
          <Trailer url={movie.trailerUrl} />
        </div>
      </div>

      <aside className="space-y-3">
        <div className="rounded-2xl border p-4">
          <h3 className="font-medium mb-2">Quick Info</h3>
          <ul className="text-sm space-y-1">
            <li>
              Status: {movie.status === "RUNNING" ? "Now Showing" : "Coming Soon"}
            </li>
            {movie.durationMins && <li>Duration: {movie.durationMins} mins</li>}
            {movie.rating && <li>Rating: {movie.rating}</li>}
          </ul>
        </div>
      </aside>
    </div>
  );
}

