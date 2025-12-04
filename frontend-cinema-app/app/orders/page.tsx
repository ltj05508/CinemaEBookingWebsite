'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getOrderHistory } from '@/lib/bookingApi';
import { getAuthStatus } from '@/lib/authClient';

type Booking = {
  bookingId: number;
  bookingDate: string;
  movieTitle?: string;
  showtime?: string;
  seats?: string[];
  ticketCount?: number;
  totalPrice: number;
  status: string;
  promoId?: string;
};

export default function OrderHistoryPage() {
  const router = useRouter();
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadOrders = async () => {
      try {
        // Check authentication
        const authStatus = await getAuthStatus();
        if (!authStatus?.loggedIn) {
          router.push('/login?redirect=/orders');
          return;
        }

        // Fetch order history
        const response = await getOrderHistory();
        setBookings(response.bookings || []);
      } catch (err: any) {
        setError(err.message || 'Failed to load order history');
      } finally {
        setLoading(false);
      }
    };

    loadOrders();
  }, [router]);

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto p-6">
        <div className="text-center py-12">
          <p className="text-gray-600">Loading your orders...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-5xl mx-auto p-6">
        <div className="text-center py-12">
          <p className="text-red-600">{error}</p>
          <button 
            onClick={() => router.push('/')}
            className="mt-4 px-4 py-2 bg-emerald-600 text-white rounded"
          >
            Go Home
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Order History</h1>
        <button 
          onClick={() => router.push('/')}
          className="text-sm text-gray-600 hover:text-gray-900"
        >
          ← Back to Home
        </button>
      </div>

      {bookings.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg border">
          <p className="text-gray-600 mb-4">You haven't made any bookings yet.</p>
          <button 
            onClick={() => router.push('/movies')}
            className="px-4 py-2 bg-emerald-600 text-white rounded"
          >
            Browse Movies
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {bookings.map((booking) => (
            <div 
              key={booking.bookingId}
              className="bg-white rounded-lg border shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <h2 className="text-xl font-semibold text-gray-900">
                      {booking.movieTitle || `Booking #${booking.bookingId}`}
                    </h2>
                    <span className={`px-2 py-1 text-xs rounded-full ${
                      booking.status === 'confirmed' || booking.status === 'active'
                        ? 'bg-green-100 text-green-800'
                        : booking.status === 'cancelled'
                        ? 'bg-red-100 text-red-800'
                        : 'bg-gray-100 text-gray-800'
                    }`}>
                      {booking.status.toUpperCase()}
                    </span>
                  </div>
                  
                  <div className="grid grid-cols-2 gap-4 text-sm text-gray-600 mb-3">
                    {booking.showtime && (
                      <div>
                        <span className="font-medium">Showtime:</span> {booking.showtime}
                      </div>
                    )}
                    <div>
                      <span className="font-medium">Booked:</span> {formatDate(booking.bookingDate)}
                    </div>
                    {booking.seats && booking.seats.length > 0 && (
                      <div>
                        <span className="font-medium">Seats:</span> {booking.seats.join(', ')}
                      </div>
                    )}
                    {booking.ticketCount && (
                      <div>
                        <span className="font-medium">Tickets:</span> {booking.ticketCount}
                      </div>
                    )}
                    <div>
                      <span className="font-medium">Total:</span> ${booking.totalPrice.toFixed(2)}
                    </div>
                  </div>

                  <div className="flex gap-2">
                    <button
                      onClick={() => router.push(`/movies/${booking.bookingId}/confirmation?bookingId=${booking.bookingId}`)}
                      className="text-sm text-emerald-600 hover:text-emerald-700 font-medium"
                    >
                      View Details →
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
