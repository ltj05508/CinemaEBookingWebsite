export const dynamic = "force-dynamic";

import ProfileForm from "@/components/accountEdit/ProfileForm";
import AddressForm from "@/components/accountEdit/AddressForm";
import BillingAddressForm from "@/components/accountEdit/BillingForm";
import PaymentCardForm from "@/components/accountEdit/PaymentCardForm";
import ChangePasswordForm from "@/components/accountEdit/ChangePasswordForm";

export default function AccountEditPage() {
  return (
    <main className="mx-auto max-w-5xl px-4 py-6 space-y-6">
      <h1 className="text-2xl font-semibold">Account Edit</h1>

      <div className="grid gap-6">
        <ProfileForm />
        <AddressForm />
        <BillingAddressForm />
        <PaymentCardForm />
        <ChangePasswordForm />
      </div>
    </main>
  );
}
