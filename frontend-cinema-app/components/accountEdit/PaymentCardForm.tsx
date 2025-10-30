


"use client";
import { useEffect, useState } from "react";
import Section from "./Section"; import Row from "./Row"; import Input from "./Input"; import Button from "./Button";
import { AccountAPI } from "@/lib/accountClient";
import BillingAddressForm from "./BillingForm";

export default function PaymentCardForm() {
  const [cardholderName, setName] = useState("");
  const [number, setNumber] = useState("");
  const [expMonth, setMonth] = useState("");
  const [expYear, setYear] = useState("");
  const [saving, setSaving] = useState(false);
  const [existing, setExisting] = useState<{brand?: string; last4: string; expMonth: string; expYear: string; cardholderName: string}[]>([]);

  useEffect(() => { AccountAPI.listCards().then(setExisting); }, []);

  async function onAdd() {
    setSaving(true);
    try {
      if (!number || number.replace(/\s+/g, "").length < 12) throw new Error("Enter a valid card number");
      await AccountAPI.addCard({ cardholderName, number, expMonth, expYear });
      setName(""); setNumber(""); setMonth(""); setYear("");
      setExisting(await AccountAPI.listCards());
      alert("Card added");
    } catch (e:any) {
      alert(e?.message || "Failed to add card");
    } finally { setSaving(false); }
  }

  return (
    <Section title="Payment method" desc="Add a new card (demo uses local storage; numbers are not sent anywhere).">
      <label className="text-sm">
        <div className="mb-1">Cardholder name</div>
        <Input value={cardholderName} onChange={(e)=>setName(e.target.value)} autoComplete="cc-name"/>
      </label>
      <label className="text-sm">
        <div className="mb-1">Card number</div>
        <Input value={number} onChange={(e)=>setNumber(e.target.value)} placeholder="1234 5678 9012 3456" autoComplete="cc-number"/>
      </label>
      <Row>
        <label className="text-sm">
          <div className="mb-1">Exp. month</div>
          <Input value={expMonth} onChange={(e)=>setMonth(e.target.value)} placeholder="MM" autoComplete="cc-exp-month"/>
        </label>
        <label className="text-sm">
          <div className="mb-1">Exp. year</div>
          <Input value={expYear} onChange={(e)=>setYear(e.target.value)} placeholder="YYYY" autoComplete="cc-exp-year"/>
        </label>
      </Row>
      <Button onClick={onAdd} disabled={saving}>{saving ? "Adding..." : "Add card"}</Button>

      <div className="grid gap-6 mt-6">
        <BillingAddressForm />
      </div>

      {existing.length > 0 && (
        <div className="pt-4">
          <div className="text-sm font-medium mb-2">Saved cards</div>
          <ul className="space-y-2 text-sm">
            {existing.map((c, i) => (
              <li key={i} className="rounded-xl border p-3">
                {c.brand || "Card"} •••• {c.last4} — {c.expMonth}/{c.expYear} — {c.cardholderName}
              </li>
            ))}
          </ul>
        </div>
      )}
    </Section>
  );
}
