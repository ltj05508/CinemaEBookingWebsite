import Link from "next/link";
import type { Movie } from "@/types/cinema";

export default function MovieCard({ movie }: { movie: Movie }) {
  return (
    <div className="rounded-2xl border shadow-sm overflow-hidden">
      <div className="aspect-[2/3] bg-gray-100">
        {movie.posterUrl ? (
          <img
            src={movie.posterUrl}
            alt={movie.title}
            className="w-full h-full object-cover"
          />
        ) : null}
      </div>
      <div className="p-4 space-y-2">
        <div className="flex items-center justify-between gap-3">
          <h3 className="font-medium">{movie.title}</h3>
          {movie.rating ? (
            <span className="text-xs rounded-md border px-1.5 py-0.5">
              {movie.rating}
            </span>
          ) : null}
        </div>
        <p className="text-xs opacity-70 line-clamp-2">
          {movie.description}
        </p>
        <div className="text-xs opacity-70">{movie.genres.join(" • ")}</div>
        <div className="text-sm">Showtimes: {movie.showtimes.join(" · ")}</div>
        <Link
          className="inline-block text-sm underline mt-1"
          href={`/movies/${movie.id}`}
        >
          Details
        </Link>
      </div>
    </div>
  );
}
