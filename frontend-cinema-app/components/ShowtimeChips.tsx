"use client";

import { useRouter } from "next/navigation";

export default function ShowtimeChips({
  movieId,
  showtimes,
}: {
  movieId: string;
  showtimes: string[];
}) {
  const router = useRouter();

  return (
    <div className="space-y-2 relative z-50 pointer-events-auto">
      <h2 className="font-semibold">Showtimes</h2>
      <div className="flex flex-wrap gap-2">
        {showtimes.map((t) => (
          <button
            key={t}
            type="button"
            onClick={() =>
              router.push(`/movies/${movieId}/book/${encodeURIComponent(t)}`)
            }
            className="text-sm rounded-lg border px-3 py-1 bg-white hover:bg-gray-50 cursor-pointer"
          >
            {t}
          </button>
        ))}
      </div>
    </div>
  );
}
