# Finance Pro — Personal Finance Dashboard

A premium personal-finance dashboard built with **Java Spring Boot**, **Thymeleaf**, **Bootstrap 5**, and **Chart.js**.

**No database required.** All data — users, expenses, income, transactions, and savings goals — is persisted to a single human-readable `project.json` file in the project root.

---

## Tech Stack

| Layer       | Technology |
|-------------|------------|
| Language    | Java 17    |
| Framework   | Spring Boot 3.2, Spring MVC, Spring Security |
| Persistence | Jackson-backed JSON file (`project.json`) |
| Frontend    | HTML5, CSS3, JavaScript (ES6+), Bootstrap 5.3, Chart.js 4 |
| Templates   | Thymeleaf |
| Build       | Maven |

---

## Prerequisites

- **Java 17+** — `java -version`
- **Maven 3.8+** — `mvn -version`

That's it — no MySQL, no database setup.

---

## Quick Start

```bash
mvn clean install
mvn spring-boot:run
```

The application starts at **http://localhost:8080**

On first launch the app writes `project.json` next to the working directory and seeds it with two demo users plus a handful of sample expense, income, and savings-goal records.

---

## Default Login

| Username | Password |
|----------|----------|
| `admin`  | `admin123` |
| `demo`   | `admin123` |

---

## How persistence works

All persistence is funnelled through `com.financepro.storage.JsonDataStore`:

- Loads `project.json` at startup into an in-memory `RootDocument`.
- Hands out auto-incremented `id` values per collection (users, expenses, incomes, transactions, savings_goals).
- Wraps every read / write in a single `ReentrantLock`.
- Persists atomically on every mutation: writes to `project.json.tmp` and `Files.move(...)` over the target.

Repositories under `com.financepro.repository.*` are thin wrappers around this store and expose the exact surface area the services need (e.g. `sumByCategoryAndUserId`, `findFiltered`).

### Inspecting / hand-editing the data

`project.json` is pretty-printed by default — you can open it in any editor and tweak values; the app will pick them up on the next restart. Configure the file path or formatting with:

```properties
app.data.file=project.json
app.data.pretty-print=true
```

---

## Features

### Money handling

- All amounts are entered and displayed in **Indian Rupees (₹)**.
- Expense and income amounts are validated as **strictly positive** at three layers:
  - HTML5 input: `type="number" min="0.01"` and a small `oninput` guard that strips leading `-` characters.
  - Server-side bean validation: `@DecimalMin("0.01")` on the DTO.
  - Service layer: each create/update calls `amount.abs()` and rejects zero.

### Dashboard
- Animated stat cards (Total Income, Monthly Budget, Remaining Balance)
- Color-coded balance, animated counters
- Category cards (Food, Travel, Shopping, Bills) with percentages
- Budget health bar
- Doughnut + Bar charts
- Savings goals + recent transactions

### Analytics
- Monthly trend line chart
- Category distribution doughnut + weekly bar
- Category breakdown table

### Expenses / Income / Transactions
- Full CRUD on the Expense and Income pages.
- Every Expense / Income write also appends a row to the unified Transaction log.
- Transactions page: searchable, type/category filters, paginated (15 per page), CSV export.

### Settings
- Profile (full name, currency)
- Dark mode (persisted in localStorage)
- Password change

### Security
- Spring Security form login
- BCrypt-encoded passwords (the seed users use freshly encoded `admin123`)
- CSRF protection on all forms
- 7-day remember-me cookie

---

## Project Structure

```
finance-pro/
├── pom.xml
├── README.md
├── project.json                  ← created on first run, all data lives here
└── src/main/
    ├── java/com/financepro/
    │   ├── FinanceProApplication.java
    │   ├── config/               ← Spring Security + UserDetails
    │   ├── controller/           ← MVC + REST controllers
    │   ├── service/              ← Business logic
    │   ├── repository/           ← JSON-backed repositories (no JPA)
    │   ├── storage/
    │   │   ├── JsonDataStore.java   ← Reads/writes project.json
    │   │   └── DataInitializer.java ← First-run seed
    │   ├── entity/               ← Plain POJOs (Lombok @Data)
    │   ├── dto/                  ← DTOs + PagedResult
    │   └── exception/            ← Global error handler
    └── resources/
        ├── application.properties
        ├── templates/            ← Thymeleaf views
        └── static/css|js         ← Styles + main.js
```

---

## REST API (cookie-authenticated)

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/api/dashboard/summary`   | Dashboard aggregated data |
| GET    | `/api/expenses`            | List user expenses |
| POST   | `/api/expenses`            | Create expense |
| PUT    | `/api/expenses/{id}`       | Update expense |
| DELETE | `/api/expenses/{id}`       | Delete expense |
| GET    | `/api/income`              | List user income |
| POST   | `/api/income`              | Create income |
| PUT    | `/api/income/{id}`         | Update income |
| DELETE | `/api/income/{id}`         | Delete income |
| GET    | `/api/transactions/recent` | Last 10 transactions |

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Port 8080 in use | Add `server.port=9090` to `application.properties` |
| Want to wipe everything | Stop the app and delete `project.json`; restart — it will be re-seeded |
| Custom data location | Set `app.data.file=/absolute/path/to/data.json` in `application.properties` |
| Want minified JSON | Set `app.data.pretty-print=false` |

---

## License

MIT — free for personal and commercial use.
