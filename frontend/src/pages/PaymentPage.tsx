import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api/client";
import type { AuctionState, ItemDetail, PaymentResponse } from "../types/api";

export default function PaymentPage() {
  const { itemId } = useParams();
  const navigate = useNavigate();

  const [item, setItem] = useState<ItemDetail | null>(null);
  const [auctionState, setAuctionState] = useState<AuctionState | null>(null);
  const [form, setForm] = useState({
    expeditedShipping: false,
    cardNumber: "",
    nameOnCard: "",
    expiration: "",
    securityCode: ""
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    Promise.all([
      api.get<ItemDetail>(`/items/${itemId}`),
      api.get<AuctionState>(`/auctions/${itemId}`)
    ]).then(([itemRes, auctionRes]) => {
      setItem(itemRes.data);
      setAuctionState(auctionRes.data);
    }).catch((err: any) => {
      setError(err?.response?.data?.message || "Failed to load item");
    });
  }, [itemId]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const { data } = await api.post<PaymentResponse>(`/payments/${itemId}`, form);
      navigate(`/receipts/${data.receiptId}`);
    } catch (err: any) {
      setError(err?.response?.data?.message || "Payment failed");
    } finally {
      setLoading(false);
    }
  }

  const shippingCost = item
    ? (form.expeditedShipping
        ? item.shippingCost + item.expeditedShippingCost
        : item.shippingCost)
    : 0;
  const total = (auctionState?.currentBid ?? 0) + shippingCost;

  return (
    <div style={{ maxWidth: 560, margin: "40px auto", padding: "0 16px" }}>
      <h2 style={{ marginBottom: 24 }}>Complete Payment</h2>

      {error && <div className="error">{error}</div>}

      {/* Order Summary */}
      {item && auctionState && (
        <div style={{
          background: "#fff",
          border: "1px solid #e0e0e0",
          borderRadius: 10,
          padding: 20,
          marginBottom: 24
        }}>
          <p style={{ fontWeight: 600, fontSize: 16, marginBottom: 12 }}>
            Order Summary
          </p>
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
            <span style={{ color: "#555" }}>{item.name}</span>
            <span>${auctionState.currentBid.toFixed(2)}</span>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
            <span style={{ color: "#555" }}>
              Shipping {form.expeditedShipping ? "(expedited)" : "(standard)"}
            </span>
            <span>${shippingCost.toFixed(2)}</span>
          </div>

          {/* Expedited shipping toggle */}
          <div style={{
            display: "flex",
            alignItems: "center",
            gap: 8,
            margin: "12px 0",
            padding: "10px 12px",
            background: "#f5f5f5",
            borderRadius: 6,
            cursor: "pointer"
          }}
            onClick={() => setForm(prev => ({ ...prev, expeditedShipping: !prev.expeditedShipping }))}
          >
            <input
              type="checkbox"
              checked={form.expeditedShipping}
              onChange={(e) => setForm(prev => ({ ...prev, expeditedShipping: e.target.checked }))}
              style={{ width: 16, height: 16, cursor: "pointer" }}
            />
            <div>
              <span style={{ fontWeight: 500 }}>Expedited Shipping</span>
              <span style={{ color: "#888", fontSize: 13, marginLeft: 8 }}>
                +${item.expeditedShippingCost.toFixed(2)}
              </span>
            </div>
          </div>

          <div style={{
            display: "flex",
            justifyContent: "space-between",
            borderTop: "1px solid #e0e0e0",
            paddingTop: 12,
            marginTop: 4,
            fontWeight: 700,
            fontSize: 16
          }}>
            <span>Total</span>
            <span>${total.toFixed(2)}</span>
          </div>
        </div>
      )}

      {/* Card Details */}
      <div style={{
        background: "#fff",
        border: "1px solid #e0e0e0",
        borderRadius: 10,
        padding: 20
      }}>
        <p style={{ fontWeight: 600, fontSize: 16, marginBottom: 16 }}>
          Card Details
        </p>

        <form onSubmit={handleSubmit}>
          <label>Card Number</label>
          <input
            value={form.cardNumber}
            onChange={(e) => setForm(prev => ({ ...prev, cardNumber: e.target.value }))}
            placeholder="4111 1111 1111 1111"
            maxLength={16}
            required
          />

          <label>Name on Card</label>
          <input
            value={form.nameOnCard}
            onChange={(e) => setForm(prev => ({ ...prev, nameOnCard: e.target.value }))}
            placeholder="Alice Smith"
            required
          />

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
            <div>
              <label>Expiration</label>
              <input
                value={form.expiration}
                onChange={(e) => setForm(prev => ({ ...prev, expiration: e.target.value }))}
                placeholder="MM/YY"
                maxLength={5}
                required
              />
            </div>
            <div>
              <label>Security Code</label>
              <input
                value={form.securityCode}
                onChange={(e) => setForm(prev => ({ ...prev, securityCode: e.target.value }))}
                placeholder="123"
                maxLength={4}
                required
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            style={{ marginTop: 8 }}
          >
            {loading ? "Processing..." : `Pay $${total.toFixed(2)}`}
          </button>
        </form>
      </div>
    </div>
  );
}