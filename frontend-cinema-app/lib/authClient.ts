// Optional base URL (useful if your backend is on a different origin/port).
// If unset, same-origin /api/... routes will be used.
const API_BASE = process.env.NEXT_PUBLIC_API_BASE?.replace(/\/$/, "") || "";

// lib/authClient.ts

export async function login(body: { email: string; password: string; remember?: boolean }) {
  const res = await fetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    let message = "Login failed";
    try {
      const data = await res.json();
      message = data?.error || data?.message || message;
    } catch {}
    throw new Error(message);
  }
  return res.json();
}

export async function requestPasswordReset(body: { email: string }) {
  const res = await fetch("/api/auth/forgot-password", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    let message = "Unable to send reset link";
    try {
      const data = await res.json();
      message = data?.error || data?.message || message;
    } catch {}
    throw new Error(message);
  }
  return res.json();
}

export async function verifyEmail(code: string) {
  const res = await fetch("/api/auth/verify", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ code }),
  });
  if (!res.ok) {
    let message = "Invalid or expired code";
    try {
      const data = await res.json();
      message = data?.error || data?.message || message;
    } catch {}
    throw new Error(message);
  }
  return res.json();
}

export async function getAuthStatus() {
  const res = await fetch("/api/auth/status", {
    method: "GET",
    credentials: "include",
  });
  if (!res.ok) throw new Error("Failed to check auth status");
  return res.json();
}

export async function logout() {
  const res = await fetch("/api/auth/logout", {
    method: "POST",
    credentials: "include",
  });
  if (!res.ok) {
    let message = "Logout failed";
    try {
      const data = await res.json();
      message = data?.message || message;
    } catch {}
    throw new Error(message);
  }
  return true;
}
