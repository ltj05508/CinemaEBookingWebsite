import React from 'react';
import { Ticket } from '../lib/bookingApi';

type Props = {
  seatIds: string[];
  prices: Record<'adult' | 'child' | 'senior', number>;
  value: Ticket[];
  onChange: (tickets: Ticket[]) => void;
};

export const TicketTypeSelector: React.FC<Props> = ({ seatIds, prices, value, onChange }) => {
  const current = new Map(value.map((t) => [t.seatId, t.type]));
  const handle = (seatId: string, type: Ticket['type']) => {
    const next = seatIds.map((s) => ({
      seatId: s,
      type: s === seatId ? type : (current.get(s) || 'adult') as Ticket['type'],
    }));
    onChange(next);
  };
  return (
    <div className="space-y-3">
      {seatIds.map((s) => (
        <div key={s} className="flex items-center justify-between gap-3">
          <div className="font-semibold">{s}</div>
          <select
            className="border rounded px-2 py-1"
            value={current.get(s) || 'adult'}
            onChange={(e) => handle(s, e.target.value as Ticket['type'])}
          >
            <option value="adult">Adult (${prices.adult.toFixed(2)})</option>
            <option value="child">Child (${prices.child.toFixed(2)})</option>
            <option value="senior">Senior (${prices.senior.toFixed(2)})</option>
          </select>
        </div>
      ))}
    </div>
  );
};
