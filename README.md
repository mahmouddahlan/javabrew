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
1) Environment Setup

Use the JavaBrew Local environment.

Required variables:

baseUrl = http://localhost:8080/api
token → seller token
token2 → bidder token
itemId → created item id

Because authentication tokens are stored in memory, after every backend restart 
you must rerun login requests to refresh tokens.

2) Happy Path

Run in this exact order:

01-health
02-Signup
03 - Login request
04 - Create Item (UC7)
05 - Search Items
NEG 05a - Signup Bob
NEG 05b - Login Bob
06 - Place Bid
07 - Get Auction State (end)
08 - Pay as Winner
09 - Get Receipt
Expected outcomes
Signup: 201 Created
Login: 200 OK
Create item: 201 Created
Place bid: 200 OK
Payment: 200 OK
Receipt: 200 OK
3) Negative Path

Run after Happy Path setup exists.

Recommended order
NEG 01 - Duplicate Username
NEG 02 - Bad Login Credentials
NEG 03 - Bid <= Current Bid
NEG 04 - Bid After Auction End
NEG 05 - Non-winner pays
NEG 06 - Missing Payment Fields
NEG 07 - Invalid Auction ID
NEG 08 - Empty Request Body
NEG 09 - Unauthorized Access
Expected outcomes
Duplicate username → 409 Conflict
Bad login → 401 Unauthorized
Low bid → 400 Bad Request
Bid after auction end → 409 Conflict
Non-winner payment → 403 Forbidden
Missing payment fields → 400 Bad Request
Invalid auction id → 404 Not Found
Empty request → 400 Bad Request
Receipt unauthorized access → currently left public (200 OK) for this milestone
4) Security Tests

Before running security tests, run this setup:

03 - Login request
04 - Create Item
NEG 05b - Login Bob
06 - Place Bid

Then run security tests in this order:

SEC 01 - Place Bid No Token
SEC 02 - Invalid Token
SEC 03 - Access Another User
Expected outcomes
No token → 401 Unauthorized
Invalid token → 401 Unauthorized
Wrong authenticated user → 403 Forbidden
5) Important Notes
Token refresh rule

After every backend restart:

rerun seller login
rerun Bob login
rerun create item

Otherwise stale tokens will cause authentication failures or null bidder username database errors.

Scalability Test: A repeated-load test was performed using Postman Collection Runner on the item search endpoint for 50 iterations. All requests returned 200 OK with stable response times and no server failures, demonstrating backend stability under repeated read traffic
---
## Performance Testing (JMeter)

This project was tested using Apache JMeter to evaluate performance under concurrent user load.

### Test Setup
- Server: `localhost:8080`
- Tool: Apache JMeter  
- Endpoints tested:
  - `GET /api/items`
  - `POST /api/auth/login`
- Load levels: **10, 50, 100, 200 users**
- Loop count: 1 (each user sends one request)

The JMX and data files can be found in `src/test/jmeter` containing `javabrew-jmeter.jmx`, `10-users-aggregate.csv`, `50-users-aggregate.csv`, `100-users-aggregate.csv` and `200-users-aggregate.csv`.

---

### Results

#### `/api/items` (Read Operation)

| Users | Avg (ms) | Median (ms) | 90% Line (ms) | 95% Line (ms) |
|------|---------|--------|-----|-----|
| 10 | 6 | 6 | 9 | 9 |
| 50 | 7 | 6 | 8 | 9 |
| 100 | 4 | 4 | 6 | 6 |
| 200 | 4 | 4 | 5 | 5 |

---

#### `/api/auth/login` (Authentication)

| Users | Avg (ms) | Median (ms) | 90% Line (ms) | 95% Line (ms) |
|------|---------|--------|-----|-----|
| 10 | 128 | 122 | 136 | 136 |
| 50 | 119 | 118 | 129 | 131 |
| 100 | 126 | 125 | 138 | 147 |
| 200 | 123 | 123 | 131 | 137 |

---

### Load Normalization

Normalized load using: v / v_0 (v_0 = 500)

| Users | Load |
|------|------|
| 10 | 0.02 |
| 50 | 0.10 |
| 100 | 0.20 |
| 200 | 0.40 |


- Read operations are **extremely fast (4–7 ms)** and scale well  
- Authentication is slower (~120 ms) due to password hashing and database validation  
- Performance remains **stable from 10 → 200 users**  
- System handles up to **40% of baseline capacity without degradation**

The system demonstrates strong performance and scalability under moderate concurrent load. Read-heavy operations perform efficiently, while authentication remains stable despite higher processing overhead.

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

- Admin AI analytics chat feature (A3 GenAI use case)

## Admin AI Chat Setup
The admin dashboard includes an AI analytics assistant for business questions about auction activity, bid trends, top auctions, and items with no bids.

### OpenAI setup
1. Create your own OpenAI API key from the OpenAI platform https://platform.openai.com/api-keys
2. Start the backend by setting your key in the terminal:

```bash
cd /path/to/javabrew
export OPENAI_API_KEY="YOUR_OPENAI_API_KEY_HERE"
export OPENAI_MODEL="gpt-4.1-mini"
./mvnw spring-boot:run

How to test: 
Log in as admin:
username: admin
password: admin123
Open the admin page
Try prompts such as:
How many active auctions do we have?
Show me items with no bids
What are the top 5 auctions by current bid?
Summarize recent bid activity