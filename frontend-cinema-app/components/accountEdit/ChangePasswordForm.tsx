"use client";
import { useState } from "react";
import Section from "./Section"; import Input from "./Input"; import Button from "./Button";
import { AccountAPI } from "@/lib/accountClient";

export default function ChangePasswordForm() {
  const [oldPassword, setOld] = useState("");
  const [newPassword, setNew] = useState("");
  const [confirm, setConfirm] = useState("");
  const [saving, setSaving] = useState(false);

  async function onChangePassword() {
    if (newPassword !== confirm) { alert("New passwords do not match"); return; }
    if (newPassword.length < 8) { alert("Password must be at least 8 characters"); return; }
    setSaving(true);
    try {
      await AccountAPI.changePassword(oldPassword, newPassword);
      alert("Password changed (demo)");
      setOld(""); setNew(""); setConfirm("");
    } catch (e:any) {
      alert(e?.message || "Failed to change password");
    } finally { setSaving(false); }
  }

  return (
    <Section title="Change password">
      <label className="text-sm">
        <div className="mb-1">Current password</div>
        <Input type="password" value={oldPassword} onChange={(e)=>setOld(e.target.value)}/>
      </label>
      <label className="text-sm">
        <div className="mb-1">New password</div>
        <Input type="password" value={newPassword} onChange={(e)=>setNew(e.target.value)}/>
      </label>
      <label className="text-sm">
        <div className="mb-1">Confirm new password</div>
        <Input type="password" value={confirm} onChange={(e)=>setConfirm(e.target.value)}/>
      </label>
      <Button onClick={onChangePassword} disabled={saving}>{saving ? "Saving..." : "Change password"}</Button>
    </Section>
  );
}
