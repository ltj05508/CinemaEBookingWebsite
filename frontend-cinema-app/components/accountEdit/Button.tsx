import { ButtonHTMLAttributes } from "react";
export default function Button(props: ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      {...props}
      className={
        "rounded-xl border px-4 py-2 text-sm font-medium hover:bg-ugared/10 active:scale-[.99] " +
        (props.className || "")
      }
    />
  );
}
