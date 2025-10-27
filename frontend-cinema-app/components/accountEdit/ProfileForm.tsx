"use client";
import { useEffect, useState } from "react";
import Section from "./Section"; import Row from "./Row"; import Input from "./Input"; import Button from "./Button";
import { AccountAPI, type Profile } from "@/lib/accountClient";

export default function ProfileForm() {
  const [profile, setProfile] = useState<Profile>({ firstName: "", lastName: "", email: "" });
  const [saving, setSaving] = useState(false);

  useEffect(() => { AccountAPI.getProfile().then(setProfile); }, []);

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
      <Button onClick={onSave} disabled={saving}>{saving ? "Saving..." : "Save profile"}</Button>
    </Section>
  );
}
