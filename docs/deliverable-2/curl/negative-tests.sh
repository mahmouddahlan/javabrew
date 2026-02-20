#!/bin/bash

BASE="http://localhost:8080"

echo "Duplicate username test"
curl -i -X POST $BASE/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username":"u1",
    "password":"pass123",
    "firstName":"X",
    "lastName":"Y",
    "shippingAddress":{
      "streetName":"A",
      "streetNumber":"1",
      "city":"City",
      "country":"Country",
      "postalCode":"11111"
    }
  }'

echo ""
echo "Bid lower than current"
curl -i -X POST $BASE/api/auctions/1/bids \
  -H "Authorization: Bearer ecab393a-6080-4600-b561-8dd6e182c02f" \
  -H "Content-Type: application/json" \
  -d '{"bidAmount":5}'