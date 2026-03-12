# EECS 4413 Auction System
## Overview
JavaBrew is a forward-auction e-commerce backend project for EECS 4413. It supports user registration/login, item listing and search, placing strictly-increasing integer bids, auction state tracking (active vs ended/removed), winner-only payment, and receipt/shipping info.Deliverable 2 focuses on REST backend implementation and API testing scripts (Postman/curl). UI + async notifications are not required in this milestone.
## Tech Stack
Language: Java 17
Framework: Spring Boot (REST)
Persistence: Spring Data JPA + H2 (in-memory DB for local testing)
Build Tool: Maven
API Testing: Postman Collection (Happy Path + Negative Tests)
## How to Run
1) Prereqs
Java 17+
Maven 3.8+
2) Clone the repo 
git clone https://github.com/mahmouddahlan/javabrew
3) Start the server
From repo root:

./mvnw spring-boot:run

Server runs on: http://localhost:8080

Health check:
curl http://localhost:8080/api/health
## Docker Deployment
### Prerequisites
- Docker Desktop installed and running
### Run with Docker Compose (recommended)
docker-compose up --build

- Backend: http://localhost:8080
- Frontend: http://localhost:5173

### Run backend only
docker build -t javabrew-backend .
docker run -p 8080:8080 javabrew-backend

### Run frontend only
cd frontend
docker build -t javabrew-frontend .
docker run -p 5173:5173 javabrew-frontend

### Stop containers
docker-compose down

### Notes
- H2 in-memory database resets on every restart — re-run Postman happy path to seed data
- Admin credentials are seeded automatically on startup (see AdminSeeder.java)

## Running Postman Collection
## **Step 1 – Import Collection**
1. Open Postman
2. Click **Import**
3. Import:
   * `JavaBrew - D2.postman_collection.json`
   * `JavaBrew Local.postman_environment.json`
---
## **Step 2 – Set Environment**
### Select the environment:
JavaBrew Local

Ensure:
baseUrl \= http://localhost:8080/api  
---

## Happy Path (End-to-End Flow)**

Run the following requests in order:

1. 01 \- Health
2. 02 \- Signup
3. 03 \- Login
4. 04 \- Create Item
5. 05 \- Search Items
6. 06 \- Place Bid
7. 07 \- Get Auction State (After End)
8. 08 \- Pay as Winner
9. 09 \- Get Receipt

This demonstrates:

* UC1 – Sign-up / Login
* UC2 – Browse/Search Catalogue
* UC3 – Valid Bidding (integer \+ strictly increasing)
* UC4 – Auction End Behavior
* UC5 – Payment Page Logic
* UC6 – Receipt \+ Shipping Message
* UC7 – Seller Upload Item

All requests in the Happy Path return **200 or 201** depending on endpoint.
---

## Negative Tests (Robustness)**

The following scenarios are implemented:
* Duplicate username → 409 Conflict
* Invalid login credentials → 401 Unauthorized
* Bid ≤ current bid → 400 Bad Request
* Bid after auction ended → 409 Conflict
* Non-winner attempts payment → 403 Forbidden
* Missing payment fields → 400 Bad Request

Each negative test includes assertions validating the expected status code.

### ***NEG 01 – Duplicate Username***
***Purpose: Ensure the system prevents creating a user with an existing username.***

***Run Order:***
1. ***`NEG 05a – Signup Bob`***
2. ***`NEG 01 – Duplicate Username`***

***Expected Result:***  
 ***`409 Conflict`*** 
---
## ***NEG 02 – Bad Login Credentials***
***Purpose: Ensure invalid login credentials are rejected.***

***Run Order:***
1. ***`NEG 02 – Bad Login Credentials`***

***Expected Result:***  
 ***`401 Unauthorized` or `403 Forbidden` (depending on backend implementation)***
---
## ***NEG 03 – Bid \<= Current Bid***
***Purpose: Ensure a bid must be strictly greater than the current highest bid.***
***Required Setup: An item must exist and already have a valid bid.***

***Run Order:***
1. ***`SETUP 01 – Create Item`***
2. ***`SETUP 02 – Place Valid Bid`***
3. ***`NEG 03 – Bid <= Current Bid`***

***Expected Result:***  
 ***`409 Conflict`*** 
---
## ***NEG 04 – Bid After Auction Ended***
***Purpose: Ensure bidding is blocked once an auction has ended.***
***Required Setup: An item must exist, have at least one bid, and the auction must be ended.***

***Run Order:***
1. ***`SETUP 03 – Create Item (Ends in …)`***
2. ***`SETUP 05-2 – Place bid as Alice`***
3. ***`GET end`***
4. ***`NEG 04 – Bid After Auction Ended`***

***Expected Result:***  
 ***`409 Conflict`*** 
---
## ***NEG 05 – Non-winner Pays***
***Purpose: Ensure only the winning bidder can complete payment.***
***Required Setup: An ended auction with a winner must exist. A different user attempts to pay.***

***Run Order:***
1. ***`NEG 05a – Signup Bob`***
2. ***`NEG 05b – Login Bob`***
3. ***`SETUP 05-1 – Create Item`***
4. ***`SETUP 05-2 – Place bid as Alice`***
5. ***`GET end`***
6. ***`NEG 05 – Non-winner pays`***

***Expected Result:***  
 ***`403 Forbidden` or `409 Conflict`***
---
## ***NEG 06 – Missing Payment Fields***
***Purpose: Ensure payment validation fails when required fields are missing.***
***Required Setup: An ended auction where the winner attempts to pay.***

***Run Order:***
1. ***`SETUP 01 – Create Item`***
2. ***`SETUP 02 – Place Valid Bid`***
3. ***`GET end`***
4. ***`NEG 06 – Missing Payment Fields`***

***Expected Result:***  
 ***`400 Bad Request`***
---

## Deliverable 2 Notes
The following deliverables are included in this repository:

- Postman collections and environment files for API testing (`docs/deliverable-2/Postman Collections/`)
- API contract documentation (`docs/deliverable-2/api-contracts.md`)
- Appended design document and design addendum (`docs/deliverable-2/Document_2_Team_1_JavaBrew-Software_Design.pdf`)
- Updated UML diagrams (architecture, sequence, activity)(`docs/deliverable-2/Diagrams/`)
- Backend implementation of the REST API
- Automated unit tests located in `src/test`
- Instructions for running the system, tests, and API flow in this README

## Deliverable 3 Notes
- React TypeScript frontend (UC1–UC7)
- Role-Based Access Control (Admin/User)
- Docker containerization
- Postman D3 collection with admin security tests
- docs/deliverable-3/