import React from 'react';

type Props = {
  rows: number;
  cols: number;
  booked: Set<string>;
  selected: Set<string>;
  onToggle: (seatId: string) => void;
};

export const SeatMap: React.FC<Props> = ({ rows, cols, booked, selected, onToggle }) => {
  const seats: string[] = [];
  for (let r = 0; r < rows; r++) {
    for (let c = 1; c <= cols; c++) seats.push(String.fromCharCode(65 + r) + c);
  }
  return (
    <div className="grid gap-2" style={{ gridTemplateColumns: `repeat(${cols}, minmax(40px,1fr))` }}>
      {seats.map((id) => {
        const isBooked = booked.has(id);
        const isSelected = selected.has(id);
        return (
          <button
            key={id}
            disabled={isBooked}
            onClick={() => onToggle(id)}
            className={`h-10 rounded text-sm ${
              isBooked ? 'bg-gray-400 cursor-not-allowed' : isSelected ? 'bg-emerald-500' : 'bg-slate-200'
            }`}
          >
            {id}
          </button>
        );
      })}
    </div>
  );
};
