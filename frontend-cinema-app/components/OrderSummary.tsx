import React from 'react';

type Props = {
  subtotal: number;
  discount: number;
  total: number;
  promoCode?: string;
  onPromoChange?: (code: string) => void;
  onApplyPromo?: () => void;
};

export const OrderSummary: React.FC<Props> = ({ subtotal, discount, total, promoCode, onPromoChange, onApplyPromo }) => (
  <div className="border rounded p-4 space-y-3 bg-white shadow-sm text-gray-900">
    <div className="flex justify-between"><span className="font-medium">Subtotal</span><span className="font-semibold">${subtotal.toFixed(2)}</span></div>
    <div className="flex justify-between text-emerald-700"><span className="font-medium">Discount</span><span className="font-semibold">- ${discount.toFixed(2)}</span></div>
    <div className="flex justify-between font-bold text-lg"><span>Total</span><span>${total.toFixed(2)}</span></div>
    {onPromoChange && (
      <div className="flex gap-2">
        <input
          className="flex-1 border rounded px-2 py-1 text-gray-900"
          placeholder="Promo code"
          value={promoCode || ''}
          onChange={(e) => onPromoChange(e.target.value)}
        />
        <button className="px-3 py-1 rounded bg-slate-800 text-white" onClick={onApplyPromo}>Apply</button>
      </div>
    )}
  </div>
);
