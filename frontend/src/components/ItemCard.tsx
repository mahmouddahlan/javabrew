import { Link } from "react-router-dom";
import type { ItemSummary } from "../types/api";

export default function ItemCard({ item }: { item: ItemSummary }) {
  return (
    <div className="card">
      <h3>{item.name}</h3>
      <p><strong>Current Bid:</strong> ${item.currentBid}</p>
      <p><strong>Type:</strong> {item.auctionType}</p>
      <p><strong>Ends At:</strong> {new Date(item.endsAt).toLocaleString()}</p>
      <Link to={`/items/${item.itemId}`}>
        <button>View Item</button>
      </Link>
    </div>
  );
}