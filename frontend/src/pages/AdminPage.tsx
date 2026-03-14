import { useEffect, useState } from "react";
import { api } from "../api/client";
import type { AdminStatsResponse } from "../types/api";

type AdminItem = {
  id: number;
  name: string;
  status: string;
  currentBid: number;
};

export default function AdminPage() {
  const [stats, setStats] = useState<AdminStatsResponse | null>(null);
  const [items, setItems] = useState<AdminItem[]>([]);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function load() {
    try {
      const [statsRes, itemsRes] = await Promise.all([
        api.get<AdminStatsResponse>("/admin/stats"),
        api.get<AdminItem[]>("/admin/items")
      ]);
      setStats(statsRes.data);
      setItems(itemsRes.data);
      setError("");
    } catch (err: any) {
      setError(err?.response?.data?.message ?? err?.message ?? "Unexpected error");
    }
  }

  async function deleteItem(itemId: number) {
    try {
      await api.delete(`/admin/items/${itemId}`);
      setSuccess(`Deleted item ${itemId}`);
      setError("");
      await load();
    } catch (err: any) {
      setError(err?.response?.data?.message ?? err?.message ?? "Delete failed");
    }
  }

  async function forceEnd(itemId: number) {
    try {
      await api.post(`/admin/auctions/${itemId}/end`);
      setSuccess(`Force-ended auction ${itemId}`);
      setError("");
      await load();
    } catch (err: any) {
      setError(err?.response?.data?.message ?? err?.message ?? "Force end failed");
    }
  }

  useEffect(() => {
    load();
  }, []);

  return (
    <div>
      <div className="card">
        <h2>Admin Dashboard</h2>
        {error && <div className="error">{error}</div>}
        {success && <div className="success">{success}</div>}

        {stats && (
          <div className="grid grid-2">
            <div className="card"><strong>Total Items:</strong> {stats.totalItems}</div>
            <div className="card"><strong>Active Items:</strong> {stats.activeItems}</div>
            <div className="card"><strong>Ended Items:</strong> {stats.endedItems}</div>
            <div className="card"><strong>Removed No Bid Items:</strong> {stats.removedNoBidItems}</div>
          </div>
        )}
      </div>

      <div className="card">
        <h3>All Auction Items</h3>
        {items.map((item) => (
          <div key={item.id} className="card">
            <p><strong>{item.name}</strong></p>
            <p>Status: {item.status}</p>
            <p>Current Bid: ${item.currentBid}</p>
            <div className="row">
              {item.status === "ACTIVE" && (
                <button className="secondary" onClick={() => forceEnd(item.id)}>
                  Force End
                </button>
              )}
              <button className="danger" onClick={() => deleteItem(item.id)}>
                Delete Item
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}