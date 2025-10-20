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
    return res.json().catch(() => ({}));
}

export async function resetPassword(body: { token: string; newPassword: string }) {
    const res = await fetch("/api/auth/reset-password", {
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
