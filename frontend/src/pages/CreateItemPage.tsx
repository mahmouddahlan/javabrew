import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";

export default function CreateItemPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: "",
    description: "",
    keywords: "",
    startingBid: 100,
    durationSeconds: 120,
    shippingCost: 15,
    expeditedShippingCost: 10,
    shippingDays: 5
  });

  const [error, setError] = useState("");

  function update(key: string, value: string | number) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");

    try {
      const { data } = await api.post("/items", {
        name: form.name,
        description: form.description,
        keywords: form.keywords.split(",").map((x) => x.trim()).filter(Boolean),
        startingBid: Number(form.startingBid),
        durationSeconds: Number(form.durationSeconds),
        shippingCost: Number(form.shippingCost),
        expeditedShippingCost: Number(form.expeditedShippingCost),
        shippingDays: Number(form.shippingDays)
      });

      navigate(`/items/${data.itemId}`);
    } catch (err: any) {
      setError(err?.response?.data?.message || "Failed to create item");
    }
  }

  return (
    <div className="card">
      <h2>Create Auction Item</h2>
      {error && <div className="error">{error}</div>}

      <form onSubmit={handleSubmit}>
        <label>Name</label>
        <input value={form.name} onChange={(e) => update("name", e.target.value)} />

        <label>Description</label>
        <textarea value={form.description} onChange={(e) => update("description", e.target.value)} />

        <label>Keywords (comma separated)</label>
        <input value={form.keywords} onChange={(e) => update("keywords", e.target.value)} />

        <label>Starting Bid</label>
        <input type="number" value={form.startingBid} onChange={(e) => update("startingBid", Number(e.target.value))} />

        <label>Duration (seconds)</label>
        <input type="number" value={form.durationSeconds} onChange={(e) => update("durationSeconds", Number(e.target.value))} />

        <label>Shipping Cost</label>
        <input type="number" value={form.shippingCost} onChange={(e) => update("shippingCost", Number(e.target.value))} />

        <label>Expedited Shipping Cost</label>
        <input type="number" value={form.expeditedShippingCost} onChange={(e) => update("expeditedShippingCost", Number(e.target.value))} />

        <label>Shipping Days</label>
        <input type="number" value={form.shippingDays} onChange={(e) => update("shippingDays", Number(e.target.value))} />

        <button type="submit">Create Item</button>
      </form>
    </div>
  );
}