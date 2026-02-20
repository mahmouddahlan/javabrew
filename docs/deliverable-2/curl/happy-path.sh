#!/usr/bin/env bash
set -e

BASE="http://localhost:8080/api"

echo "1) Health"
curl -s "$BASE/health"; echo

echo "2) Sign up"
curl -s -X POST "$BASE/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username":"alice","password":"Passw0rd!",
    "firstName":"Alice","lastName":"Smith",
    "shippingAddress":{"streetName":"Main","streetNumber":"123","city":"Toronto","country":"Canada","postalCode":"M1M1M1"}
  }'; echo

echo "3) Login"
curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"Passw0rd!"}'; echo