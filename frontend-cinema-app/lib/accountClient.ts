export type Profile = { firstName: string; lastName: string; email?: string };
export type Address = { line1: string; line2?: string; city: string; state: string; zip: string };
export type BillingAddress = Address;
export type Card = { cardholderName: string; last4: string; brand?: string; expMonth: string; expYear: string };

// Storage helpers
const K = {
  profile: "acct_profile",
  addr: "acct_address",
  bill: "acct_billing",
  cards: "acct_cards",
  pwd: "acct_pwd", // demo only
};

function get<T>(key: string, fallback: T): T {
  if (typeof window === "undefined") return fallback;
  const raw = localStorage.getItem(key);
  return raw ? (JSON.parse(raw) as T) : fallback;
}
function set<T>(key: string, val: T) {
  if (typeof window === "undefined") return;
  localStorage.setItem(key, JSON.stringify(val));
}

export const AccountAPI = {
  // Profile
  async getProfile(): Promise<Profile> {
    return get<Profile>(K.profile, { firstName: "", lastName: "", email: "you@example.com" });
  },
  async updateProfile(p: Profile) {
    set(K.profile, p);
    return { ok: true };
  },

  // Shipping Address
  async getAddress(): Promise<Address> {
    return get<Address>(K.addr, { line1: "", line2: "", city: "", state: "", zip: "" });
  },
  async updateAddress(a: Address) {
    set(K.addr, a);
    return { ok: true };
  },

  // Billing Address
  async getBilling(): Promise<BillingAddress> {
    return get<BillingAddress>(K.bill, { line1: "", line2: "", city: "", state: "", zip: "" });
  },
  async updateBilling(a: BillingAddress) {
    set(K.bill, a);
    return { ok: true };
  },

  // Cards (store masked meta only)
  async listCards(): Promise<Card[]> {
    return get<Card[]>(K.cards, []);
  },
  async addCard(input: { cardholderName: string; number: string; expMonth: string; expYear: string }) {
    const last4 = input.number.replace(/\s+/g, "").slice(-4) || "0000";
    const brand = detectBrand(input.number);
    const next: Card[] = [...get<Card[]>(K.cards, []), {
      cardholderName: input.cardholderName,
      last4,
      brand,
      expMonth: input.expMonth,
      expYear: input.expYear,
    }];
    set(K.cards, next);
    return { ok: true };
  },

  // Password (demo only)
  async changePassword(oldPassword: string, newPassword: string) {
    const current = get<string>(K.pwd, "demo");
    if (current !== oldPassword) throw new Error("Current password is incorrect (demo)");
    set(K.pwd, newPassword);
    return { ok: true };
  },
};

function detectBrand(num: string) {
  const n = num.replace(/\s+/g, "");
  if (/^4\d{12,18}$/.test(n)) return "Visa";
  if (/^5[1-5]\d{14}$/.test(n)) return "Mastercard";
  if (/^3[47]\d{13}$/.test(n)) return "Amex";
  if (/^6(?:011|5)/.test(n)) return "Discover";
  return "Card";
}
