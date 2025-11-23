
/*
export type Showroom = {showroomId: number; name: string; seatCount: number; numOfRows: number; numOfCols: number; theatreId: string};
const API_BASE = process.env.NEXT_PUBLIC_API_BASE?.replace(/\/$/, "") || "";




export async function getSeats(id: string, showtime: string) {   //Promise<Showroom | null> {
    try {
     const res = await fetch(`${API_BASE}/api/booking/seats?id=${id}&showtime=${showtime}`, {
       method: "GET",
       credentials: "include",
       cache: "no-store",
      });

  const activeShowroom = await res.json();
  if (activeShowroom.success && activeShowroom.showroom && activeShowroom.showroom.length > 0) {
    const data = activeShowroom.showroom[0];
    const showroom = {
      showroomId: data.showroomId || "",
      name: data.name || "",
      seatCount: data.seatCount || "",
      numOfRows: data.numOfRows || "",
      numOfCols: data.numOfCols || "",
      theatreId: data.theatreId || ""
    };
    return showroom;
  }
  } catch(error) {
    console.error('Error fetching profile:', error);
    return <Showroom>({showroomId: -1, name: "nope", seatCount: -1, numOfRows: -1, numOfCols: -1, theatreId: "nope"});
  }
  console.error('Mistake while fetching profile:');
  return <Showroom>({showroomId: -1, name: "nope", seatCount: -1, numOfRows: -1, numOfCols: -1, theatreId: "nope"});
}
  
*/

/*
export type Showroom = {
  showroomId: number;
  name: string;
  seatCount: number;
  numOfRows: number;
  numOfCols: number;
  theatreId: string;
};


const API_BASE = process.env.NEXT_PUBLIC_API_BASE?.replace(/\/$/, "") || "";

export async function getSeats(id: string, showtime: string) {   //Promise<Showroom | null> {
  try {
    const res = await fetch(`${API_BASE}/api/booking/seats/${id}/${showtime}`, {          
      method: "GET",
      //credentials: "include",
      cache: "no-store",
    });

    const data = await res.json();

    console.log(res.status, res.statusText);
    if (!res.ok) throw new Error("Failed to fetch seats");

    if (!data.success || !data.showroom) // || data.showroom.length === 0
      return null;

    const s = data.showroom;
    const result = {
      showroomId: Number(s.showroomId),
      name: s.name,
      seatCount: Number(s.seatCount),
      numOfRows: Number(s.numOfRows),     
      numOfCols: Number(s.numOfCols),     
      theatreId: s.theatreId
    };
    return result;
  } catch (err) {
    console.error("Error fetching seats:", err);
    return null;
  }
}
  */

export interface Showroom {
  showroomId: number;
  name: string;
  seatCount: number;
  numOfRows: number;
  numOfCols: number;
  theatreId: number;
}

const API_BASE = process.env.NEXT_PUBLIC_API_BASE?.replace(/\/$/, "") || "";

export async function getSeats(id: string, showtime: string): Promise<Showroom | null> {
  try {
    const res = await fetch(`${API_BASE}/api/booking/seats/${id}/${showtime}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
    });

    const data = await res.json();

    if (!res.ok || !data.success || !data.showroom) {
      console.error("Backend returned error:", data);
      return null;
    }

    const s = data.showroom;

    return {
      showroomId: Number(s.showroomId),
      name: s.name,
      seatCount: Number(s.seatCount),
      numOfRows: Number(s.numOfRows),
      numOfCols: Number(s.numOfCols),
      theatreId: s.theatreId,
    };
  } catch (err) {
    console.error("Error fetching seats:", err);
    return null;
  }
}
