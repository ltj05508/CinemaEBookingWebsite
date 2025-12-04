export type Ticket = { seatId: string; type: 'adult' | 'child' | 'senior' };

// Default to relative paths so cookies work via Next.js rewrite; allow override via NEXT_PUBLIC_API_BASE if desired.
const API_BASE = (process.env.NEXT_PUBLIC_API_BASE ?? '').replace(/\/$/, '');

const jsonFetch = async <T>(url: string, opts: RequestInit = {}) => {
  const res = await fetch(url, {
    ...opts,
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...(opts.headers || {}) },
  });
  if (!res.ok) throw new Error(await res.text());
  return (await res.json()) as T;
};

export const getSeats = (movieId: string, showtime: string) =>
  jsonFetch<{ showroom: any; bookedSeats: string[] }>(
    `${API_BASE}/api/auth/seats/${movieId}/${encodeURIComponent(showtime)}`
  );

export const getAvailability = (movieId: string, showtime: string) =>
  jsonFetch<{ bookedSeats: string[] }>(
    `${API_BASE}/api/auth/availability/${movieId}/${encodeURIComponent(showtime)}`
  );

export const getPrices = () =>
  jsonFetch<{ prices: Record<'adult' | 'child' | 'senior', number> }>(
    `${API_BASE}/api/auth/prices`
  );

  /*
export const postQuote = (body: {
  movieId: number;
  showtimeId: number;
  tickets: Ticket[];
  promoCode?: string;
}) =>
  jsonFetch<{ quote: { subtotal: number; discount: number; total: number } }>(
    `${API_BASE}/api/booking/quote`,
    {
      method: 'POST',
      credentials: "include",
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    }
  );
  */

  export const postQuote = async (body: {
    movieId: number;
    showtimeId: number;
    tickets: Ticket[];
    promoCode?: string;
  }) => {
    console.log('Sending request body:', body);
    console.log('Stringified body:', JSON.stringify(body));
  
    const response = await fetch(`${API_BASE}/api/booking/quote`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });
  
    console.log('Response status:', response.status);
  
    if (!response.ok) {
      const errorText = await response.text();
      console.error('Error response:', errorText);
      throw new Error(`Request failed with status ${response.status}: ${errorText}`);
    }
  
    const responseBody = await response.json();
    console.log('Response JSON:', responseBody);
    
    return responseBody;
  };
  
  

export const postBooking = (body: {
  movieId: number;
  showtimeId: number;
  tickets: Ticket[];
  promoCode?: string;
  cardId: string;
  billingAddressId?: string;
}) =>
  jsonFetch<{ bookingId: number }>(`${API_BASE}/api/booking/create`, {
    method: 'POST',
    body: JSON.stringify(body),
  });

export const getCards = () =>
  jsonFetch<{ cards: { cardId: string; cardNumber?: string; expirationDate?: string }[] }>(
    `${API_BASE}/api/profile/cards`
  );

export const getBooking = (bookingId: number) =>
  jsonFetch<{ booking: any }>(`${API_BASE}/api/booking/${bookingId}`);

export const addCard = (body: { cardNumber: string; expirationDate: string; billingAddressId?: string }) =>
  jsonFetch<{ cardId: string }>(`${API_BASE}/api/profile/cards`, {
    method: 'POST',
    body: JSON.stringify(body),
  });

export const deleteCard = (cardId: string) =>
  jsonFetch<{ success: boolean }>(`${API_BASE}/api/profile/cards/${cardId}`, {
    method: 'DELETE',
  });
