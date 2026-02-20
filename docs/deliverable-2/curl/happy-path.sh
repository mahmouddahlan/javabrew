#!/bin/bash

BASE="http://localhost:8080"

echo "1. Signup user1"
curl -s -X POST $BASE/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username":"u1",
    "password":"pass123",
    "firstName":"A",
    "lastName":"B",
    "shippingAddress":{
      "streetName":"King",
      "streetNumber":"1",
      "city":"Toronto",
      "country":"Canada",
      "postalCode":"M1M1M1"
    }
  }'

echo ""
echo "2. Login user1"
TOKEN=$(curl -s -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"u1","password":"pass123"}' | jq -r '.token')

echo "Token: $TOKEN"

echo ""
echo "3. Create item"
curl -s -X POST $BASE/api/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Laptop",
    "description":"Auction test",
    "keywords":["laptop","test"],
    "startingBid":10,
    "durationSeconds":30,
    "shippingCost":5,
    "expeditedShippingCost":10,
    "shippingDays":7
  }'

echo ""
echo "4. Place bid"
curl -s -X POST $BASE/api/auctions/1/bids \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bidAmount":20}'