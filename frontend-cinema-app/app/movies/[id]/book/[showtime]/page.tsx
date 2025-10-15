// app/movies/[id]/book/[showtime]/page.tsx
"use client";

import * as React from "react"; // ⬅️ add this
import { useMemo, useState, useEffect } from "react";
import Link from "next/link";
import { getMovieById } from "@/lib/data";
import { notFound } from "next/navigation";
import type { Movie } from "@/types/cinema";

type PageProps = {
  // ⬇️ params is now a Promise in Next 15
  params: Promise<{ id: string; showtime: string }>;
};

type SeatId = string;

const ROWS = ["A", "B", "C", "D", "E", "F", "G", "H"];
const COLS = 12;
const PRICE_PER_SEAT = 12.0;

export default function BookingPage({ params }: PageProps) {
  // ⬇️ unwrap promised params on the client
  const { id, showtime: rawShowtime } = React.use(params);
  const showtime = decodeURIComponent(rawShowtime);
  
  const [movie, setMovie] = useState<Movie | null>(null);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<Set<SeatId>>(new Set());

  useEffect(() => {
    async function fetchMovie() {
      try {
        const movieData = await getMovieById(id);
        setMovie(movieData);
      } catch (error) {
        console.error('Error fetching movie:', error);
        setMovie(null);
      } finally {
        setLoading(false);
      }
    }
    fetchMovie();
  }, [id]);

  // Always call useMemo, even if movie is null (to maintain hook order)
  const reservedSeats = useMemo(() => {
    if (!movie) return new Set<SeatId>(); // Return empty set if no movie
    
    const base = (movie.id + "|" + showtime)
      .split("")
      .reduce((acc, ch) => acc + ch.charCodeAt(0), 0);
    const picks: SeatId[] = [];
    const totalSeats = ROWS.length * COLS;
    const target = Math.max(6, Math.floor(totalSeats * 0.1));

    let n = base;
    while (picks.length < target) {
      n = (n * 1103515245 + 12345) & 0x7fffffff;
      const idx = n % totalSeats;
      const r = Math.floor(idx / COLS);
      const c = (idx % COLS) + 1;
      const seat: SeatId = `${ROWS[r]}${c}`;
      if (!picks.includes(seat)) picks.push(seat);
    }
    return new Set(picks);
  }, [movie?.id, showtime]);

  if (loading) {
    return <div className="p-8">Loading...</div>;
  }
  
  if (!movie) return notFound();

  const toggleSeat = (seat: SeatId) => {
    if (reservedSeats.has(seat)) return;
    const next = new Set(selected);
    if (next.has(seat)) next.delete(seat);
    else next.add(seat);
    setSelected(next);
  };

  const selectedArray = Array.from(selected).sort((a, b) => {
    const ra = ROWS.indexOf(a[0]);
    const rb = ROWS.indexOf(b[0]);
    if (ra !== rb) return ra - rb;
    return parseInt(a.slice(1)) - parseInt(b.slice(1));
  });

  const total = (selectedArray.length * PRICE_PER_SEAT).toFixed(2);

  return (
    <div className="grid gap-8 md:grid-cols-3">
      <div className="md:col-span-2 space-y-6">
        <div className="flex items-center gap-2 text-sm opacity-70">
          <Link href={`/movies/${id}`} className="hover:underline">
            ← Back to {movie.title}
          </Link>
          <span>•</span>
          <span>{showtime}</span>
        </div>

        <header className="space-y-1">
          <h1 className="text-2xl font-semibold">Select your seats</h1>
          <p className="opacity-70">
            {movie.title} • {showtime}
          </p>
        </header>

        {/* Screen visual */}
        <div className="relative h-10">
          <div className="absolute left-0 right-0 top-1/2 -translate-y-1/2 mx-auto w-3/4 h-1 rounded-full bg-gray-300" />
          <p className="text-center text-xs mt-6 opacity-70">SCREEN</p>
        </div>

        {/* Legend */}
        <div className="flex items-center gap-4 text-sm">
          <LegendSwatch className="border" label="Available" />
          <LegendSwatch className="bg-gray-300" label="Reserved" />
          <LegendSwatch className="bg-black text-white" label="Selected" />
        </div>

        {/* Seat grid */}
        <div className="overflow-x-auto rounded-2xl border p-4 bg-white">
          <div className="inline-block">
            <div className="grid" style={{ gridTemplateColumns: `auto repeat(${COLS}, minmax(2.25rem, auto))` }}>
              <div />
              {Array.from({ length: COLS }, (_, i) => (
                <div key={`h-${i}`} className="text-xs text-center opacity-70">
                  {i + 1}
                </div>
              ))}

              {ROWS.map((row) => (
                <Row
                  key={row}
                  row={row}
                  cols={COLS}
                  reserved={reservedSeats}
                  selected={selected}
                  onToggle={toggleSeat}
                />
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Summary */}
      <aside className="space-y-4">
        <div className="rounded-2xl border p-4">
          <h2 className="font-semibold mb-2">Your selection</h2>
          <div className="text-sm space-y-1">
            <div className="flex items-center justify-between">
              <span className="opacity-70">Movie</span>
              <span>{movie.title}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="opacity-70">Showtime</span>
              <span>{showtime}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="opacity-70">Seats</span>
              <span>{selectedArray.length > 0 ? selectedArray.join(", ") : "—"}</span>
            </div>
            <div className="flex items-center justify-between pt-2 border-t mt-2">
              <span className="font-medium">Total</span>
              <span className="font-semibold">${total}</span>
            </div>
          </div>
          <button
            className="mt-4 w-full rounded-xl border px-4 py-2 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            disabled={selectedArray.length === 0}
            onClick={() => {
              alert(`Proceeding with seats: ${selectedArray.join(", ")}`);
            }}
          >
            Proceed
          </button>
        </div>

        <div className="rounded-2xl border p-4">
          <h3 className="font-medium mb-2">Notes</h3>
          <ul className="list-disc pl-5 text-sm space-y-1 opacity-80">
            <li>This is a UI prototype. Seat availability is simulated.</li>
            <li>Implementation (locking, payment, etc.) comes later.</li>
          </ul>
        </div>
      </aside>
    </div>
  );
}

function Row({
  row,
  cols,
  reserved,
  selected,
  onToggle,
}: {
  row: string;
  cols: number;
  reserved: Set<SeatId>;
  selected: Set<SeatId>;
  onToggle: (id: SeatId) => void;
}) {
  return (
    <>
      <div className="text-xs pr-2 flex items-center opacity-70">{row}</div>
      {Array.from({ length: cols }, (_, i) => {
        const seatId: SeatId = `${row}${i + 1}`;
        const isReserved = reserved.has(seatId);
        const isSelected = selected.has(seatId);
        return (
          <button
            key={seatId}
            aria-label={`Seat ${seatId}${isReserved ? " (reserved)" : isSelected ? " (selected)" : ""}`}
            onClick={() => onToggle(seatId)}
            disabled={isReserved}
            className={[
              "m-1 h-9 w-9 rounded-lg text-xs flex items-center justify-center select-none",
              "transition-colors",
              isReserved
                ? "bg-gray-300 cursor-not-allowed"
                : isSelected
                ? "bg-black text-white"
                : "border hover:bg-gray-50",
            ].join(" ")}
          >
            {i + 1}
          </button>
        );
      })}
    </>
  );
}

function LegendSwatch({ className, label }: { className?: string; label: string }) {
  return (
    <div className="flex items-center gap-2">
      <div className={`h-4 w-6 rounded ${className || ""}`} />
      <span className="text-sm opacity-80">{label}</span>
    </div>
  );
}
