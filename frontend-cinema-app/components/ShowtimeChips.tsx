/*
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
*/ 
/*
"use client";

import { useRouter } from "next/navigation";

export default function ShowtimeChips({
  movieId,
  showtimes,
}: {
  movieId: string;
  showtimes: { time: string; showtimeId: number }[];
}) {
  const router = useRouter();

  return (
    <div className="space-y-2 relative z-50 pointer-events-auto">
      <h2 className="font-semibold">Showtimes</h2>
      <div className="flex flex-wrap gap-2">
        {showtimes.map((s) => (
          <button
            key={s.showtimeId}
            type="button"
            onClick={() =>
              router.push(
                `/movies/${movieId}/book/${encodeURIComponent(s.time)}?showtimeId=${s.showtimeId}`
              )
            }
            className="text-sm rounded-lg border px-3 py-1 bg-white hover:bg-gray-50 cursor-pointer"
          >
            {s.time}
          </button>
        ))}
      </div>
    </div>
  );
}
  */

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
            className="text-sm font-semibold text-gray-900 rounded-lg border border-gray-300 px-3 py-1 bg-white hover:bg-gray-100 shadow-sm"
          >
            {t}
          </button>
        ))}
      </div>
    </div>
  );
}

