'use client';

import React, { useEffect, useState, use as reactUse } from 'react';
import Link from 'next/link';
import { useSearchParams, notFound } from 'next/navigation';

type Params = { id: string };
type Booking = {
  bookingId: number;
  status: string;
  totalPrice: number;
  tickets?: { seatId: string; type: string; price: number }[];
};

export default function ConfirmationPage({ params }: { params?: Promise<Params> }) {
  const { id: movieId } = reactUse((params ?? Promise.resolve({ id: '' })) as Promise<Params>) as Params;
  const searchParams = useSearchParams();
  const bookingIdParam = searchParams?.get('bookingId') || '';
  const bookingId = bookingIdParam ? Number(bookingIdParam) : NaN;

  const [booking, setBooking] = useState<Booking | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!bookingId || Number.isNaN(bookingId)) {
      setError('Missing booking id');
      return;
    }
    fetch(`/api/booking/${bookingId}`, { credentials: 'include' })
      .then(async (res) => {
        if (!res.ok) throw new Error(await res.text());
        return res.json();
      })
      .then((data) => {
        if (!data?.booking) throw new Error('No booking found');
        setBooking(data.booking as Booking);
        setError(null);
      })
      .catch((err) => {
        console.error('Load booking failed', err);
        setError('Unable to load booking details. Please sign in and try again.');
      });
  }, [bookingId]);

  if (!movieId) return notFound();

  return (
    <div className="max-w-3xl mx-auto p-6 space-y-6">
      <div className="flex items-center gap-3 text-sm">
        <Link href={`/movies/${movieId}`} className="hover:underline">
          ← Back to movie
        </Link>
        {bookingId ? <span>Booking #{bookingId}</span> : null}
      </div>

      <div className="space-y-2">
        <h1 className="text-2xl font-bold">Order Confirmation</h1>
        {error && <p className="text-red-600 text-sm">{error}</p>}
        {!error && !booking && <p>Loading booking details…</p>}
      </div>

      {booking && (
        <div className="space-y-4 border rounded p-4 bg-white shadow-sm">
          <div className="flex justify-between">
            <span className="font-semibold">Status</span>
            <span>{booking.status}</span>
          </div>
          <div className="flex justify-between">
            <span className="font-semibold">Total</span>
            <span>${booking.totalPrice?.toFixed(2) ?? '—'}</span>
          </div>
          <div className="space-y-2">
            <div className="font-semibold">Tickets</div>
            <ul className="space-y-1 text-sm">
              {(booking.tickets || []).map((t) => (
                <li key={`${t.seatId}-${t.type}`} className="flex justify-between">
                  <span>
                    Seat {t.seatId} — {t.type}
                  </span>
                  <span>${t.price?.toFixed(2) ?? '—'}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </div>
  );
}
