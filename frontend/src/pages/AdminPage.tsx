import { useEffect, useState } from "react";
import { api } from "../api/client";
import type { AdminStatsResponse, ItemDetail } from "../types/api";

type AdminItem = ItemDetail & {
  sellerUsername?: string;
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
      setError(err?.response?.data?.message || "Failed to load admin dashboard");
    }
  }

  async function deleteItem(itemId: number) {
    try {
      await api.delete(`/admin/items/${itemId}`);
      setSuccess(`Deleted item ${itemId}`);
      await load();
    } catch (err: any) {
      setError(err?.response?.data?.message || "Delete failed");
    }
  }

  async function forceEnd(itemId: number) {
    try {
      await api.post(`/admin/auctions/${itemId}/end`);
      setSuccess(`Force-ended auction ${itemId}`);
      await load();
    } catch (err: any) {
      setError(err?.response?.data?.message || "Force end failed");
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
          <div key={item.itemId} className="card">
            <p><strong>{item.name}</strong></p>
            <p>Status: {item.status}</p>
            <p>Current Bid: ${item.currentBid}</p>
            <div className="row">
              <button className="secondary" onClick={() => forceEnd(item.itemId)}>
                Force End
              </button>
              <button className="danger" onClick={() => deleteItem(item.itemId)}>
                Delete Item
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}