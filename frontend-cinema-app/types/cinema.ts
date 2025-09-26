export type MovieStatus = "RUNNING" | "COMING_SOON";

export type Movie = {
  id: string;
  title: string;
  description: string;
  posterUrl?: string;
  rating?: "G" | "PG" | "PG-13" | "R";
  durationMins?: number;
  genres: string[];
  status: MovieStatus;
  trailerUrl?: string;        // e.g., https://www.youtube.com/watch?v=XXXXX
  showtimes: string[];        // e.g., ["2:00 PM","5:00 PM","8:00 PM"]
};
