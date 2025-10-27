import { ReactNode } from "react";

export default function Section({ title, desc, children }: { title: string; desc?: string; children: ReactNode }) {
  return (
    <section className="rounded-2xl border shadow-sm p-5 space-y-3 bg-white/5">
      <div className="space-y-1">
        <h2 className="text-lg font-semibold">{title}</h2>
        {desc ? <p className="text-sm opacity-80">{desc}</p> : null}
      </div>
      <div className="grid gap-3">{children}</div>
    </section>
  );
}
