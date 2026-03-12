import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { api } from "../api/client";
import type { ReceiptResponse } from "../types/api";

export default function ReceiptPage() {
  const { receiptId } = useParams();
  const navigate = useNavigate();
  const [receipt, setReceipt] = useState<ReceiptResponse | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get<ReceiptResponse>(`/receipts/${receiptId}`)
      .then((res) => setReceipt(res.data))
      .catch((err) => setError(err?.response?.data?.message || "Failed to load receipt"));
  }, [receiptId]);

  if (error) return <div className="card error">{error}</div>;
  if (!receipt) return <div className="card">Loading...</div>;

  return (
    <div className="card">
      <h2>🧾 Receipt</h2>
      <p><strong>Receipt ID:</strong> #{receipt.receiptId}</p>
      <p><strong>Total Paid:</strong> ${receipt.totalPaid}</p>
      <p><strong>Shipping Info:</strong> {receipt.shippingInfo}</p>
      <button
        onClick={() => navigate("/catalogue")}
        style={{ marginTop: 16 }}
      >
        Back to Main Page
      </button>
    </div>
  );
}