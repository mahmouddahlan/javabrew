import { useEffect, useState, useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";
import type { AuctionState, ItemDetail } from "../types/api";

export default function ItemPage() {
  const { itemId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [item, setItem] = useState<ItemDetail | null>(null);
  const [auctionState, setAuctionState] = useState<AuctionState | null>(null);
  const [bidAmount, setBidAmount] = useState("");
  const [error, setError] = useState("");
  const [timeLeft, setTimeLeft] = useState("");
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  async function loadData() {
    try {
      const [itemRes, auctionRes] = await Promise.all([
        api.get<ItemDetail>(`/items/${itemId}`),
        api.get<AuctionState>(`/auctions/${itemId}`)
      ]);
      setItem(itemRes.data);
      setAuctionState(auctionRes.data);
      setError("");
    } catch (err: any) {
      setError(err?.response?.data?.message || "Failed to load item");
    }
  }

  // Countdown timer + auto-refresh auction state every 3s
  useEffect(() => {
    if (!auctionState?.endsAt) return;

    intervalRef.current = setInterval(async () => {
      const diff = new Date(auctionState.endsAt).getTime() - Date.now();

      if (diff <= 0) {
        setTimeLeft("Ended");
        // Refresh from server to get final ENDED status
        try {
          const res = await api.get<AuctionState>(`/auctions/${itemId}`);
          setAuctionState(res.data);
        } catch {}
        clearInterval(intervalRef.current!);
      } else {
        const h = Math.floor(diff / 3600000);
        const m = Math.floor((diff % 3600000) / 60000);
        const s = Math.floor((diff % 60000) / 1000);
        setTimeLeft(h > 0 ? `${h}h ${m}m ${s}s` : `${m}m ${s}s`);

        // Also refresh auction state every 3s to pick up other users' bids
        if (Math.floor(diff / 1000) % 3 === 0) {
          try {
            const res = await api.get<AuctionState>(`/auctions/${itemId}`);
            setAuctionState(res.data);
          } catch {}
        }
      }
    }, 1000);

    return () => clearInterval(intervalRef.current!);
  }, [auctionState?.endsAt, itemId]);

  useEffect(() => {
    loadData();
  }, [itemId]);

  async function placeBid(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    try {
      await api.post(`/auctions/${itemId}/bids`, {
        bidAmount: Number(bidAmount)
      });
      setBidAmount("");
      await loadData();
    } catch (err: any) {
      setError(err?.response?.data?.message || "Bid failed");
    }
  }

  if (!item || !auctionState) return <div className="card">Loading...</div>;

  const isEnded = auctionState.status === "ENDED";
  const isWinner = isEnded && user?.username === auctionState.highestBidder;
  const isActive = auctionState.status === "ACTIVE";

  return (
    <div className="card">
      <h2>{item.name}</h2>
      {error && <div className="error">{error}</div>}

      <p><strong>Description:</strong> {item.description}</p>
      <p><strong>Current Bid:</strong> ${auctionState.currentBid}</p>
      <p><strong>Highest Bidder:</strong> {auctionState.highestBidder || "None"}</p>
      <p><strong>Status:</strong> {auctionState.status}</p>

      {isActive && (
        <p><strong>Time Remaining:</strong> {timeLeft || "Calculating..."}</p>
      )}

      {/* Active auction — show bid form */}
      {isActive && (
        <form onSubmit={placeBid}>
          <label>Place Bid</label>
          <input
            type="number"
            value={bidAmount}
            onChange={(e) => setBidAmount(e.target.value)}
            placeholder={`Enter amount > ${auctionState.currentBid}`}
          />
          <button type="submit">Submit Bid</button>
        </form>
      )}

      {/* Auction ended — winner sees Pay Now */}
      {isEnded && isWinner && (
        <div>
          <p><strong>🎉 You won this auction!</strong></p>
          <button onClick={() => navigate(`/payments/${item.itemId}`)}>
            Pay Now
          </button>
        </div>
      )}

      {/* Auction ended — non-winner sees failure notice */}
      {isEnded && !isWinner && (
        <div className="error">
          This auction has ended. 
          {auctionState.highestBidder
            ? ` Winner: ${auctionState.highestBidder}`
            : " No bids were placed."}
        </div>
      )}
    </div>
  );
}