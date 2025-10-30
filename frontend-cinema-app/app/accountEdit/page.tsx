"use client";
export const dynamic = "force-dynamic";

import ProfileForm from "@/components/accountEdit/ProfileForm";
import AddressForm from "@/components/accountEdit/AddressForm";
import PaymentCardForm from "@/components/accountEdit/PaymentCardForm";
import ChangePasswordForm from "@/components/accountEdit/ChangePasswordForm";
import { useState } from "react";


export default function AccountEditPage() {
  
  const [marketingOptIn, setMarketingOptIn] = useState(false); 


  return (
    <main className="mx-auto max-w-5xl px-4 py-6 space-y-6">
      <h1 className="text-2xl font-semibold">Account Edit</h1>

      <div className="grid gap-6">
        <ProfileForm />
        <AddressForm />
        <PaymentCardForm />
        <ChangePasswordForm />

                {/* Optional marketing opt-in */}
        <label className="inline-flex items-start gap-3 text-sm select-none cursor-pointer">
          <input
            type="checkbox"
            className="size-4 mt-0.5"
            checked={marketingOptIn}
            onChange={(e) => setMarketingOptIn(e.target.checked)}
          />
          <span>
            Send me promotions, special offers, and updates about new releases.
          </span>
        </label>
      </div>
      


    </main>
  );
  
}
