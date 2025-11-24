export interface Showroom {
  showroomId: number;
  name: string;
  seatCount: number;
  numOfRows: number;
  numOfCols: number;
  theatreId: string;
  showtimeId: number;  // Added to support availability checking and booking
}

const API_BASE = process.env.NEXT_PUBLIC_API_BASE?.replace(/\/$/, "") || "";

export async function getSeats(
  id: string,
  showtimeParam: string
): Promise<Showroom | null> {
  try {
    const res = await fetch(
      `${API_BASE}/api/auth/seats/${id}/${showtimeParam}`, ///api
      {
        method: "GET",
        credentials: "include",
        cache: "no-store",
      }
    );

    const data = await res.json();

    /*
    if (!res.ok || !data.success || !Array.isArray(data.bookedSeats)) {
      console.error("Backend error in getSeatAvailability:", data);
      return new Set();
    }
      */

    if (!res.ok) {
      console.error("Backend error1 in getSeats:", data);
      return null;
    }

    if (!data.success) {
      console.error("Backend error2 in getSeats:", data);
      return null;
    }

    /*
    if (!Array.isArray(data.bookedSeats)) {
      console.error("Backend error3 in getSeats:", data);
      return null;
    }
      */

    const s = data.showroom;

    return {
      showroomId: Number(s.showroomId),
      name: s.name,
      seatCount: Number(s.seatCount),
      numOfRows: Number(s.numOfRows),
      numOfCols: Number(s.numOfCols),
      theatreId: String(s.theatreId),
      showtimeId: Number(s.showtimeId),
    };
  } catch (err) {
    console.error("Error fetching seats:", err);
    return null;
  }
}

export type SeatId = string;

export async function getSeatAvailability(
  movieId: string, 
  showtimeParam: string
): Promise<Set<SeatId>> {
  try {
    const res = await fetch(
      `${API_BASE}/api/auth/availability/${movieId}/${showtimeParam}`, ///api
      {
        method: "GET",
        credentials: "include",
        cache: "no-store",
      }
    );

    const data = await res.json();

    /*
    if (!res.ok || !data.success || !Array.isArray(data.bookedSeats)) {
      console.error("Backend error in getSeatAvailability:", data);
      return new Set();
    }
      */

    if (!res.ok) {
      console.error("Backend error1 in getSeatAvailability:", data);
      return new Set();
    }

    if (!data.success) {
      console.error("Backend error2 in getSeatAvailability:", data);
      return new Set();
    }

    if (!Array.isArray(data.bookedSeats)) {
      console.error("Backend error3 in getSeatAvailability:", data);
      return new Set();
    }

    return new Set<SeatId>(data.bookedSeats as string[]);
  } catch (err) {
    console.error("Error fetching seat availability:", err);
    return new Set();
  }
}

