# Online Banking System (MVP)

A simple, lightweight Online Banking MVP application built with **Spring Boot** and **MySQL / Embedded H2**, featuring a clean **HTML, CSS, and Vanilla JavaScript** frontend.

---

## 🚀 How to Run the Project (Step-by-Step)

Follow these simple steps to run and use the project on your machine:

### 1. Prerequisites
- **Java 17 or higher** installed (`java -version`)
- A modern web browser (Chrome, Edge, Firefox, etc.)
- Maven 3.8+ *(optional — not required if using the one-click script or VS Code)*

---

### 2. Start the Backend Server

Choose **any one** of the following 3 options to start the application:

#### Option 1: One-Click Startup (Easiest for Windows)
Simply double-click the **`run.bat`** file located in the project root folder.  
*(This runs the pre-packaged JAR file directly using Java).*

---

#### Option 2: Using Terminal / Command Prompt
Open your terminal (PowerShell, Command Prompt, or Bash) in the project root folder and run:

- **Using Java JAR (Fastest):**
  ```bash
  java -jar target/online-banking-mvp-0.0.1-SNAPSHOT.jar
  ```
- **Using Maven (Source code):**
  ```bash
  mvn spring-boot:run
  ```

---

#### Option 3: Directly in Visual Studio Code
1. Open the project folder in **VS Code**.
2. In the Explorer, open:  
   `src/main/java/com/bank/mvp/OnlineBankingMvpApplication.java`
3. Click the **Run** button located above `public static void main` (or press **F5**).

---

### 3. Open the Frontend Application

Once the terminal outputs:
```
Tomcat started on port 8080 (http) with context path '/'
Started OnlineBankingMvpApplication ...
```

Open your web browser and go to:
👉 **[http://localhost:8080/](http://localhost:8080/)**

The clean MVP frontend will open, ready to:
1. Create a new bank account.
2. View account balance and details.
3. Perform deposits.
4. Perform withdrawals (with overdraft protection).
5. View transaction logs in real time.

---

### 4. Database Setup & Configuration

The application is pre-configured to run with **zero setup** using an embedded database in MySQL compatibility mode (`jdbc:h2:mem:bankdb;MODE=MySQL`).

#### (Optional) H2 Database Web Console:
- URL: **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**
- JDBC URL: `jdbc:h2:mem:bankdb`
- User Name: `sa`
- Password: *(leave empty)*

#### (Optional) Switching to your local MySQL Server:
If you want to persist data to a local MySQL instance:
1. Open `src/main/resources/application.properties`.
2. Comment out the H2 settings and uncomment the MySQL section:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/online_banking_mvp?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
   ```
3. Restart the application.

---

## 📋 Table of Contents
1. [Objective](#1-objective)
2. [Technology Stack](#2-technology-stack)
3. [Core Features](#3-core-features)
4. [Database Schema](#4-database-schema)
5. [API Endpoints](#5-api-endpoints)
6. [How to Test Using Postman](#6-how-to-test-using-postman)

---

## 1. Objective

Provide a functional Minimum Viable Product (MVP) that allows users to:
- Open a bank account with an initial deposit.
- View real-time account details and current balance.
- Deposit funds into their account.
- Withdraw funds with validation against overdrafts.
- View a chronological history of all account transactions.

---

## 2. Technology Stack

- **Backend:** Java 17+, Spring Boot 3.3.x, Spring Data JPA, Hibernate, Bean Validation (Jakarta)
- **Database:** MySQL 8.x / Embedded H2 (MySQL compatibility mode)
- **Frontend:** HTML5, CSS3, Vanilla JavaScript (No external frameworks)
- **Build Tool:** Apache Maven
- **API Testing:** Postman / cURL / REST Client

---

## 3. Core Features

1. **Create Account:** Open an account with name, email, optional custom account number (auto-generated if empty), and initial balance.
2. **Account Details:** View account ID, account holder name, account number, and current balance.
3. **Deposit Money:** Add funds to the account, immediately updating balance and recording a transaction.
4. **Withdraw Money:** Withdraw funds with automated balance check. Rejects attempts to withdraw more than the available balance with an `Insufficient balance for withdrawal` error.
5. **Fund Transfer:** Transfer money safely between accounts using atomic `@Transactional` processing. Automatically validates sender, receiver, balance, deducts from sender, adds to receiver, and logs both debit (`TRANSFER_OUT`) and credit (`TRANSFER_IN`) records.
6. **Transaction Management & History:** Stores and lists every deposit, withdrawal, and transfer chronologically with counterparty account numbers, description, type, amount, status, and timestamp.
7. **Frontend Web Interface:** Clean and responsive UI accessible directly at `http://localhost:8080/`.

---

## 4. Database Schema

The database consists of three core tables:

### `users` Table
| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| `name` | `VARCHAR(100)` | NOT NULL | User full name |
| `email` | `VARCHAR(150)` | UNIQUE, NOT NULL | User email address |
| `password` | `VARCHAR(255)` | NOT NULL | BCrypt encrypted password |
| `phone` | `VARCHAR(20)` | NULL | Optional contact phone number |
| `created_at` | `TIMESTAMP` | NOT NULL | Registration timestamp |

### `accounts` Table
| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for account |
| `user_id` | `BIGINT` | FOREIGN KEY (`users.id`), NULL | Associated user (optional) |
| `account_number` | `VARCHAR(20)` | UNIQUE, NOT NULL | Account number (e.g. `ACC-12345678`) |
| `name` | `VARCHAR(100)` | NOT NULL | Account holder full name |
| `email` | `VARCHAR(150)` | UNIQUE, NOT NULL | Account holder email address |
| `balance` | `DECIMAL(19, 2)` | NOT NULL | Current account balance |

### `transactions` Table
| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique transaction identifier |
| `account_id` | `BIGINT` | FOREIGN KEY (`accounts.id`), NOT NULL | Associated account ID |
| `type` | `VARCHAR(20)` | NOT NULL | `DEPOSIT`, `WITHDRAW`, `TRANSFER_OUT`, `TRANSFER_IN` |
| `amount` | `DECIMAL(19, 2)` | NOT NULL | Transaction amount |
| `status` | `VARCHAR(20)` | NOT NULL | `SUCCESS` |
| `sender_account` | `VARCHAR(30)` | NULL | Sender account number |
| `receiver_account` | `VARCHAR(30)` | NULL | Receiver account number |
| `description` | `VARCHAR(255)` | NULL | Transaction remarks/purpose |
| `created_at` | `TIMESTAMP` | NOT NULL | Timestamp when transaction occurred |

---

## 5. API Endpoints

Base URL: `http://localhost:8080/api`

### 5.1 User Registration
- **Method:** `POST`
- **Endpoint:** `/users/register`
- **Request Body:**
  ```json
  {
    "name": "David Miller",
    "email": "david.miller@example.com",
    "password": "securePassword123",
    "phone": "9876501234"
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "id": 1,
    "name": "David Miller",
    "email": "david.miller@example.com",
    "phone": "9876501234",
    "createdAt": "2026-10-02T23:36:25.521"
  }
  ```

### 5.2 User Login
- **Method:** `POST`
- **Endpoint:** `/users/login`
- **Request Body:**
  ```json
  {
    "email": "david.miller@example.com",
    "password": "securePassword123"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Login successful",
    "user": {
      "id": 1,
      "name": "David Miller",
      "email": "david.miller@example.com",
      "phone": "9876501234",
      "createdAt": "2026-10-02T23:36:25.521"
    }
  }
  ```

### 5.3 Get User's Accounts
- **Method:** `GET`
- **Endpoint:** `/users/{id}/accounts`
- **Response (200 OK):**
  ```json
  [
    {
      "id": 1,
      "accountNumber": "ACC-DAVE-01",
      "name": "David Checking",
      "email": "david.checking@example.com",
      "balance": 10000.00,
      "userId": 1
    }
  ]
  ```

### 5.4 Create Account
- **Method:** `POST`
- **Endpoint:** `/accounts`
- **Request Body:**
  ```json
  {
    "name": "John Doe",
    "email": "john.doe@example.com",
    "accountNumber": "ACC-1001",
    "initialBalance": 5000.00,
    "userId": 1
  }
  ```
  *(Note: `accountNumber` and `userId` are optional).*
- **Response (201 Created):**
  ```json
  {
    "id": 1,
    "accountNumber": "ACC-1001",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "balance": 5000.00,
    "userId": 1
  }
  ```

### 5.5 View Account Details
- **Method:** `GET`
- **Endpoint:** `/accounts/{id}`
- **Response (200 OK):**
  ```json
  {
    "id": 1,
    "accountNumber": "ACC-1001",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "balance": 5000.00,
    "userId": 1
  }
  ```

### 5.6 Deposit Money
- **Method:** `POST`
- **Endpoint:** `/accounts/{id}/deposit`
- **Request Body:**
  ```json
  {
    "amount": 2000.00
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "id": 1,
    "accountNumber": "ACC-1001",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "balance": 7000.00,
    "userId": 1
  }
  ```

### 5.7 Withdraw Money
- **Method:** `POST`
- **Endpoint:** `/accounts/{id}/withdraw`
- **Request Body:**
  ```json
  {
    "amount": 1500.00
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "id": 1,
    "accountNumber": "ACC-1001",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "balance": 5500.00,
    "userId": 1
  }
  ```
- **Response (400 Bad Request - Insufficient Funds):**
  ```json
  {
    "message": "Insufficient balance for withdrawal"
  }
  ```

### 5.8 Fund Transfer
- **Method:** `POST`
- **Endpoint:** `/transactions/transfer` (also aliases to `/accounts/transfer`)
- **Request Body (By Account ID or Account Number):**
  ```json
  {
    "fromAccountId": 1,
    "toAccountId": 2,
    "amount": 1500.00,
    "description": "Monthly rent payment"
  }
  ```
  *(Alternatively, specify `"fromAccountNumber": "ACC-1001"` and `"toAccountNumber": "ACC-2002"`)*
- **Response (200 OK):**
  ```json
  {
    "message": "Transfer successful",
    "transactionId": 1,
    "fromAccountId": 1,
    "fromAccountNumber": "ACC-1001",
    "toAccountId": 2,
    "toAccountNumber": "ACC-2002",
    "amount": 1500.00,
    "senderBalance": 3500.00,
    "status": "SUCCESS",
    "timestamp": "2026-10-02T23:57:11.017"
  }
  ```
- **Error Responses:**
  - `400 Bad Request` (Insufficient balance):
    ```json
    { "message": "Insufficient balance for fund transfer" }
    ```
  - `400 Bad Request` (Invalid amount):
    ```json
    { "message": "Transfer amount must be greater than zero" }
    ```
  - `400 Bad Request` (Same account):
    ```json
    { "message": "Cannot transfer money to the same account" }
    ```
  - `404 Not Found` (Account does not exist):
    ```json
    { "message": "Sender account not found with id: 999" }
    ```

### 5.9 Transaction History
- **Method:** `GET`
- **Endpoint:** `/transactions/account/{accountId}`
- **Response (200 OK):**
  ```json
  [
    {
      "id": 3,
      "accountId": 1,
      "type": "TRANSFER_OUT",
      "amount": 1500.00,
      "status": "SUCCESS",
      "createdAt": "2026-10-02T23:57:11.017",
      "senderAccount": "ACC-1001",
      "receiverAccount": "ACC-2002",
      "description": "Monthly rent payment"
    },
    {
      "id": 2,
      "accountId": 1,
      "type": "WITHDRAW",
      "amount": 500.00,
      "status": "SUCCESS",
      "createdAt": "2026-10-02T22:30:10.120",
      "senderAccount": "ACC-1001",
      "receiverAccount": null,
      "description": "Withdrawal"
    },
    {
      "id": 1,
      "accountId": 1,
      "type": "DEPOSIT",
      "amount": 2000.00,
      "status": "SUCCESS",
      "createdAt": "2026-10-02T22:15:00.450",
      "senderAccount": null,
      "receiverAccount": "ACC-1001",
      "description": "Deposit"
    }
  ]
  ```

---

## 6. How to Test Using Postman

1. Open **Postman** and create a new collection called `Banking MVP`.
2. Set the request header `Content-Type: application/json` for all POST requests.

### Test Sequence:
1. **User Registration:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/users/register`
   - Body: `{"name":"Alice Smith","email":"alice@example.com","password":"password123","phone":"9876543210"}`
   - Verify: Status `201 Created`.

2. **User Login:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/users/login`
   - Body: `{"email":"alice@example.com","password":"password123"}`
   - Verify: Status `200 OK` with user details.

3. **Create Sender Account:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/accounts`
   - Body: `{"name":"Alice Smith","email":"alice@example.com","accountNumber":"ACC-ALICE-1","initialBalance":5000.00,"userId":1}`
   - Verify: Status `201 Created` with generated account ID.

4. **Create Receiver Account:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/accounts`
   - Body: `{"name":"Bob Jones","email":"bob@example.com","accountNumber":"ACC-BOB-1","initialBalance":1000.00}`
   - Verify: Status `201 Created`.

5. **Transfer Money (Fund Transfer):**
   - Method: `POST`
   - URL: `http://localhost:8080/api/transactions/transfer`
   - Body:
     ```json
     {
       "fromAccountId": 1,
       "toAccountId": 2,
       "amount": 1500.00,
       "description": "Payment for services"
     }
     ```
   - Verify: Status `200 OK`, `senderBalance` reduced by 1500 to `3500.00`.

6. **Deposit Money:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/accounts/1/deposit`
   - Body: `{"amount": 500.00}`
   - Verify: Status `200 OK` with updated balance `4000.00`.

7. **Withdraw Money:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/accounts/1/withdraw`
   - Body: `{"amount": 300.00}`
   - Verify: Status `200 OK` with updated balance `3700.00`.

8. **Test Insufficient Balance on Transfer:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/transactions/transfer`
   - Body: `{"fromAccountId": 1, "toAccountId": 2, "amount": 99999.00}`
   - Verify: Status `400 Bad Request` with `"Insufficient balance for fund transfer"`.

9. **View Transaction History:**
   - Method: `GET`
   - URL: `http://localhost:8080/api/transactions/account/1`
   - Verify: Status `200 OK` returning complete transaction history with `TRANSFER_OUT`, `DEPOSIT`, `WITHDRAW`, counterparty accounts, descriptions, and timestamps.