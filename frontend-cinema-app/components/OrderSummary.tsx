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
  <div className="border rounded p-4 space-y-3 bg-white shadow-sm">
    <div className="flex justify-between"><span>Subtotal</span><span>${subtotal.toFixed(2)}</span></div>
    <div className="flex justify-between text-emerald-600"><span>Discount</span><span>- ${discount.toFixed(2)}</span></div>
    <div className="flex justify-between font-bold text-lg"><span>Total</span><span>${total.toFixed(2)}</span></div>
    {onPromoChange && (
      <div className="flex gap-2">
        <input
          className="flex-1 border rounded px-2 py-1"
          placeholder="Promo code"
          value={promoCode || ''}
          onChange={(e) => onPromoChange(e.target.value)}
        />
        <button className="px-3 py-1 rounded bg-slate-800 text-white" onClick={onApplyPromo}>Apply</button>
      </div>
    )}
  </div>
);
