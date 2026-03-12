export type Role = "USER" | "ADMIN";

export interface LoginResponse {
  token: string;
  username: string;
  role: Role;
}

export interface CurrentUserResponse {
  userId: number;
  username: string;
  firstName: string;
  lastName: string;
  role: Role;
}

export interface SignupRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  shippingAddress: {
    streetName: string;
    streetNumber: string;
    city: string;
    country: string;
    postalCode: string;
  };
}

export interface ItemSummary {
  itemId: number;
  name: string;
  currentBid: number;
  auctionType: string;
  endsAt: string;
}

export interface ItemDetail {
  itemId: number;
  name: string;
  description: string;
  keywords: string[];
  currentBid: number;
  highestBidder: string | null;
  status: string;
  endsAt: string;
  shippingCost: number;
  expeditedShippingCost: number;
  shippingDays: number;
}

export interface AuctionState {
  itemId: number;
  status: string;
  currentBid: number;
  highestBidder: string | null;
  endsAt: string;
}

export interface PaymentResponse {
  receiptId: number;
  itemId: number;
  paidBy: string;
  itemPrice: number;
  shippingCost: number;
  totalPaid: number;
  shippingDays: number;
}

export interface ReceiptResponse {
  receiptId: number;
  totalPaid: number;
  shippingInfo: string;
}

export interface AdminStatsResponse {
  totalItems: number;
  activeItems: number;
  endedItems: number;
  removedNoBidItems: number;
}