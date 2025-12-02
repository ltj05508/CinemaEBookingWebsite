"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthStatus } from "@/lib/authClient";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE?.replace(/\/$/, "") || "";

export default function PromotionsPage() {
    const router = useRouter();
    const [err, setErr] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        (async () => {
            const status = await getAuthStatus();
            const role = String(status?.user?.role ?? "").toLowerCase();
            if (!status?.loggedIn) router.replace(`/login?redirect=${encodeURIComponent("/admin/promotions")}`);
            if (role !== "admin") router.replace("/account");
        })();
    }, [router]);

    async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setErr(null);
        setSubmitting(true);
        const fd = new FormData(e.currentTarget);
        const promoCode = String(fd.get("promoCode") || "").trim().toUpperCase();
        const description = String(fd.get("description") || "");
        const startDate = String(fd.get("startDate") || "");
        const endDate = String(fd.get("endDate") || "");
        const discountPercent = Number(fd.get("discountPercent") || 0);
        //const subject = "New Promotion!";
        //const message = "There is a brand new promotion from the team at CinemaEBooking!\nPromotion Code: " + promoCode + "\nPromo Description: " + description + "\n";
        //const emailNow = fd.get("emailNow") === "on";

        if (!promoCode || !startDate || !description || !endDate || !(discountPercent > 0 && discountPercent <= 100)) {
            setErr("Please provide a code, description, valid dates, and a discount between 1 and 100.");
            setSubmitting(false);
            return;
        }
        if (endDate < startDate) {
            setErr("End date must be on or after the start date.");
            setSubmitting(false);
            return;
        }

        try {
            const createRes = await fetch(`${API_BASE}/api/auth/promotions`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ promoCode, description, startDate, endDate, discountPercent }),
            });
            if (!createRes.ok) throw new Error(`Create failed (${createRes.status})`);
            const created = await createRes.json();

            if (createRes.ok) {
                const subject = "New Promotion!";
                const message = "There is a brand new promotion from the team at CinemaEBooking!\nPromotion Code: " + promoCode + "\nPromo Description: " + description + "\n";
                const emailRes = await fetch(`${API_BASE}/api/auth/promotions/email`, {
                     method: "POST", 
                     headers: { "Content-Type": "application/json" },
                     body: JSON.stringify({ subject, message }),
                    }); //${encodeURIComponent(created.id)}
                if (!emailRes.ok) throw new Error("Created, but email send failed.");
            }

            router.push("/admin?promoCreated=1");
        } catch (e: any) {
            setErr(e.message || "Failed to create promotion.");
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="relative z-10 mx-auto max-w-2xl px-4 py-8 space-y-6">
            <h1 className="text-2xl font-semibold">Create Promotion</h1>
            {err && <p className="text-red-600">{err}</p>}

            <form onSubmit={onSubmit} className="space-y-4">
                <div>
                    <label className="block text-sm mb-1">Promo Code *</label>
                    <input name="promoCode" required placeholder="FALL25" className="w-full border rounded-xl px-3 py-2 uppercase" />
                </div>

                <div>
                    <label className="block text-sm mb-1">Description *</label>
                    <input name="description" required placeholder="Test promo code!" className="w-full border rounded-xl px-3 py-2" />
                </div>

                <div className="grid sm:grid-cols-3 gap-4">
                    <div>
                        <label className="block text-sm mb-1">Start Date *</label>
                        <input
                            name="startDate"
                            type="date"
                            required
                            className="w-full border rounded-xl px-3 py-2 bg-white"
                            inputMode="numeric"
                            placeholder="YYYY-MM-DD"
                            pattern="\d{4}-\d{2}-\d{2}"
                        />
                    </div>
                    <div>
                        <label className="block text-sm mb-1">End Date *</label>
                        <input
                            name="endDate"
                            type="date"
                            required
                            className="w-full border rounded-xl px-3 py-2 bg-white"
                            inputMode="numeric"
                            placeholder="YYYY-MM-DD"
                            pattern="\d{4}-\d{2}-\d{2}"
                        />
                    </div>
                    <div>
                        <label className="block text-sm mb-1">Discount % *</label>
                        <input name="discountPercent" type="number" min={1} max={100} required className="w-full border rounded-xl px-3 py-2" />
                    </div>
                </div>


                <div className="flex gap-3">
                    <button disabled={submitting} className="rounded-xl border px-4 py-2 hover:bg-gray-50">
                        {submitting ? "Saving…" : "Save Promotion"}
                    </button>
                    <button type="button" onClick={() => router.back()} className="rounded-xl border px-4 py-2">
                        Cancel
                    </button>
                </div>
            </form>
        </main>
    );
}

