import { InputHTMLAttributes } from "react";

export default function Input(props: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      {...props}
      className={
        "w-full rounded-xl border px-3 py-2 bg-transparent outline-none " +
        (props.className || "")
      }
    />
  );
}
