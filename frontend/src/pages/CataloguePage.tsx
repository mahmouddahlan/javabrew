import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";
import type { ItemSummary } from "../types/api";
import ItemCard from "../components/ItemCard";

export default function CataloguePage() {
  const { user } = useAuth();
  const [keyword, setKeyword] = useState("");
  const [items, setItems] = useState<ItemSummary[]>([]);
  const [wonItems, setWonItems] = useState<ItemSummary[]>([]);
  const [error, setError] = useState("");

  async function loadItems(search = "") {
    try {
      const { data } = await api.get<ItemSummary[]>("/items", {
        params: search ? { keyword: search } : {}
      });
      setItems(data);
      setError("");
    } catch (err: any) {
      setError(err?.response?.data?.message || "Failed to load items");
    }
  }

  async function loadWonItems() {
    if (!user) return;
    try {
      // Get all ended items where this user is the highest bidder
      const { data } = await api.get<ItemSummary[]>("/items/won");
      setWonItems(data);
    } catch {
      // Endpoint may not exist yet — fail silently
    }
  }

  async function handleAdminDelete(itemId: number) {
    if (!confirm("Delete this item?")) return;
    try {
      await api.delete(`/admin/items/${itemId}`);
      setItems(prev => prev.filter(i => i.itemId !== itemId));
    } catch (err: any) {
      alert(err?.response?.data?.message || "Delete failed");
    }
  }

  useEffect(() => {
    loadItems();
    loadWonItems();
  }, [user]);

  const isAdmin = user?.role === "ADMIN";

  return (
    <div>
      <div className="card">
        <h2>Catalogue</h2>
        <div className="row">
          <input
            placeholder="Search items..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <button style={{ width: "160px" }} onClick={() => loadItems(keyword)}>
            Search
          </button>
        </div>
      </div>

      {error && <div className="error">{error}</div>}

      {items.length === 0 ? (
        <div className="card">No active auctions found.</div>
      ) : (
        items.map((item) => (
          <div key={item.itemId} style={{ position: "relative" }}>
            <ItemCard item={item} />
            {isAdmin && (
              <button
                onClick={() => handleAdminDelete(item.itemId)}
                className="secondary"
                style={{
                  position: "absolute", top: 12, right: 12,
                  width: "auto", margin: 0, background: "#c0392b",
                  color: "white", border: "none", padding: "4px 10px",
                  borderRadius: 4, cursor: "pointer", fontSize: 13
                }}
              >
                Delete
              </button>
            )}
          </div>
        ))
      )}

      {/* Won items — only shown if user has unpaid wins */}
      {wonItems.length > 0 && (
        <div>
          <div className="card" style={{ marginTop: 24 }}>
            <h2>🏆 Your Won Auctions</h2>
            <p style={{ color: "#666", fontSize: 14 }}>
              You won these auctions — complete payment to finish your purchase.
            </p>
          </div>
          {wonItems.map((item) => (
            <ItemCard key={item.itemId} item={item} />
          ))}
        </div>
      )}
    </div>
  );
}