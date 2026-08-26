# Purchase Decision API

A REST API that answers one question — **"Should I buy this?"** — and backs the answer with a concrete financial plan.

Submit a purchase, get a verdict (**BUY** / **WAIT** / **SKIP**), an affordability score, and — when the answer is WAIT — a savings plan showing exactly how long to save and at what pace. An LLM turns the numbers into plain-English reasoning.

**Live app:** `https://purchase-decision-api-production.up.railway.app`
**Interactive docs:** [Swagger UI](https://purchase-decision-api-production.up.railway.app/swagger-ui/index.html) (also at `/docs`)

---

## Try it in your browser

Open the root URL and you get the **web app** — a small single-page front end served
by the same Spring Boot process, so there's no second deployment and no CORS to
configure.

1. Open the [app](https://purchase-decision-api-production.up.railway.app).
2. Hit **Try it with a sample profile**. That calls `POST /api/auth/demo` behind the
   scenes and drops you straight on the decision form with a profile already set.
3. Type a thing and a price, pick NEED / WANT / LUXURY, get the verdict.

Creating your own account walks you through the financial profile first, then the
same decision form. The token is kept in `localStorage`, so a reload picks up where
you left off.

The front end is three files — `index.html`, `app.css`, `app.js` — in
`src/main/resources/static/`. No framework, no build step, no `node_modules`: the
jar is still the whole deployment.

### Or drive the API directly

Everything is also testable from Swagger UI — no Postman, no curl, no signup, no local setup.

1. Open the [Swagger UI](https://purchase-decision-api-production.up.railway.app/swagger-ui/index.html).
2. **`POST /api/auth/demo`** → *Try it out* → *Execute*. No request body. You get a token for a throwaway account that **already has a financial profile**. Copy the `token`.
3. Click the green **Authorize** button (top right), paste the token, *Authorize*, *Close*. Paste the raw token only — Swagger adds the `Bearer ` prefix itself. It's remembered across page reloads.
4. **`POST /api/decision`** → pick any example from the dropdown → *Execute*.

The demo profile has **₹24,500 disposable income** per month (₹62,500 income − ₹25,500 fixed expenses − 20% saved), and the three sample purchases are picked to land on a different verdict each:

| Example | Score | Verdict |
|---|---|---|
| Standing desk, ₹15,000, NEED | 75 | **BUY** |
| Sony headphones, ₹30,000, WANT, daily use | 65 | **WAIT** + 2-month savings plan |
| Second-hand car, ₹450,000, LUXURY, ₹9,000/mo running cost | 10 | **SKIP** |

### Using your own account

**`POST /api/auth/register`** with your own email, or **`POST /api/auth/login`** as the shared demo user `demo@purchasedecision.app` / `Demo@1234`. Then call **`PUT /api/users/profile`** *before* `POST /api/decision` — without a profile there's nothing to score against and the decision endpoint returns `400`.

The shared demo account is reset to its documented profile every time the service restarts, so it can't stay broken. `POST /api/auth/demo` mints a private account per call and avoids the shared state altogether.

Set `DEMO_ENABLED=false` to remove the seeded account and the `/api/auth/demo` endpoint entirely.

---

## The core design decision

**The math decides the verdict. The AI only explains it.**

- A deterministic scoring engine computes the score and verdict from real numbers. It never calls the AI, so it can never hallucinate a verdict and never returns a server error.
- The LLM sits on top purely to explain the verdict in natural language.
- If the LLM call fails — rate limit, network, malformed response — a rule-based explanation is used instead, so the endpoint always returns a usable answer.

This is a *deterministic safety floor*: the important output (the verdict) is guaranteed correct and reproducible; the AI is a graceful enhancement that degrades cleanly.

```
┌─────────────────────────────────────────────┐
│  CEILING — LLM explanation (can fail)       │
├─────────────────────────────────────────────┤
│  FLOOR — score + verdict + savings plan     │
│          (never fails, always reproducible) │
└─────────────────────────────────────────────┘
```

---

## How the score works

Everything hangs on one derived number: **disposable income**.

```
disposable income = monthly income − fixed expenses − savings target
```

That's what the user can actually spend this month without touching rent or breaking their savings goal.

The engine then measures the purchase against it:

```
ratio = price ÷ disposable income
```

A low ratio means easily affordable (high score); a high ratio means it strains the budget (low score). Two further signals adjust it:

- **Purchase type** — NEED / WANT / LUXURY. Necessities scored leniently, luxuries strictly.
- **Usage frequency** — DAILY / WEEKLY / MONTHLY / RARELY. Daily-use items are easier to justify.

The final 0–100 score maps to a verdict:

| Score | Verdict |
|-------|---------|
| ≥ 70 | **BUY** |
| 40–69 | **WAIT** |
| < 40 | **SKIP** |

When the verdict is WAIT, a separate calculator works out the shortfall, how many months to save it, and the target date.

---

## Tech stack

- **Java 21**, **Spring Boot 3.5**
- **PostgreSQL** with **Spring Data JPA / Hibernate**
- **Spring Security + JWT** — stateless authentication
- **BCrypt** password hashing
- **OpenAI API** for explanations, with rule-based fallback
- **Docker** (multi-stage build) · **Railway** deployment
- **Maven**
- **Front end:** hand-written HTML / CSS / vanilla JS, served as static resources
  from the same jar — no framework and no build step

---

## Architecture

A single Spring Boot service organised in clean layers:

```
Static      →  the web app (index.html / app.css / app.js), same origin
Controller  →  receives HTTP requests, delegates (no logic)
Service     →  business logic; translates between API and database shapes
Repository  →  database access (Spring Data JPA)
Entity      →  maps to database tables
DTO         →  the shape of data in and out of the API
```

The front end is a client of the public API and nothing more — it calls the same
endpoints Swagger does, with the same JWT. Nothing is exposed to it that isn't
already documented.

Two classes are pure, framework-free Java — no Spring, no database, no annotations:

- `AffordScoreCalculator` — numbers in, score out
- `SavingsPlanCalculator` — computes the savings timeline

Keeping these pure makes them trivially unit-testable and guarantees the scoring can never fail on a database or network error.

---

## API endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/demo` | — | Throwaway account with a profile already set, returns JWT |
| POST | `/api/auth/register` | — | Create an account, returns JWT |
| POST | `/api/auth/login` | — | Authenticate, returns JWT |
| GET | `/api/users/profile` | Bearer | Read the saved profile; `400` when none is set yet |
| PUT | `/api/users/profile` | Bearer | Set income bracket, fixed expenses, savings target |
| POST | `/api/decision` | Bearer | Submit a purchase, get a verdict |

### Example — making a decision

**Request**
```http
POST /api/decision
Authorization: Bearer <token>
Content-Type: application/json

{
  "itemName": "Sony headphones",
  "price": 30000,
  "category": "Electronics",
  "purchaseType": "WANT",
  "usageFrequency": "DAILY"
}
```

**Response**
```json
{
    "itemName": "Sony headphones",
    "price": 30000,
    "verdict": "WAIT",
    "affordScore": 65,
    "aiExplanation": "It looks like your desire for those Sony headphones is strong, but your current financial situation suggests it might be wise to hold off for now. With your monthly disposable income, it’s great to know you’re prioritizing your budget, and saving a bit more could allow you to enjoy that purchase without any stress. Patience can pay off, so maybe consider setting aside a little each month to make it even easier to treat yourself later!",
    "disposableIncome": 24000.00,
    "savingsPlan": {
        "shortfall": 6000.00,
        "monthlySavings": 24000.00,
        "monthsNeeded": 2,
        "targetDate": "2026-08-16"
    }
}
```

---

## What the user provides

Deliberately minimal.

**Once, at signup:**
- Income — chosen as a **bracket**, scored against the midpoint (lower friction than typing an exact salary)
- Fixed monthly expenses — as itemised amounts, or a single percentage of income
- Monthly savings target

**Per decision:**
- Item, price, category
- NEED / WANT / LUXURY
- *(Optional)* usage frequency and recurring cost — these default to sensible values if omitted

The design goal is **minimum input, maximum output** — the user types very little and gets a full verdict plus a plan.

---

## Security

- **Stateless JWT auth** — no server-side sessions, scales horizontally
- **BCrypt-hashed passwords** — never stored or returned in plain text
- **Response DTOs, never raw entities** — internal fields like the password hash are never exposed
- **Identity from the token, not the request body** — every authenticated request takes the user from the verified JWT
- **Secrets via environment variables** — nothing sensitive is committed to version control

---

## Running locally

**Prerequisites:** Java 21, Maven, Docker

```bash
# start PostgreSQL
docker run --name purchase-db \
  -e POSTGRES_USER=vaishnavi \
  -e POSTGRES_PASSWORD=password123 \
  -e POSTGRES_DB=purchasedecision \
  -p 5432:5432 -d postgres

# run
./mvnw spring-boot:run
```

That's it — the defaults in `application.properties` match the `docker run` above, so
nothing needs exporting to get a working local instance. `http://localhost:8080`
serves the web app, and `/docs` the Swagger UI.

Editing anything under `src/main/resources/static/` needs a restart to take effect —
the files are served from `target/classes`, not from the source tree.

Without `OPENAI_API_KEY` the decision endpoint still works; it just returns the
rule-based explanation instead of the LLM one.

**Overrides**, all optional locally and all required in production:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/purchasedecision
export DB_USERNAME=vaishnavi
export DB_PASSWORD=password123
export JWT_SECRET=<a-long-random-string-32-chars-minimum>   # required in production
export OPENAI_API_KEY=<your-openai-key>
```

The bundled `jwt.secret` fallback is a development convenience and is **not** safe
to deploy with — set `JWT_SECRET` in any real environment.

---

## Roadmap

**v1 — current**
- Auth, financial profile, deterministic scoring, savings plan, LLM explanation with fallback, deployment

**v2 — planned**
- LLM pre-fills usage frequency and recurring cost as *suggestions* the user confirms with one tap — reducing input while keeping the verdict deterministic, since the engine only ever scores confirmed values
- A chat interface so the user can add context ("this replaces a broken one I use daily") and get a refined verdict
- Transactional email delivering the savings plan, plus scheduled reminders as a savings goal approaches
- Unit test suite for the calculators

---

## Notes on the build

This project was built to be **genuinely understood rather than tutorial-copied** — every architectural decision (stateless auth, deterministic scoring, DTO boundaries, the AI fallback) was made deliberately and can be explained.

A few decisions worth calling out:

**Income as a bracket, not an exact figure.** People are cagey about typing an exact salary. The bracket enum carries its own midpoint, so the user picks a friendly range while the engine works with a concrete number.

**Months, not weeks, in the savings plan.** Salaries arrive monthly and the whole financial model is monthly — introducing weeks would force the user to mentally convert back to their actual budgeting cycle.

**The AI never touches the verdict.** It would have been easy to let the LLM infer usage frequency or recurring costs. That would make the same purchase produce different verdicts on different days, and an LLM outage would break the core feature rather than just the prose.
