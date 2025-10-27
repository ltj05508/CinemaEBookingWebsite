"use client";
import { useEffect, useState } from "react";
import Section from "./Section"; import Row from "./Row"; import Input from "./Input"; import Button from "./Button";
import { AccountAPI, type BillingAddress } from "@/lib/accountClient";

export default function BillingAddressForm() {
  const [addr, setAddr] = useState<BillingAddress>({ line1:"", line2:"", city:"", state:"", zip:"" });
  const [saving, setSaving] = useState(false);

  useEffect(() => { AccountAPI.getBilling().then(setAddr); }, []);

  async function onSave() {
    setSaving(true);
    try { await AccountAPI.updateBilling(addr); alert("Billing address saved"); }
    catch (e:any) { alert(e?.message || "Failed to save"); }
    finally { setSaving(false); }
  }

  return (
    <Section title="Billing address" desc="Used for payments and receipts.">
      <label className="text-sm">
        <div className="mb-1">Address line 1</div>
        <Input value={addr.line1} onChange={(e)=>setAddr({...addr, line1: e.target.value})}/>
      </label>
      <label className="text-sm">
        <div className="mb-1">Address line 2 (optional)</div>
        <Input value={addr.line2 || ""} onChange={(e)=>setAddr({...addr, line2: e.target.value})}/>
      </label>
      <Row>
        <label className="text-sm">
          <div className="mb-1">City</div>
          <Input value={addr.city} onChange={(e)=>setAddr({...addr, city: e.target.value})}/>
        </label>
        <label className="text-sm">
          <div className="mb-1">State</div>
          <Input value={addr.state} onChange={(e)=>setAddr({...addr, state: e.target.value})}/>
        </label>
      </Row>
      <label className="text-sm">
        <div className="mb-1">ZIP</div>
        <Input value={addr.zip} onChange={(e)=>setAddr({...addr, zip: e.target.value})}/>
      </label>
      <Button onClick={onSave} disabled={saving}>{saving ? "Saving..." : "Save billing address"}</Button>
    </Section>
  );
}
