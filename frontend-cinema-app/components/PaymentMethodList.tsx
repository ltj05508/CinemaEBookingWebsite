import React from 'react';

type Card = { cardId: string; cardNumber?: string; expirationDate?: string };
type Props = {
  cards: Card[];
  selectedCardId?: string;
  onSelect: (cardId: string) => void;
  onAdd?: () => void;
  onDelete?: (cardId: string) => void;
};

export const PaymentMethodList: React.FC<Props> = ({ cards, selectedCardId, onSelect, onAdd, onDelete }) => (
  <div className="space-y-2">
    {cards.map((c) => {
      const num = c.cardNumber || '';
      const last4 = num.slice(-4);
      return (
        <div key={c.cardId} className="flex items-center gap-3 border rounded p-3">
          <input
            type="radio"
            name="card"
            checked={selectedCardId === c.cardId}
            onChange={() => onSelect(c.cardId)}
          />
          <div className="flex-1">
            <div className="font-semibold">**** **** **** {last4 || 'XXXX'}</div>
            <div className="text-sm text-gray-600">Exp: {c.expirationDate || 'MM/YY'}</div>
          </div>
          {onDelete && (
            <button
              type="button"
              className="text-sm text-red-600 hover:underline"
              onClick={() => onDelete(c.cardId)}
            >
              Remove
            </button>
          )}
        </div>
      );
    })}
    {onAdd && (
      <button className="px-3 py-2 rounded border border-dashed w-full text-gray-900 font-semibold" onClick={onAdd} type="button">
        + Add new card
      </button>
    )}
  </div>
);
