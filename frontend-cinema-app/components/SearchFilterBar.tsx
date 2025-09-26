"use client";

import { ALL_GENRES } from "@/lib/data";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useMemo, useState, useEffect } from "react";

export default function SearchFilterBar() {
  const router = useRouter();
  const pathname = usePathname();
  const sp = useSearchParams();

  const [q, setQ] = useState(sp.get("q") ?? "");
  const [genre, setGenre] = useState(sp.get("genre") ?? "ALL");

  // keep inputs in sync if user navigates back/forward
  useEffect(() => {
    setQ(sp.get("q") ?? "");
    setGenre(sp.get("genre") ?? "ALL");
  }, [sp]);

  const genres = useMemo(() => ["ALL", ...ALL_GENRES], []);

  const apply = () => {
    const params = new URLSearchParams(sp.toString());
    q ? params.set("q", q) : params.delete("q");
    genre && genre !== "ALL" ? params.set("genre", genre) : params.delete("genre");
    router.push(`${pathname}?${params.toString()}`);
  };

  return (
    <div className="flex flex-col sm:flex-row gap-3 sm:items-end">
      <label className="flex-1 text-sm">
        <div className="mb-1">Search by title</div>
        <input
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="e.g., Glass Planet"
          className="w-full rounded-xl border px-3 py-2"
        />
      </label>

      <label className="text-sm">
        <div className="mb-1">Genre</div>
        <select
          value={genre}
          onChange={(e) => setGenre(e.target.value)}
          className="rounded-xl border px-3 py-2"
        >
          {genres.map((g) => (
            <option key={g} value={g}>
              {g}
            </option>
          ))}
        </select>
      </label>

      <button
        onClick={apply}
        className="h-10 rounded-xl bg-black text-white px-4"
      >
        Apply
      </button>
    </div>
  );
}
