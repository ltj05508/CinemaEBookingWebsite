/*
// app/movies/[id]/book/[showtime]/page.tsx
"use client";

//import * as React from "react"; // ⬅️ add this
import { use, useMemo, useState, useEffect } from "react";
import Link from "next/link";
import { getMovieById } from "@/lib/data";
import { notFound } from "next/navigation";
import type { Movie } from "@/types/cinema";
import { getSeats } from "@/lib/bookingClient";
import type { Showroom } from "@/lib/bookingClient";

/*

type PageProps = {
  params: Promise<{ id: string; showtime: string }>;
};
*/
/*
type PageProps = {
  params: { id: string; showtime: string };
}

  // Now you can safely use id and showtime

/*
export default function BookingPage({ params }: PageProps) {
  const { id, showtime: rawShowtime } = params; // <-- FIXED
  const showtime = decodeURIComponent(rawShowtime);
}
  */
/*
type SeatId = string;

//Change below by calling getSeats


export default function BookingPage({ params }: PageProps) {
  // ⬇️ unwrap promised params on the client
  const { id, showtime: rawShowtime } = params;
  const showtime = decodeURIComponent(rawShowtime);

  //const { id, showtime } = params;
  //const seats: Showroom | null = getSeats(id, showtime);

  /*
  if (!seats) {
    return <div>No seats available or failed to fetch seats.</div>;
  }
    */
  /*
  const [movie, setMovie] = useState<Movie | null>(null);
  const [loadingMovie, setLoadingMovie] = useState(true);
  const [loadingShowroom, setLoadingShowroom] = useState(true);
  const [selected, setSelected] = useState<Set<SeatId>>(new Set());

  const [showroom, setShowroom] = useState<Showroom | null>(null);
  //const [loadingSeats, setLoadingSeats] = useState(true);

  const ROWS = useMemo(() => {
    if (!showroom) return [];
    return Array.from({ length: showroom.numOfRows }, (_, i) =>
    String.fromCharCode(65 + i)
  );
  }, [showroom]);                                    
  const COLS = showroom?.numOfCols ?? 0;                 
  const PRICE_PER_SEAT = 12.0;

  
  useEffect(() => {
    async function fetchSeats() {
      try {
        const room = await getSeats(id, showtime);
        setShowroom(room);
      } finally {
        setLoadingShowroom(false);
      }
    }
    fetchSeats();
  }, []);
  

  useEffect(() => {
    async function fetchMovie() {
      try {
        const movieData = await getMovieById(id);
        setMovie(movieData);
      } finally {
        setLoadingMovie(false);
      }
    }
    fetchMovie();
  }, [id]);
  

  // Always call useMemo, even if movie is null (to maintain hook order)
  const reservedSeats = useMemo(() => {
    if (!movie || !showroom) return new Set<SeatId>();
    
    const ROWS_LOCAL = Array.from({ length: showroom.numOfRows }, (_, i) =>
      String.fromCharCode(65 + i)
    );
  
    const COLS_LOCAL = showroom.numOfCols;
    const totalSeats = ROWS_LOCAL.length * COLS_LOCAL;
  
    if (totalSeats === 0) return new Set<SeatId>();
  
    const base = (movie.id + "|" + showtime)
      .split("")
      .reduce((acc, ch) => acc + ch.charCodeAt(0), 0);
  
    const picks: SeatId[] = [];
    const target = Math.max(6, Math.floor(totalSeats * 0.1));
  
    let n = base;
    while (picks.length < target) {
      n = (n * 1103515245 + 12345) & 0x7fffffff;
      const idx = n % totalSeats;
      const r = Math.floor(idx / COLS_LOCAL);
      const c = (idx % COLS_LOCAL) + 1;
      const seat: SeatId = `${ROWS_LOCAL[r]}${c}`;
      if (!picks.includes(seat)) picks.push(seat);
    }
  
    return new Set(picks);
  }, [movie?.id, showtime, showroom]);

  if (loadingMovie || loadingShowroom) {
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
          <h1 className="text-2xl font-semibold">{id}</h1>
          <p className="opacity-70">
            {movie.title} • {showtime}
          </p>
        </header>

        {/* Screen visual *//*}
/*
        <div className="relative h-10">
          <div className="absolute left-0 right-0 top-1/2 -translate-y-1/2 mx-auto w-3/4 h-1 rounded-full bg-gray-300" />
          <p className="text-center text-xs mt-6 opacity-70">SCREEN</p>
        </div>

        {/* Legend *//*}
/*
        <div className="flex items-center gap-4 text-sm">
          <LegendSwatch className="border" label="Available" />
          <LegendSwatch className="bg-gray-300" label="Reserved" />
          <LegendSwatch className="bg-black text-white" label="Selected" />
        </div>

        {/* Seat grid *//*}
/*
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

      {/* Summary *//*}
/*
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
*/
"use client";

import { use, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { getMovieById } from "@/lib/data";
import { notFound } from "next/navigation";
import type { Movie } from "@/types/cinema";
import {
  getSeats,
  getSeatAvailability,
  SeatId,
} from "@/lib/bookingClient";
import type { Showroom } from "@/lib/bookingClient";

type PageProps = {
  params?: Promise<{ id: string; showtime: string }>;
};

export default function BookingPage({ params }: PageProps) {
  const { id, showtime: rawShowtime } = use((params ?? Promise.resolve({ id: "", showtime: "" })) as Promise<{ id: string; showtime: string }>) as { id: string; showtime: string };
  const showtime = decodeURIComponent(rawShowtime);

  /*
  const searchParams = useSearchParams();
  const showtimeIdParam = searchParams.get("showtimeId");
  const parsedShowtimeId = showtimeIdParam
    ? parseInt(showtimeIdParam, 10)
    : NaN;
  const showtimeId = Number.isNaN(parsedShowtimeId) ? null : parsedShowtimeId;
  */

  const [movie, setMovie] = useState<Movie | null>(null);
  const [loadingMovie, setLoadingMovie] = useState(true);
  const [loadingShowroom, setLoadingShowroom] = useState(true);
  const [loadingAvailability, setLoadingAvailability] = useState(true);
  const [selected, setSelected] = useState<Set<SeatId>>(new Set());
  const [showroom, setShowroom] = useState<Showroom | null>(null);
  const [bookedSeats, setBookedSeats] = useState<Set<SeatId>>(new Set());

  const ROWS = useMemo(() => {
    if (!showroom) return [];
    return Array.from({ length: showroom.numOfRows }, (_, i) =>
      String.fromCharCode(65 + i)
    );
  }, [showroom]);

  const COLS = showroom?.numOfCols ?? 0;
  const PRICE_PER_SEAT = 12.0;
  const reservedSeats = bookedSeats;
  //const regex = /[ABCDEFGHIJKLMNOPQRSTUVWXYZ]x\d+/g;
  





  useEffect(() => {
    async function fetchSeatsData() {
      try {
        const room = await getSeats(id, showtime);
        setShowroom(room);
      } finally {
        setLoadingShowroom(false);
      }
    }
    fetchSeatsData();
  }, [id, showtime]);

  useEffect(() => {
    async function fetchMovie() {
      try {
        const movieData = await getMovieById(id);
        setMovie(movieData);
      } finally {
        setLoadingMovie(false);
      }
    }
    fetchMovie();
  }, [id]);

  useEffect(() => {
    async function fetchAvailability() {
      /*
      if (!showtimeId) {
        setBookedSeats(new Set());
        setLoadingAvailability(false);
        return;
      }
        */
      try {
        const booked = await getSeatAvailability(id, showtime);
        setBookedSeats(booked);
      } finally {
        setLoadingAvailability(false);
      }
    }
    fetchAvailability();
  }, [id, showtime]);

  if (loadingMovie || loadingShowroom || loadingAvailability) {
    return <div className="p-8">Loading...</div>;
  }

  if (!movie) return notFound();

  if (!showroom) {
    return (
      <div className="p-8">
        <Link href={`/movies/${id}`} className="hover:underline text-sm">
          ← Back to {movie.title}
        </Link>
        <h1 className="mt-4 text-xl font-semibold">
          Seat map unavailable for this showtime.
        </h1>
        <h1 className="text-2xl font-semibold">{showtime}</h1>
        <p className="mt-2 text-sm opacity-70">
          We couldn&apos;t load a showroom for {movie.title} at {showtime}.
        </p>
      </div>
    );
  }

  const toggleSeat = (seat: SeatId) => {
    if (reservedSeats.has(seat)) return;
    const next = new Set(selected);
    if (next.has(seat)) {
      next.delete(seat);
    } else {
      //alert(`Proceeding with seats`);
      next.add(seat);
    }
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

        <div className="relative h-10">
          <div className="absolute left-0 right-0 top-1/2 -translate-y-1/2 mx-auto w-3/4 h-1 rounded-full bg-gray-300" />
          <p className="text-center text-xs mt-6 opacity-70">SCREEN</p>
        </div>

        <div className="flex items-center gap-4 text-sm">
          <LegendSwatch className="border" label="Available" />
          <LegendSwatch className="bg-gray-300" label="Reserved" />
          <LegendSwatch className="bg-black text-white" label="Selected" />
        </div>

        <div className="overflow-x-auto rounded-2xl border p-4 bg-white">
          <div className="inline-block">
            <div
              className="grid"
              style={{
                gridTemplateColumns: `auto repeat(${COLS}, minmax(2.25rem, auto))`,
              }}
            >
              <div />
              {Array.from({ length: COLS }, (_, i) => (
                <div
                  key={`h-${i}`}
                  className="text-xs text-center opacity-70"
                >
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
              <span>
                {selectedArray.length > 0
                  ? selectedArray.join(", ")
                  : "—"}
              </span>
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
            <li>Seat availability is loaded from the backend.</li>
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
        const seatId: SeatId = `${row}x${i + 1}`;
        const isReserved = reserved.has(seatId); //seatId
        const isSelected = selected.has(seatId);
        return (
          <button
            key={seatId}
            aria-label={`Seat ${seatId}${
              isReserved ? " (reserved)" : isSelected ? " (selected)" : ""
            }`}
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

function LegendSwatch({
  className,
  label,
}: {
  className?: string;
  label: string;
}) {
  return (
    <div className="flex items-center gap-2">
      <div className={`h-4 w-6 rounded ${className || ""}`} />
      <span className="text-sm opacity-80">{label}</span>
    </div>
  );
}

