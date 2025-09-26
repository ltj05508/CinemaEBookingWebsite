import { MOVIES } from "@/lib/data";
import Trailer from "@/components/Trailer";
import { notFound } from "next/navigation";

export default function MovieDetailPage({ params }: { params: { id: string } }) {
  const movie = MOVIES.find((m) => m.id === params.id);
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

        <div className="space-y-2">
          <h2 className="font-semibold">Showtimes</h2>
          <div className="flex flex-wrap gap-2">
            {movie.showtimes.map((t) => (
              <span
                key={t}
                className="text-sm rounded-lg border px-3 py-1 bg-white"
              >
                {t}
              </span>
            ))}
          </div>
        </div>

        <div className="space-y-2">
          <h2 className="font-semibold">Trailer</h2>
          <Trailer url={movie.trailerUrl} />
        </div>
      </div>

      <aside className="space-y-3">
        <div className="rounded-2xl border p-4">
          <h3 className="font-medium mb-2">Quick Info</h3>
          <ul className="text-sm space-y-1">
            <li>Status: {movie.status === "RUNNING" ? "Now Showing" : "Coming Soon"}</li>
            {movie.durationMins && <li>Duration: {movie.durationMins} mins</li>}
            {movie.rating && <li>Rating: {movie.rating}</li>}
          </ul>
        </div>
      </aside>
    </div>
  );
}
