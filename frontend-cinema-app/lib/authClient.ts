// Optional base URL (useful if your backend is on a different origin/port).
// If unset, same-origin /api/... routes will be used.
const API_BASE = process.env.NEXT_PUBLIC_API_BASE?.replace(/\/$/, "") || "";

/* =========================
 *  LOGIN
 *  Backend: POST /api/auth/login
 * ========================= */
export async function login(body: { email: string; password: string; remember?: boolean }) {
  const res = await fetch(`${API_BASE}/api/auth/login`, {
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
  window.dispatchEvent(new Event("auth-changed"));
  return res.json();
}

/* =========================
 *  FORGOT PASSWORD
 *  Backend: POST /api/auth/forgot-password
 * ========================= */
export async function requestPasswordReset(body: { email: string }) {
  const res = await fetch(`${API_BASE}/api/auth/forgot-password`, {
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
 *  Backend: POST /api/auth/reset-password
 * ========================= */
export async function resetPassword(body: { token: string; newPassword: string }) {
  const res = await fetch(`${API_BASE}/api/auth/reset-password`, {
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
 *  Backend: POST /api/auth/register
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
  const res = await fetch(`${API_BASE}/api/auth/register`, {
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
 *  Backend: POST /api/auth/verify
 *  Matches backend: verifyEmail(code) -> boolean success
 * ========================= */
export async function verifyEmail(code: string) {
  const res = await fetch(`${API_BASE}/api/auth/verify`, {
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

/* =========================
 *  AUTH STATUS
 *  Backend: GET /api/auth/status
 *  Returns { loggedIn: boolean, user?: {...} }
 * ========================= */
export async function getAuthStatus() {
  const ts = Date.now(); // cache buster
  const res = await fetch(`${API_BASE}/api/auth/status`, {
    method: "GET",
    credentials: "include",
    cache: "no-store", // ensure fresh status (avoid stale UI)
  });
  if (!res.ok) throw new Error("Failed to check auth status");
  return res.json();
}

/* =========================
 *  LOGOUT
 *  Backend: POST /api/auth/logout
 *  Ends the current session
 * ========================= */
export async function logout() {
  const res = await fetch(`${API_BASE}/api/auth/logout`, {
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
  window.dispatchEvent(new Event("auth-changed"));
  return true;
}
