# Javabrew API Contracts

Base URL: http://localhost:8080
All endpoints are prefixed with `/api`.

## Conventions
- JSON request/response
- Error response format:
  - `{ "timestamp": "...", "status": 400, "error": "Bad Request", "message": "...", "path": "/api/..." }`

---

## 0. Health
### GET /api/health
Response 200:
{ "status": "ok" }

---

## 1. Authentication (UC1)
### POST /api/auth/signup
Request:
{
  "username": "alice",
  "password": "Passw0rd!",
  "firstName": "Alice",
  "lastName": "Smith",
  "shippingAddress": {
    "streetName": "Main St",
    "streetNumber": "123",
    "city": "Toronto",
    "country": "Canada",
    "postalCode": "M1M1M1"
  }
}
Response 201:
{ "userId": 1, "username": "alice" }
Errors: 400 invalid input, 409 username exists

### POST /api/auth/login
Request:
{ "username": "alice", "password": "Passw0rd!" }
Response 200:
{ "token": "FAKE-JWT-OR-SESSION-TOKEN", "username": "alice" }
Errors: 401 invalid credentials

---

## 2. Catalogue (UC2 + UC7)
### POST /api/items
(Seller creates an item for forward auction)
Request:
{
  "name": "Nintendo Switch",
  "description": "Like new",
  "keywords": ["console", "gaming", "switch"],
  "auctionType": "FORWARD",
  "startingBid": 100,
  "durationSeconds": 120,
  "shippingCost": 15,
  "expeditedShippingCost": 10,
  "shippingDays": 5
}
Response 201:
{ "itemId": 101, "status": "ACTIVE" }
Errors: 400 invalid input

### GET /api/items?keyword=switch
Response 200:
[
  {
    "itemId": 101,
    "name": "Nintendo Switch",
    "currentBid": 100,
    "auctionType": "FORWARD",
    "endsAt": "2026-02-20T20:00:00Z"
  }
]

### GET /api/items/{itemId}
Response 200:
{
  "itemId": 101,
  "name": "...",
  "description": "...",
  "currentBid": 120,
  "highestBidder": "alice",
  "endsAt": "..."
}

---

## 3. Bidding (UC3 + UC4)
### POST /api/auctions/{itemId}/bids
Request:
{ "bidAmount": 121 }
Response 200:
{
  "itemId": 101,
  "currentBid": 121,
  "highestBidder": "alice",
  "endsAt": "..."
}
Errors:
- 400 bid not integer / <= current bid
- 404 item not found
- 409 auction ended

### GET /api/auctions/{itemId}
Response 200:
{
  "itemId": 101,
  "status": "ACTIVE|ENDED",
  "currentBid": 121,
  "highestBidder": "alice",
  "endsAt": "..."
}

---

## 4. Payment + Receipt (UC5 + UC6)
### POST /api/payments/{itemId}
Request:
{
  "expeditedShipping": true,
  "cardNumber": "4111111111111111",
  "nameOnCard": "Alice Smith",
  "expiration": "12/28",
  "securityCode": "123"
}
Response 200:
{
  "receiptId": 9001,
  "itemId": 101,
  "paidBy": "alice",
  "itemPrice": 121,
  "shippingCost": 25,
  "totalPaid": 146,
  "shippingDays": 5
}
Errors:
- 403 user is not auction winner
- 400 missing payment fields
- 409 auction not ended yet (optional rule)

### GET /api/receipts/{receiptId}
Response 200:
{
  "receiptId": 9001,
  "totalPaid": 146,
  "shippingInfo": "The item will be shipped in 5 days."
}