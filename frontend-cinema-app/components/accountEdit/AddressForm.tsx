"use client";
import { useEffect, useState } from "react";
import Section from "./Section"; import Row from "./Row"; import Input from "./Input"; import Button from "./Button";
import { AccountAPI, type Address } from "@/lib/accountClient";

export default function AddressForm() {
  const [addr, setAddr] = useState<Address>({ line1:"", line2:"", city:"", state:"", zip:"" });
  const [saving, setSaving] = useState(false);

  useEffect(() => { AccountAPI.getAddress().then(setAddr); }, []);

  async function onSave() {
    setSaving(true);
    try { await AccountAPI.updateAddress(addr); alert("Address saved"); }
    catch (e:any) { alert(e?.message || "Failed to save"); }
    finally { setSaving(false); }
  }

  return (
    <Section title="Address" desc="Your shipping / primary address.">
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
      <Button onClick={onSave} disabled={saving}>{saving ? "Saving..." : "Save address"}</Button>
    </Section>
  );
}
