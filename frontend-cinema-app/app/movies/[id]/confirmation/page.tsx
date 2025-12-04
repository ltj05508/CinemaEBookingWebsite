'use client';

import React, { useEffect, useState, use as reactUse } from 'react';
import Link from 'next/link';
import { useSearchParams, notFound } from 'next/navigation';
import { getBooking } from '@/lib/bookingApi';

type Params = { id: string };
type Booking = {
  bookingId: number;
  bookingDate: string;
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
    getBooking(bookingId)
      .then((data) => {
        if (!data?.booking) throw new Error('No booking found');
        setBooking(data.booking as Booking);
        setError(null);
      })
      .catch((err: any) => {
        console.error('Load booking failed', err);
        const msg =
          typeof err?.message === 'string' && err.message
            ? err.message
            : 'Unable to load booking details. Please sign in and try again.';
        setError(msg);
      });
  }, [bookingId]);

  if (!movieId) return notFound();

  return (
    <div className="max-w-3xl mx-auto p-6 space-y-6 bg-gray-50 min-h-screen text-gray-900">
      <div className="flex items-center gap-3 text-sm">
        <Link href={`/movies/${movieId}`} className="hover:underline">
          ← Back to movie
        </Link>
        {bookingId ? <span>Booking #{bookingId}</span> : null}
      </div>

      <div className="space-y-2">
        <div className="flex items-center gap-2">
          <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
          <h1 className="text-2xl font-bold">Order Confirmed!</h1>
        </div>
        {error && <p className="text-red-600 text-sm">{error}</p>}
        {!error && !booking && <p>Loading booking details…</p>}
        {!error && booking && (
          <p className="text-gray-600">
            Your booking has been confirmed. A confirmation email has been sent to your email address.
          </p>
        )}
      </div>

      {booking && (
        <div className="space-y-4 border rounded-lg p-6 bg-white shadow-sm">
          <div className="flex justify-between pb-3 border-b">
            <span className="text-gray-600">Booking ID</span>
            <span className="font-semibold">#{booking.bookingId}</span>
          </div>
          
          <div className="flex justify-between pb-3 border-b">
            <span className="text-gray-600">Booking Date</span>
            <span className="font-semibold">
              {booking.bookingDate ? new Date(booking.bookingDate).toLocaleString('en-US', {
                year: 'numeric',
                month: 'numeric',
                day: 'numeric',
                hour: 'numeric',
                minute: '2-digit',
                hour12: true
              }) : '—'}
            </span>
          </div>
          
          <div className="flex justify-between pb-3 border-b">
            <span className="text-gray-600">Status</span>
            <span className={`font-semibold ${booking.status === 'Confirmed' ? 'text-green-600' : ''}`}>
              {booking.status}
            </span>
          </div>
          
          <div className="space-y-2 pb-3 border-b">
            <div className="font-semibold">Tickets</div>
            <ul className="space-y-2">
              {(booking.tickets || []).map((t, idx) => (
                <li key={`${t.seatId}-${t.type}-${idx}`} className="flex justify-between text-sm">
                  <span>
                    Seat <span className="font-semibold">{t.seatId}</span> — {t.type.charAt(0).toUpperCase() + t.type.slice(1)}
                  </span>
                  <span>${t.price?.toFixed(2) ?? '—'}</span>
                </li>
              ))}
            </ul>
          </div>
          
          <div className="flex justify-between text-lg font-bold">
            <span>Total</span>
            <span className="text-green-600">${booking.totalPrice?.toFixed(2) ?? '—'}</span>
          </div>
        </div>
      )}

      {booking && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 space-y-2">
          <h2 className="font-semibold text-blue-900">Important Information</h2>
          <ul className="text-sm text-blue-800 space-y-1 list-disc list-inside">
            <li>Please arrive at least 15 minutes before showtime</li>
            <li>Show your booking ID at the box office to collect your tickets</li>
            <li>A confirmation email has been sent to your email address</li>
          </ul>
        </div>
      )}

      <div className="flex gap-3">
        <Link
          href={`/movies/${movieId}`}
          className="px-4 py-2 rounded-lg border hover:bg-gray-50"
        >
          Back to Movie
        </Link>
        <Link
          href="/orders"
          className="px-4 py-2 rounded-lg bg-black text-white hover:bg-gray-800"
        >
          View Order History
        </Link>
      </div>
    </div>
  );
}

