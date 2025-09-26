import type { Movie } from "@/types/cinema";

export const MOVIES: Movie[] = [
  {
    id: "m1",
    title: "The Glass Planet",
    description:
      "An engineer uncovers a mysterious signal that could save—or doom—humanity.",
    posterUrl: "/posters/glass-planet.jpg",
    rating: "PG-13",
    durationMins: 128,
    genres: ["Sci-Fi", "Thriller"],
    status: "RUNNING",
    trailerUrl: "https://www.youtube.com/watch?v=ysz5S6PUM-U",
    showtimes: ["2:00 PM", "5:00 PM", "8:00 PM"],
  },
  {
    id: "m2",
    title: "Laugh Tracks",
    description:
      "A road-trip comedy about friends, playlists, and second chances.",
    posterUrl: "/posters/laugh-tracks.jpg",
    rating: "PG",
    durationMins: 102,
    genres: ["Comedy"],
    status: "RUNNING",
    trailerUrl: "https://www.youtube.com/watch?v=jNQXAC9IVRw",
    showtimes: ["1:30 PM", "4:30 PM", "7:30 PM"],
  },
  {
    id: "m3",
    title: "Midnight Harbor",
    description:
      "A detective returns home to confront the case that changed everything.",
    posterUrl: "/posters/midnight-harbor.jpg",
    rating: "R",
    durationMins: 115,
    genres: ["Crime", "Drama"],
    status: "COMING_SOON",
    trailerUrl: "https://www.youtube.com/watch?v=aqz-KE-bpKQ",
    showtimes: ["TBA"],
  },
];

export const ALL_GENRES = Array.from(
  new Set(MOVIES.flatMap((m) => m.genres))
).sort();
