# 📚 Library Management System (LMS)

A professional-grade Library Management System designed for robust data handling, secure authentication, and advanced testing scenarios. This system is purpose-built for demonstrating **Unit Testing (JUnit 5)** and **Performance/Stress Testing (JMeter)**.

---

## 🚀 Tech Stack

| Component | Technology | Version |
| :--- | :--- | :--- |
| **Backend** | Java / Spring Boot | 3.2.5 |
| **Frontend** | React / Vite | 18 |
| **Database** | MySQL | 8.x |
| **Security** | Spring Security / JWT | - |
| **Styling** | Tailwind CSS v4 | - |
| **Build Tool** | Maven / NPM | - |

---

## ✨ Key Features

- **🔐 Secure Authentication:** Multi-role system (Librarian & Member) handled via JWT tokens.
- **📖 Inventory Management:** Full CRUD operations for books with ISBN validation.
- **🔄 Smart Borrowing:**
  - Automatic due date calculation (14 days).
  - Fine management ($1.00/day).
  - Concurrent borrow handling with stock protection.
- **🔍 Advanced Search:** Filter library by ISBN, Title, and Category.
- **📊 Member Dashboard:** Real-time personal stats and transaction history.

---

## 📂 Project Structure

```text
ASE-PROJECT/
├── backend/            # Spring Boot REST API
│   ├── src/main/java   # Java source code
│   └── pom.xml         # Maven dependencies
├── frontend/           # Vite + React UI
│   ├── src/            # Components & Logic
│   └── package.json    # Node dependencies
└── README.md           # Documentation
```

---

## 🛠️ Installation & Setup

### 1. Database Configuration
1. Create a database named `lms_db` in your MySQL server.
2. Update the credentials in `backend/src/main/resources/application.properties`.
   ```properties
   spring.datasource.username=root
   spring.datasource.password=YOUR_PASSWORD
   ```

### 2. Launching the Backend
```bash
cd backend
mvn clean spring-boot:run
```
*Port: `http://localhost:8081` (default) or `8080`*

### 3. Launching the Frontend
```bash
cd frontend
npm install
npm run dev
```
*Access: `http://localhost:5173`*

---

## 📡 API Documentation

### Authentication
- `POST /api/auth/login` - Authenticate and receive JWT.

### Book Management
- `GET /api/books` - List all books (paged).
- `POST /api/books` - Add new book (Librarian only).
- `PUT /api/books/{id}` - Update book details (Librarian only).

### Member & Borrowing
- `POST /api/borrow/{bookId}?memberId={id}` - Borrow a book.
- `POST /api/return/{bookId}?memberId={id}` - Return a book.
- `GET /api/members/{id}/history` - View borrow history.
- `GET /api/fines/{id}` - Check outstanding fines.

---

## 🧪 Testing & Quality Assurance

### 1. JUnit 5 (Unit Testing)
The service layer (`BorrowServiceImpl`) is isolated for testing business logic like fine calculation and member eligibility.
Run tests: `mvn test`

### 2. JMeter (Performance Testing)
Profiles included for LMS analysis:

#### **Spike Test (User Burst)**
- **Scenario:** 300 users borrowing simultaneously.
- **Goal:** Verify thread safety and stock management.
- **Ramp-up:** 5 seconds.

#### **Stress Test (Breaking Point)**
- **Scenario:** 500-1000 users logging in simultaneously.
- **Goal:** Find the hardware limits for BCrypt hashing.
- **Ramp-up:** 300 seconds.

> [!TIP]
> Use the `Aggregate Report` listener in JMeter to capture the 95th Percentile ($p95$) for your report.

---

## 🔐 Credentials (Seeded Data)
| Role | Username | Password |
| :--- | :--- | :--- |
| **Librarian** | `librarian` | `password123` |
| **Member** | `member1` | `password123` |

---

## 🚩 Troubleshooting

- **CORS Errors:** If you change the backend port, update the `baseURL` in `frontend/src/api/api.js`.
- **Port 8080 in use:** Change `server.port` in `application.properties` to `8081`.
- **Invalid Credentials:** Ensure the `DataInitializer` has run successfully (check logs for "LMS INITIALIZATION READY").
