const BASE_URL = "http://localhost:8080";

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
  });

  const contentType = res.headers.get("content-type") || "";
  const body = contentType.includes("application/json")
    ? await res.json().catch(() => null)
    : await res.text();

  if (!res.ok) {
    const message =
      (body && typeof body === "object" && (body.message || body.error)) ||
      (typeof body === "string" ? body : `Request failed (${res.status})`);
    const err = new Error(message);
    err.status = res.status;
    err.body = body;
    throw err;
  }
  return body;
}

// ---- Auth ----
export const registerUser = (user) =>
  request("/api/auth/register", { method: "POST", body: JSON.stringify(user) });

export const loginUser = (email, password) =>
  request("/api/auth/login", { method: "POST", body: JSON.stringify({ email, password }) });

export const logoutUser = () => request("/api/auth/logout", { method: "POST" });

// ---- Properties ----
export const addProperty = (userId, uniqueUserId, property) =>
  request(`/api/user/add/property/${userId}/${uniqueUserId}`, {
    method: "POST",
    body: JSON.stringify(property),
  });

export const getPropertiesByUser = (userId) =>
  request(`/api/user/${userId}/properties`);

export const getPropertiesForSale = () =>
  request(`/api/user/properties/for-sale`);

export const markPropertyForSale = (propertyId, uniqueUserId) =>
  request(`/api/user/property/sell/${propertyId}/${uniqueUserId}`, { method: "PUT" });

export const deleteProperty = (userId, propertyId) =>
  request(`/api/user/${userId}/delete/property/${propertyId}`, { method: "DELETE" });

export const transferProperty = (propertyId, sellerId, buyerId) =>
  request(`/api/user/property/transfer/${propertyId}/${sellerId}/${buyerId}`, { method: "PUT" });

// ---- Ledger (raw escrow contract calls) ----
export const ledgerRegister = () => request(`/ledger/register`, { method: "POST" });

export const ledgerList = (propertyId, params) => {
  const q = new URLSearchParams(params).toString();
  return request(`/ledger/${propertyId}/list?${q}`, { method: "POST" });
};

export const ledgerDepositEarnest = (propertyId, earnestAmountWei) =>
  request(`/ledger/${propertyId}/earnest?earnestAmountWei=${earnestAmountWei}`, { method: "POST" });

export const ledgerUpdateInspection = (propertyId, passed) =>
  request(`/ledger/${propertyId}/inspection?passed=${passed}`, { method: "POST" });

export const ledgerFund = (propertyId, remainingWei) =>
  request(`/ledger/${propertyId}/fund?remainingWei=${remainingWei}`, { method: "POST" });

export const ledgerFinalize = (propertyId) =>
  request(`/ledger/${propertyId}/finalize`, { method: "POST" });

export const ledgerCancel = (propertyId) =>
  request(`/ledger/${propertyId}/cancel`, { method: "POST" });

export const ledgerGetSale = (propertyId) => request(`/ledger/${propertyId}`);

export const ledgerGetOwner = (propertyId) => request(`/ledger/${propertyId}/owner`);
