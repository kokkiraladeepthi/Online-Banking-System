# PRD — Online Banking System MVP

## 1. Project Name

Online Banking System

## 2. Project Goal

Build a simple banking application using Spring Boot and MySQL
that allows a user to manage a bank account and perform basic
banking operations.

---

## 3. MVP Features

### 3.1 Create Account

The user can create a bank account.

Required details:

- Name
- Email
- Account Number
- Initial Balance

API:

POST /api/accounts

---

### 3.2 View Account

The user can view their account details and current balance.

API:

GET /api/accounts/{id}

---

### 3.3 Deposit Money

The user can deposit money into their account.

Example:

Balance = ₹5,000
Deposit = ₹2,000

New Balance = ₹7,000

API:

POST /api/accounts/{id}/deposit

---

### 3.4 Withdraw Money

The user can withdraw money from their account.

The system must check whether sufficient balance is available.

Example:

Balance = ₹7,000
Withdraw = ₹2,000

New Balance = ₹5,000

If the amount is greater than the balance:

"Insufficient balance"

API:

POST /api/accounts/{id}/withdraw

---

### 3.5 Transaction History

The system stores every deposit and withdrawal.

Example:

| ID | Type | Amount | Status |
|---|---|---:|---|
| 1 | DEPOSIT | ₹5,000 | SUCCESS |
| 2 | WITHDRAW | ₹1,000 | SUCCESS |

API:

GET /api/transactions/account/{accountId}

---

# 4. Database

Use MySQL.

## Account Table

- id
- account_number
- name
- email
- balance

## Transaction Table

- id
- account_id
- type
- amount
- status
- created_at

---

# 5. Technology Stack

Backend:

- Java
- Spring Boot
- Spring Data JPA
- Maven

Database:

- MySQL

API Testing:

- Postman

Frontend:

- Simple HTML
- CSS
- JavaScript

---

# 6. Required Spring Features

The project should demonstrate:

- REST Controller
- Spring Data JPA
- @RequestBody
- @PathVariable
- @Valid
- @RestControllerAdvice
- Basic exception handling

---

# 7. API List

| Method | Endpoint | Purpose |
|---|---|---|
| POST | /api/accounts | Create account |
| GET | /api/accounts/{id} | View account |
| POST | /api/accounts/{id}/deposit | Deposit |
| POST | /api/accounts/{id}/withdraw | Withdraw |
| GET | /api/transactions/account/{id} | Transaction history |

---

# 8. Simple Application Flow

Create Account
      ↓
View Balance
      ↓
Deposit / Withdraw
      ↓
Balance Updated
      ↓
Transaction Recorded
      ↓
View Transaction History

---

# 9. Out of Scope

The following features are NOT required for the MVP:

- Login
- User authentication
- Fund transfer
- Admin module
- Personal finance analytics
- Savings goals
- SOAP service
- OTP
- Notifications
- Payment gateway
- Advanced security
- Complex dashboard

---

# 10. MVP Success Criteria

The project is complete when:

1. A user can create an account.
2. Account details can be viewed.
3. Money can be deposited.
4. Money can be withdrawn.
5. Insufficient balance is handled.
6. Transactions are stored in MySQL.
7. Transaction history can be viewed.
8. APIs work correctly in Postman.
9. Basic validation and exception handling work.

---

# 11. Future Enhancements

Future versions may include:

- User Login
- Fund Transfer
- Admin Module
- SOAP Account Statement
- Personal Finance Analytics
- Savings Goals
- Advanced Security