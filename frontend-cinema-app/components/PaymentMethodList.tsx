import React from 'react';

type Card = { cardId: string; cardNumber?: string; expirationDate?: string };
type Props = {
  cards: Card[];
  selectedCardId?: string;
  onSelect: (cardId: string) => void;
  onAdd?: () => void;
};

export const PaymentMethodList: React.FC<Props> = ({ cards, selectedCardId, onSelect, onAdd }) => (
  <div className="space-y-2">
    {cards.map((c) => {
      const num = c.cardNumber || '';
      const last4 = num.slice(-4);
      return (
        <label key={c.cardId} className="flex items-center gap-2 border rounded p-3">
          <input
            type="radio"
            name="card"
            checked={selectedCardId === c.cardId}
            onChange={() => onSelect(c.cardId)}
          />
          <div>
            <div className="font-semibold">**** **** **** {last4 || 'XXXX'}</div>
            <div className="text-sm text-gray-600">Exp: {c.expirationDate || 'MM/YY'}</div>
          </div>
        </label>
      );
    })}
    {onAdd && (
      <button className="px-3 py-2 rounded border border-dashed w-full" onClick={onAdd}>
        + Add new card
      </button>
    )}
  </div>
);
