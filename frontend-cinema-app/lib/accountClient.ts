export type Profile = { firstName: string; lastName: string; email?: string };
export type Address = { line1: string; line2?: string; city: string; state: string; zip: string };
export type BillingAddress = Address;
export type Card = { cardholderName: string; last4: string; brand?: string; expMonth: string; expYear: string };
const API_BASE = process.env.NEXT_PUBLIC_API_BASE?.replace(/\/$/, "") || "";

import { getAuthStatus } from "@/lib/authClient";


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

  async updateProfile(p: Profile) {
    const res = await fetch(`${API_BASE}/api/auth/updateUser`, {
      method: "POST",
      credentials: "include",
      cache: "no-store",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(p), // send your profile data
    });
  
    if (!res.ok) {
      // handle error (maybe throw or return error message)
      const err = await res.text();
      throw new Error(`Failed to update profile: ${err}`);
    }
  
    // parse response if needed
    const data = await res.json();
    return { ok: true, data };
  },
  
  async getProfile(): Promise<Profile> {
    try {
      // Use auth status endpoint which we know works
      const authData = await getAuthStatus();
      if (authData?.loggedIn && authData?.user) {
        const profile = {
          firstName: authData.user.firstName || "",
          lastName: authData.user.lastName || "",
          email: authData.user.email || ""
        };
        set(K.profile, profile);
        return profile;
      }
      
      // Not logged in, return empty
      return { firstName: "", lastName: "", email: "" };
    } catch (error) {
      console.error('Error fetching profile:', error);
      return get<Profile>(K.profile, { firstName: "", lastName: "", email: "" });
    }
  },

  // Shipping Address
  async getAddress(): Promise<Address> {
    try {
      const response = await fetch(`${API_BASE}/api/auth/addresses`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error('Failed to fetch address');
      }
      
      const data = await response.json();
      if (data.success && data.addresses && data.addresses.length > 0) {
        const addr = data.addresses[0];
        const address = {
          line1: addr.street || "",
          line2: "",
          city: addr.city || "",
          state: addr.state || "",
          zip: addr.postalCode || ""
        };
        set(K.addr, address);
        return address;
      }
      
      return get<Address>(K.addr, { line1: "", line2: "", city: "", state: "", zip: "" });
    } catch (error) {
      console.error('Error fetching address:', error);
      return get<Address>(K.addr, { line1: "", line2: "", city: "", state: "", zip: "" });
    }
  },
  async updateAddress(a: Address) {
    try {
      const response = await fetch(`${API_BASE}/api/profile/address`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          street: a.line1,
          city: a.city,
          state: a.state,
          postalCode: a.zip,
          country: "USA"
        }),
      });
      
      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || 'Failed to update address');
      }
      
      const data = await response.json();
      if (data.success) {
        set(K.addr, a);
        return { ok: true };
      }
      throw new Error(data.message || 'Update failed');
    } catch (error: any) {
      console.error('Error updating address:', error);
      throw error;
    }
  },

  // Billing Address
  async getBilling(): Promise<BillingAddress> {
    try {
      const response = await fetch(`${API_BASE}/api/profile/billing-address`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || 'Failed to get billing address');
      }
      
      const data = await response.json();
      if (data.success && data.address) {
        const addr: BillingAddress = {
          line1: data.address.street || "",
          city: data.address.city || "",
          state: data.address.state || "",
          zip: data.address.postalCode || ""
        };
        set(K.bill, addr);
        return addr;
      }
      
      // Return empty address if none exists
      const emptyAddr: BillingAddress = { line1: "", city: "", state: "", zip: "" };
      set(K.bill, emptyAddr);
      return emptyAddr;
    } catch (error: any) {
      console.error('Error getting billing address:', error);
      return get(K.bill, { line1: "", city: "", state: "", zip: "" });
    }
  },
  async updateBilling(a: BillingAddress) {
    try {
      const response = await fetch(`${API_BASE}/api/profile/billing-address`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          street: a.line1,
          city: a.city,
          state: a.state,
          postalCode: a.zip,
          country: "USA"
        }),
      });
      
      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || 'Failed to update billing address');
      }
      
      const data = await response.json();
      if (data.success) {
        set(K.bill, a);
        return { ok: true };
      }
      throw new Error(data.message || 'Update failed');
    } catch (error: any) {
      console.error('Error updating billing address:', error);
      throw error;
    }
  },

  // Cards (store masked meta only)
  async listCards(): Promise<Card[]> {
    try {
      const response = await fetch(`${API_BASE}/api/auth/payment-cards`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error('Failed to fetch cards');
      }
      
      const data = await response.json();
      if (data.success && data.cards) {
        const cards = data.cards.map((c: any) => ({
          cardholderName: "Cardholder",  // Backend doesn't return this
          last4: c.cardNumber ? c.cardNumber.slice(-4) : "****",
          brand: "Card",
          expMonth: c.expirationDate ? new Date(c.expirationDate).getMonth() + 1 + "" : "01",
          expYear: c.expirationDate ? new Date(c.expirationDate).getFullYear() + "" : "2025"
        }));
        set(K.cards, cards);
        return cards;
      }
      
      return get<Card[]>(K.cards, []);
    } catch (error) {
      console.error('Error fetching cards:', error);
      return get<Card[]>(K.cards, []);
    }
  },
  async addCard(input: { cardholderName: string; number: string; expMonth: string; expYear: string }) {
    try {
      // Get billing address to link to the card
      let billingAddressId = null;
      try {
        const billingAddr = await this.getBilling();
        // If billing address exists and has data, we'll need to get its ID from backend
        // For now, we'll pass null and let backend handle it
      } catch (e) {
        console.log('No billing address to link');
      }
      
      const response = await fetch(`${API_BASE}/api/profile/cards`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          cardNumber: input.number.replace(/\s+/g, ""),
          expirationDate: `${input.expYear}-${input.expMonth.padStart(2, '0')}-01`,
          billingAddressId: billingAddressId
        }),
      });
      
      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || 'Failed to add card');
      }
      
      const data = await response.json();
      if (data.success) {
        // Refresh the card list
        await this.listCards();
        return { ok: true };
      }
      throw new Error(data.message || 'Add card failed');
    } catch (error: any) {
      console.error('Error adding card:', error);
      throw error;
    }
  },

  // Password (demo only)
  async changePassword(oldPassword: string, newPassword: string) {
    const res = await fetch(`${API_BASE}/api/auth/changePassword`, {
      method: "POST",
      credentials: "include",
      cache: "no-store",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        currentPassword: oldPassword,
        newPassword: newPassword,
      }),
    });

    if (!res.ok) {
      const data = await res.json();
      throw new Error(data.message || "Failed to change password");
    }

    const data = await res.json();
    return { ok: true, data };
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
