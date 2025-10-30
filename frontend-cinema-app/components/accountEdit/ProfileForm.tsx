"use client";
import { useEffect, useState } from "react";
import Section from "./Section"; import Row from "./Row"; import Input from "./Input"; import Button from "./Button";
import { AccountAPI, type Profile } from "@/lib/accountClient";

export default function ProfileForm() {
  const [profile, setProfile] = useState<Profile>({ firstName: "", lastName: "", email: "", marketingOptIn: false });
  const [saving, setSaving] = useState(false);

  useEffect(() => { 
    AccountAPI.getProfile().then((p) => {
      setProfile(p);
    }); 
  }, []);

  async function onSave() {
    setSaving(true);
    try { await AccountAPI.updateProfile(profile); alert("Profile saved"); }
    catch (e: any) { alert(e?.message || "Failed to save"); }
    finally { setSaving(false); }
  }

  return (
    <Section title="Profile" desc="Edit your first and last name.">
      <Row>
        <label className="text-sm">
          <div className="mb-1">First name</div>
          <Input value={profile.firstName} onChange={(e) => setProfile({ ...profile, firstName: e.target.value })}/>
        </label>
        <label className="text-sm">
          <div className="mb-1">Last name</div>
          <Input value={profile.lastName} onChange={(e) => setProfile({ ...profile, lastName: e.target.value })}/>
        </label>
      </Row>
      <label className="text-sm">
        <div className="mb-1">Email</div>
        <Input value={profile.email || ""} readOnly placeholder="you@example.com"/>
      </label>

         {/* Optional marketing opt-in */}
      <label className="inline-flex items-start gap-3 text-sm select-none cursor-pointer">
          <input
            type="checkbox"
            className="size-4 mt-0.5"
            checked={profile.marketingOptIn || false}
            onChange={(e) => setProfile({ ...profile, marketingOptIn: e.target.checked })}
          />
          <span>
            Send me promotions, special offers, and updates about new releases.
          </span>
        </label>

      <Button onClick={onSave} disabled={saving}>{saving ? "Saving..." : "Save profile"}</Button>
    </Section>
  );
}
