import type { Movie } from "@/types/cinema";

// Backend API base URL
const API_BASE_URL = 'http://localhost:8080/api';

// Transform backend movie data to frontend Movie type
function transformBackendMovie(backendMovie: any, index: number): Movie {
  // Ensure unique ID by combining movie_id with index as fallback
  const uniqueId = backendMovie.movie_id?.toString() || 
                   backendMovie.id?.toString() || 
                   `movie-${index}`;
  
  // Handle showtimes: convert string to array if needed
  let showtimes: string[];
  if (typeof backendMovie.showtimes === 'string') {
    // Backend returns comma-separated string like "2:00 PM, 5:00 PM, 8:00 PM"
    showtimes = backendMovie.showtimes
      .split(',')
      .map((time: string) => time.trim())
      .filter((time: string) => time.length > 0);
  } else if (Array.isArray(backendMovie.showtimes)) {
    // Already an array
    showtimes = backendMovie.showtimes;
  } else {
    // Fallback if no showtimes
    showtimes = ["TBA"];
  }
  
  return {
    id: uniqueId,
    title: backendMovie.title || "Unknown Movie",
    description: backendMovie.movieDescription || backendMovie.description || "No description available",
    posterUrl: backendMovie.posterUrl || backendMovie.poster_url || "/posters/default.jpg",
    rating: backendMovie.rating as "G" | "PG" | "PG-13" | "R",
    durationMins: backendMovie.duration ? parseDuration(backendMovie.duration) : 120,
    genres: [backendMovie.genre || "Drama"],
    status: backendMovie.isCurrentlyShowing || backendMovie.currentlyShowing || backendMovie.currently_showing ? "RUNNING" : "COMING_SOON",
    trailerUrl: backendMovie.trailerUrl || backendMovie.trailer_url,
    showtimes: showtimes
  };
}

// Parse duration string like "2:28" to minutes
function parseDuration(duration: string): number {
  const parts = duration.split(':');
  if (parts.length === 2) {
    return parseInt(parts[0]) * 60 + parseInt(parts[1]);
  }
  return 120; // default 2 hours
}

// Fetch all movies from backend
export async function getMovies(): Promise<Movie[]> {
  try {
    const response = await fetch(`${API_BASE_URL}/movies`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const backendMovies = await response.json();
    return backendMovies.map((movie: any, index: number) => transformBackendMovie(movie, index));
  } catch (error) {
    console.error('Error fetching movies:', error);
    return getFallbackMovies(); // Return fallback data if API fails
  }
}

// Search movies by title
export async function searchMovies(title: string): Promise<Movie[]> {
  try {
    const response = await fetch(`${API_BASE_URL}/movies/search?title=${encodeURIComponent(title)}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const backendMovies = await response.json();
    return backendMovies.map((movie: any, index: number) => transformBackendMovie(movie, index));
  } catch (error) {
    console.error('Error searching movies:', error);
    return [];
  }
}

// Filter movies by genre
export async function filterMoviesByGenre(genre: string): Promise<Movie[]> {
  try {
    const response = await fetch(`${API_BASE_URL}/movies/filter?genre=${encodeURIComponent(genre)}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const backendMovies = await response.json();
    return backendMovies.map((movie: any, index: number) => transformBackendMovie(movie, index));
  } catch (error) {
    console.error('Error filtering movies:', error);
    return [];
  }
}

// Get all available genres
export async function getGenres(): Promise<string[]> {
  try {
    const response = await fetch(`${API_BASE_URL}/genres`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.error('Error fetching genres:', error);
    return ["Action", "Comedy", "Crime", "Drama", "Sci-Fi"]; // fallback genres
  }
}

// Get a single movie by ID
export async function getMovieById(id: string): Promise<Movie | null> {
  try {
    // Extract numeric ID from string ID (e.g., "movie-27" -> 27, "movie-0" -> 1)
    let numericId: number;
    if (id.startsWith('movie-')) {
      const extracted = parseInt(id.replace('movie-', ''));
      // Since frontend uses 0-based index but database uses 1-based IDs
      numericId = extracted + 1;
    } else {
      numericId = parseInt(id);
    }
    
    // Validate the numeric ID
    if (isNaN(numericId) || numericId < 1) {
      return null;
    }
    
    const response = await fetch(`${API_BASE_URL}/movies/${numericId}`);
    if (!response.ok) {
      if (response.status === 404) {
        return null; // Movie not found
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const backendMovie = await response.json();
    return transformBackendMovie(backendMovie, 0);
  } catch (error) {
    console.error('Error fetching movie by ID:', error);
    return null;
  }
}

// Fallback movies if API is not available (for development)
function getFallbackMovies(): Movie[] {
  return [
    {
      id: "m1",
      title: "The Glass Planet",
      description: "An engineer uncovers a mysterious signal that could save—or doom—humanity.",
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
      description: "A road-trip comedy about friends, playlists, and second chances.",
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
      description: "A detective returns home to confront the case that changed everything.",
      posterUrl: "/posters/midnight-harbor.jpg",
      rating: "R",
      durationMins: 115,
      genres: ["Crime", "Drama"],
      status: "COMING_SOON",
      trailerUrl: "https://www.youtube.com/watch?v=aqz-KE-bpKQ",
      showtimes: ["TBA"],
    },
  ];
}

// For backward compatibility - this will be loaded asynchronously now
export const MOVIES: Movie[] = [];

// Export async version for getting all genres
export const getAllGenres = getGenres;
