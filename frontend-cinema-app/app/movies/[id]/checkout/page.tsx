'use client';

import React, { useEffect, useMemo, useState, use as reactUse } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { PaymentMethodList } from '@/components/PaymentMethodList';
import { OrderSummary } from '@/components/OrderSummary';
import { TicketTypeSelector } from '@/components/TicketTypeSelector';
import { SeatMap } from '@/components/SeatMap';
import {
  getAvailability,
  getSeats,
  getPrices,
  getCards,
  postQuote,
  postBooking,
  Ticket,
} from '@/lib/bookingApi';

type Props = {
  params?: Promise<{ id: string }>;
};

const CheckoutPage: React.FC<Props> = ({ params }) => {
  const { id: movieId } = reactUse(
    (params ?? Promise.resolve({ id: '' })) as Promise<{ id: string }>
  ) as {
    id: string;
  };
  const router = useRouter();
  const searchParams = useSearchParams();
  const showtimeDisplayRaw = searchParams?.get('showtime') || '';
  const showtimeDisplay = showtimeDisplayRaw ? decodeURIComponent(showtimeDisplayRaw) : '';
  const initialShowtimeIdParam = searchParams?.get('showtimeId');
  const initialShowtimeId = initialShowtimeIdParam ? Number(initialShowtimeIdParam) : undefined;
  const seatsParamRaw = searchParams?.get('seats') || '';
  const seatsParam = seatsParamRaw ? decodeURIComponent(seatsParamRaw) : '';
  const initialSeatIds = useMemo(
    () => new Set(seatsParam ? seatsParam.split(',').filter(Boolean) : []),
    [seatsParam]
  );

  const [showtimeId, setShowtimeId] = useState<number | undefined>(initialShowtimeId);
  const [showroom, setShowroom] = useState<{ numOfRows: number; numOfCols: number } | null>(null);
  const [booked, setBooked] = useState<Set<string>>(new Set());
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [prices, setPrices] = useState<Record<'adult' | 'child' | 'senior', number> | null>(null);
  const [quote, setQuote] = useState({ subtotal: 0, discount: 0, total: 0 });
  const [promo, setPromo] = useState('');
  const [cards, setCards] = useState<{ cardId: string; cardNumber?: string; expirationDate?: string }[]>([]);
  const [selectedCard, setSelectedCard] = useState<string>();
  const [checkoutError, setCheckoutError] = useState<string | null>(null);

  // Hydrate seats passed in query
  useEffect(() => {
    if (initialSeatIds.size) {
      setSelected(new Set(initialSeatIds));
    }
  }, [initialSeatIds]);

  // Load availability, layout, prices, cards
  useEffect(() => {
    (async () => {
      let availabilityResp: any = { bookedSeats: [] };
      let seatsResp: any = null;

      if (showtimeDisplay) {
        availabilityResp = await getAvailability(movieId, showtimeDisplay).catch(() => ({ bookedSeats: [] }));
        seatsResp = await getSeats(movieId, showtimeDisplay).catch(() => null);
      }

      const pricesResp = await getPrices();
      const cardsResp = await getCards().catch(() => ({ cards: [] }));

      setBooked(new Set(availabilityResp.bookedSeats || []));
      setPrices(pricesResp.prices);
      setCards(cardsResp.cards || []);

      if (seatsResp?.showroom) {
        const sr = seatsResp.showroom;
        setShowroom({ numOfRows: sr.numOfRows ?? 10, numOfCols: sr.numOfCols ?? 12 });
        if (!showtimeId && sr.showtimeId) setShowtimeId(sr.showtimeId);
      } else {
        setShowroom({ numOfRows: 10, numOfCols: 12 });
      }
      if (!showtimeId && availabilityResp.showtimeId) setShowtimeId(Number(availabilityResp.showtimeId));
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [movieId, showtimeDisplay]);

  // Sync tickets with selected seats
  useEffect(() => {
    setTickets((prev) =>
      Array.from(selected).map((seatId) => ({
        seatId,
        type: (prev.find((t) => t.seatId === seatId)?.type || 'adult') as Ticket['type'],
      }))
    );
  }, [selected]);

  // Quote pricing
  useEffect(() => {
    const run = async () => {
      if (!tickets.length || !prices) return;
      const clientQuote = () => {
        const subtotal = tickets.reduce((sum, t) => sum + (prices[t.type] || prices.adult), 0);
        return { subtotal, discount: 0, total: subtotal };
      };
      setQuote(clientQuote());

      if (!showtimeId) return;
      try {
        const res = await postQuote({
          movieId: Number(movieId),
          showtimeId,
          tickets,
          promoCode: promo || undefined,
        });
        setQuote(res.quote);
      } catch {
        // stay on client quote
      }
    };
    run();
  }, [tickets, promo, prices, showtimeId, movieId]);

  const toggleSeat = (seatId: string) => {
    setSelected((prev) => {
      const next = new Set(prev);
      next.has(seatId) ? next.delete(seatId) : next.add(seatId);
      return next;
    });
  };

  const onCheckout = async () => {
    if (!selectedCard) {
      alert('Select a payment method');
      return;
    }
    if (!showtimeId) {
      alert('Missing showtime');
      return;
    }
    try {
      const res = await postBooking({
        movieId: Number(movieId),
        showtimeId,
        tickets,
        promoCode: promo || undefined,
        cardId: selectedCard,
      });
      setCheckoutError(null);
      router.push(`/movies/${movieId}/confirmation?bookingId=${res.bookingId}`);
    } catch (err: any) {
      const rawMessage = typeof err?.message === 'string' ? err.message : '';
      let friendly = 'Unable to complete checkout. Please try again.';

      // Try to surface backend error details when available
      try {
        const parsed = JSON.parse(rawMessage);
        if (parsed && typeof parsed.message === 'string') {
          friendly = parsed.message;
        }
      } catch {
        /* not JSON, fall through */
      }

      if (/forbidden|unauthorized|not authenticated/i.test(rawMessage)) {
        friendly = 'Please log in to complete checkout.';
      } else if (!friendly && rawMessage) {
        friendly = rawMessage;
      }

      setCheckoutError(friendly);
    }
  };

  const seatIds = useMemo(() => Array.from(selected), [selected]);

  return (
    <div className="max-w-5xl mx-auto p-6 space-y-6">
      <h1 className="text-2xl font-bold">Checkout</h1>

      <section className="space-y-3">
        <h2 className="font-semibold">Select Seats</h2>
        {showroom && (
          <SeatMap rows={showroom.numOfRows} cols={showroom.numOfCols} booked={booked} selected={selected} onToggle={toggleSeat} />
        )}
      </section>

      {prices && !!seatIds.length && (
        <section className="space-y-3">
          <h2 className="font-semibold">Ticket Types</h2>
          <TicketTypeSelector seatIds={seatIds} prices={prices} value={tickets} onChange={setTickets} />
        </section>
      )}

      <section className="space-y-3">
        <h2 className="font-semibold">Order Summary</h2>
        <OrderSummary
          subtotal={quote.subtotal}
          discount={quote.discount}
          total={quote.total}
          promoCode={promo}
          onPromoChange={setPromo}
          onApplyPromo={() => null}
        />
      </section>

      <section className="space-y-3">
        <h2 className="font-semibold">Payment</h2>
        <PaymentMethodList cards={cards} selectedCardId={selectedCard} onSelect={setSelectedCard} onAdd={() => alert('Navigate to add card')} />
      </section>

      <button
        className="px-4 py-2 rounded bg-emerald-600 text-white disabled:opacity-50"
        disabled={!selected.size || !tickets.length || !selectedCard}
        onClick={onCheckout}
      >
        Confirm & Pay
      </button>
      {checkoutError && <p className="text-sm text-red-600">{checkoutError}</p>}
    </div>
  );
};

export default CheckoutPage;

