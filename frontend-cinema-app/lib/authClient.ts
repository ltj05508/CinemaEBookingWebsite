// Optional base URL (useful if your backend is on a different origin/port).
// If unset, same-origin /api/... routes will be used.
const API_BASE = process.env.NEXT_PUBLIC_API_BASE?.replace(/\/$/, "") || "";

/* =========================
 *  LOGIN
 * ========================= */
export async function login(body: { email: string; password: string; remember?: boolean }) {
  const res = await fetch(`${API_BASE}/login`, {
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

/* =========================
 *  FORGOT PASSWORD
 * ========================= */
export async function requestPasswordReset(body: { email: string }) {
  const res = await fetch(`${API_BASE}/forgot-password`, {
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
  return res.json().catch(() => ({}));
}

/* =========================
 *  RESET PASSWORD
 * ========================= */
export async function resetPassword(body: { token: string; newPassword: string }) {
  const res = await fetch(`${API_BASE}/reset-password`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    let message = "Reset failed";
    try {
      const data = await res.json();
      message = data?.error || data?.message || message;
    } catch {}
    throw new Error(message);
  }
  return res.json().catch(() => ({}));
}

/* =========================
 *  SIGNUP (Registration)
 *  Matches backend: registerUser(firstName, lastName, email, password)
 *  Backend sends a verification email with a 6-digit code.
 * ========================= */
export async function registerUser(body: {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  marketingOptIn?: boolean;
}) {
  const res = await fetch(`${API_BASE}/register`, { //signup ///api/auth/register
    method: "POST",
    headers: { "Content-Type": "application/json" },
    // no credentials here (user not logged in yet)
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    let message = "Signup failed";
    try {
      const data = await res.json();
      message = data?.error || data?.message || message;
    } catch {}
    throw new Error(message);
  }
  // Controller can return { verificationCode } or { ok:true }; we don't need it on the client.
  return res.json().catch(() => ({}));
}

/* =========================
 *  EMAIL VERIFICATION
 *  Matches backend: verifyEmail(code) -> boolean success
 * ========================= */
export async function verifyEmail(code: string) {
  const res = await fetch(`${API_BASE}/verify`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ code }),
  });
  if (!res.ok) {
    let message = "Verification failed";
    try {
      const data = await res.json();
      message = data?.error || data?.message || message;
    } catch {}
    throw new Error(message);
  }
  // Expect { ok: true } (or similar) on success
  return res.json().catch(() => ({ ok: true }));
}
