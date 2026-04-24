# Library Management System (LMS)

A production-ready Library Management System built specifically for testing with JUnit 5 and JMeter.

## Tech Stack
- **Backend:** Java 17, Spring Boot 3.2.5, Spring Data JPA, Spring Security (JWT), MySQL 8.
- **Frontend:** React 18, Vite, Tailwind CSS v4, Lucide Icons.
- **Database:** MySQL.
- **Build Tool:** Maven.

## Key Features
- **Authentication:** JWT-based login with Librarian and Member roles.
- **Borrowing Logic:** 
  - Limit of 5 books per member.
  - Due date tracking (14 days).
  - Fine calculation: $1.00 per day overdue.
- **Search:** ISBN (exact), Title (partial), and Category filters.
- **Librarian View:** Full CRUD for book inventory.
- **Dashboard:** Personal statistics for members.

## Project Structure
- `/backend`: Spring Boot application.
- `/frontend`: React application.

## How to Run

### 1. Database Setup
Create a MySQL database named `lms_db`.
```sql
CREATE DATABASE lms_db;
```
Ensure your MySQL user is `root` with password `password`, or update `backend/src/main/resources/application.properties`.

### 2. Run Backend
```bash
cd backend
mvn spring-boot:run
```
The server will start on `http://localhost:8080`.

### 3. Run Frontend
```bash
cd frontend
npm install
npm run dev
```
The app will be available on `http://localhost:5173`.

### 4. Testing Accounts
- **Librarian:** `librarian` / `password123`
- **Member:** `member1` / `password123`

## Testing (Ready for JMeter/JUnit)
The system is designed with a clean Service layer (`BorrowServiceImpl`) and REST API endpoints returning consistent JSON responses, making it ideal for:
- **Unit Testing:** Isolate `BorrowServiceImpl` and `BookServiceImpl`.
- **Load Testing:** Use `data.sql` to seed 10 books and 5 members for concurrent borrow/return scenarios.
