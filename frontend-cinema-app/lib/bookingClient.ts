const API_BASE = process.env.NEXT_PUBLIC_API_BASE?.replace(/\/$/, "") || "";

export async function getSeats(body: { movieId: string; showtime: string}) {
  const res = await fetch(`${API_BASE}/api/booking/seats`, {
    method: "GET",
    credentials: "include",
    cache: "no-store", // ensure fresh status (avoid stale UI)
  });

  if (!res.ok) throw new Error("Failed to retrieve seats!");
  return res.json();

}