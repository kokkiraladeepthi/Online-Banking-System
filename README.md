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
- **Web Services:** Spring-WS (Spring Web Services 4.x), WSDL4J, SAAJ, Jakarta XML Binding (JAXB)
- **Database:** MySQL 8.x / Embedded H2 (MySQL compatibility mode)
- **Frontend:** HTML5, CSS3, Vanilla JavaScript (No external frameworks)
- **Build Tool:** Apache Maven
- **API Testing:** Postman / cURL / REST Client / SOAP Client

---

## 3. Core Features

1. **Create Account:** Open an account with name, email, optional custom account number (auto-generated if empty), and initial balance.
2. **Account Details:** View account ID, account holder name, account number, and current balance.
3. **Deposit Money:** Add funds to the account, immediately updating balance and recording a transaction.
4. **Withdraw Money:** Withdraw funds with automated balance check. Rejects attempts to withdraw more than the available balance with an `Insufficient balance for withdrawal` error.
5. **Fund Transfer:** Transfer money safely between accounts using atomic `@Transactional` processing. Automatically validates sender, receiver, balance, deducts from sender, adds to receiver, and logs both debit (`TRANSFER_OUT`) and credit (`TRANSFER_IN`) records.
6. **Transaction Management & History:** Stores and lists every deposit, withdrawal, and transfer chronologically with counterparty account numbers, description, type, amount, status, and timestamp.
7. **Personal Finance Analytics:** Summarizes deposits, withdrawals, transfers sent/received, net savings, and monthly aggregations without heavy external libraries.
8. **Savings Goals:** Set financial targets with goal names, target amounts, current saved amounts, and automatic completion tracking (`progressPercentage = currentAmount / targetAmount * 100`).
9. **Admin Module:** Dedicated administrative portal with system-wide KPI statistics, user management, accounts overview, transaction auditing, and tamper-evident activity logging.
10. **SOAP Web Service (Account Statements):** Contract-first SOAP 1.1 Web Service (`/ws`) publishing dynamic WSDL (`/ws/statement.wsdl`) to generate comprehensive account statements by account ID or account number from existing JPA records.
11. **Frontend Web Interface:** Clean and responsive UI accessible directly at `http://localhost:8080/`.
12. **Request Validation (`@Valid`):** Strict bean validation on DTOs rejecting null values, blank required strings, negative amounts, zero transaction amounts, and invalid email formats.
13. **Global Exception Handling (`@RestControllerAdvice`):** Unified error handling translating domain exceptions (`UserNotFoundException`, `AccountNotFoundException`, `InsufficientBalanceException`, `InvalidAmountException`, `DuplicateUserException`, `InvalidAccountException`, `SavingsGoalNotFoundException`) into clear JSON error payloads.
14. **AOP Logging (`@Aspect`):** Spring AspectJ interceptor monitoring key service operations (registration, login, accounts, deposits, withdrawals, transfers, savings goals, admin) with start/completion/failure execution tracking while sanitizing sensitive credentials.

---

## 4. Database Schema

The database consists of five core tables:

### `users` Table
| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| `name` | `VARCHAR(100)` | NOT NULL | User full name |
| `email` | `VARCHAR(150)` | UNIQUE, NOT NULL | User email address |
| `password` | `VARCHAR(255)` | NOT NULL | BCrypt encrypted password |
| `phone` | `VARCHAR(20)` | NULL | Optional contact phone number |
| `role` | `VARCHAR(30)` | NOT NULL | `CUSTOMER` or `ADMIN` (default `CUSTOMER`) |
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

### `savings_goals` Table
| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique goal identifier |
| `account_id` | `BIGINT` | FOREIGN KEY (`accounts.id`), NOT NULL | Associated account ID |
| `goal_name` | `VARCHAR(100)` | NOT NULL | Name/purpose of the goal |
| `target_amount` | `DECIMAL(19, 2)` | NOT NULL | Target savings amount |
| `current_amount` | `DECIMAL(19, 2)` | NOT NULL | Current saved amount (default 0.00) |
| `target_date` | `DATE` | NULL | Optional deadline for savings goal |
| `status` | `VARCHAR(20)` | NOT NULL | `IN_PROGRESS` or `COMPLETED` |
| `created_at` | `TIMESTAMP` | NOT NULL | Goal creation timestamp |

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

### `admin_logs` Table
| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique log entry identifier |
| `action` | `VARCHAR(100)` | NOT NULL | Action name (e.g. `VIEW_DASHBOARD`, `VIEW_USERS`) |
| `performed_by` | `VARCHAR(100)` | NOT NULL | Username or email of administrator |
| `details` | `VARCHAR(500)` | NULL | Descriptive details of operation |
| `timestamp` | `TIMESTAMP` | NOT NULL | Timestamp when action was logged |

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

### 5.10 Personal Finance Analytics
- **Method:** `GET`
- **Endpoint:** `/analytics/account/{accountId}`
- **Response (200 OK):**
  ```json
  {
    "accountId": 1,
    "accountNumber": "ACC-1001",
    "currentBalance": 10000.00,
    "totalDeposits": 10000.00,
    "totalWithdrawals": 3000.00,
    "totalTransfersSent": 2000.00,
    "totalTransfersReceived": 0.00,
    "totalTransfers": 2000.00,
    "totalMoneyReceived": 10000.00,
    "totalMoneySpent": 5000.00,
    "netSavings": 5000.00,
    "transactionCount": 3,
    "monthlySummary": [
      {
        "month": "2026-10",
        "totalDeposits": 10000.00,
        "totalWithdrawals": 3000.00,
        "totalTransfersSent": 2000.00,
        "totalTransfersReceived": 0.00,
        "inflow": 10000.00,
        "outflow": 5000.00,
        "transactionCount": 3
      }
    ]
  }
  ```

### 5.11 Savings Goals
- **Create Goal:** `POST /savings-goals`
  - Body:
    ```json
    {
      "accountId": 1,
      "goalName": "New Laptop",
      "targetAmount": 50000.00,
      "currentAmount": 10000.00,
      "targetDate": "2026-12-31"
    }
    ```
  - Response: `201 Created` with `progressPercentage: 20.0%`, `status: "IN_PROGRESS"`.
- **View Goal by ID:** `GET /savings-goals/{id}` (Returns single goal)
- **View Goals by Account:** `GET /savings-goals/account/{accountId}` (Returns array of goals for account)
- **Update Goal / Add Funds:** `PUT /savings-goals/{id}`
  - Body:
    ```json
    {
      "currentAmount": 50000.00
    }
    ```
  - Automatically transitions `status` to `"COMPLETED"` when target amount is reached.
- **Delete Goal:** `DELETE /savings-goals/{id}` (Returns `200 OK`)

### 5.12 Admin Module
- **Admin Dashboard Overview:** `GET /admin/dashboard`
  - Response:
    ```json
    {
      "totalUsers": 2,
      "totalAccounts": 3,
      "totalTransactions": 12,
      "totalDeposits": 55000.00,
      "totalWithdrawals": 12000.00,
      "totalTransfers": 8500.00,
      "totalSystemBalance": 43000.00,
      "totalSavingsGoals": 4
    }
    ```
- **View All Users:** `GET /admin/users` (Returns array of users with IDs, names, emails, phones, roles; passwords omitted)
- **View All Accounts:** `GET /admin/accounts` (Returns array of all bank accounts)
- **View All Transactions:** `GET /admin/transactions` (Returns array of system transactions chronologically)
- **View Activity Logs:** `GET /admin/logs` (Returns audit trail of administrative accesses and operations)

### 5.13 SOAP Web Service (Account Statements)
- **Endpoint URL:** `POST http://localhost:8080/ws`
- **WSDL Definition URL:** `GET http://localhost:8080/ws/statement.wsdl`
- **Target Namespace:** `http://com.bank.mvp/soap/statement`
- **Protocol:** SOAP 1.1 with SAAJ and Jakarta XML Binding
- **Request Format (`getStatementRequest`):** Lookup statement by `accountId` OR `accountNumber`:
  ```xml
  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                    xmlns:tns="http://com.bank.mvp/soap/statement">
      <soapenv:Header/>
      <soapenv:Body>
          <tns:getStatementRequest>
              <tns:accountNumber>ACC-ALICE-1</tns:accountNumber>
          </tns:getStatementRequest>
      </soapenv:Body>
  </soapenv:Envelope>
  ```
- **Response Format (`getStatementResponse`):**
  ```xml
  <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
      <SOAP-ENV:Header/>
      <SOAP-ENV:Body>
          <ns2:getStatementResponse xmlns:ns2="http://com.bank.mvp/soap/statement">
              <ns2:accountNumber>ACC-ALICE-1</ns2:accountNumber>
              <ns2:accountHolder>Alice Smith</ns2:accountHolder>
              <ns2:currentBalance>3700.00</ns2:currentBalance>
              <ns2:transactionDetails>
                  <ns2:id>3</ns2:id>
                  <ns2:type>WITHDRAW</ns2:type>
                  <ns2:amount>300.00</ns2:amount>
                  <ns2:date>2026-10-03T00:52:45</ns2:date>
                  <ns2:status>SUCCESS</ns2:status>
                  <ns2:senderAccount>ACC-ALICE-1</ns2:senderAccount>
                  <ns2:description>ATM Withdrawal</ns2:description>
              </ns2:transactionDetails>
              <ns2:transactionDetails>
                  <ns2:id>2</ns2:id>
                  <ns2:type>DEPOSIT</ns2:type>
                  <ns2:amount>500.00</ns2:amount>
                  <ns2:date>2026-10-03T00:50:12</ns2:date>
                  <ns2:status>SUCCESS</ns2:status>
                  <ns2:receiverAccount>ACC-ALICE-1</ns2:receiverAccount>
                  <ns2:description>Cash Deposit</ns2:description>
              </ns2:transactionDetails>
          </ns2:getStatementResponse>
      </SOAP-ENV:Body>
  </SOAP-ENV:Envelope>
  ```

---

## 6. How to Test Using Postman

1. Open **Postman** and create a new collection called `Banking MVP`.
2. Set the request header `Content-Type: application/json` for all REST POST/PUT requests, and `Content-Type: text/xml` for SOAP requests.

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

10. **View Personal Finance Analytics:**
    - Method: `GET`
    - URL: `http://localhost:8080/api/analytics/account/1`
    - Verify: Status `200 OK` returning summary metrics: `totalDeposits`, `totalWithdrawals`, `totalTransfers`, `totalMoneyReceived`, `totalMoneySpent`, `netSavings`, `transactionCount`, and `monthlySummary`.

11. **Savings Goals Lifecycle:**
    - **Create Goal:** `POST http://localhost:8080/api/savings-goals` with `{"accountId": 1, "goalName": "Car Down Payment", "targetAmount": 10000.00, "currentAmount": 2500.00}` -> Verify `201 Created` with progress `25.0%`.
    - **View Goals:** `GET http://localhost:8080/api/savings-goals/account/1` -> Verify list returned.
    - **Update Goal (Reach Target):** `PUT http://localhost:8080/api/savings-goals/1` with `{"currentAmount": 10000.00}` -> Verify status transitions to `COMPLETED` with `100.0%`.
    - **Delete Goal:** `DELETE http://localhost:8080/api/savings-goals/1` -> Verify `200 OK`.

12. **Admin Module Operations:**
    - **Admin Login:** `POST http://localhost:8080/api/users/login` with `{"email": "admin@bank.com", "password": "admin123"}` -> Verify role is `ADMIN`.
    - **System Dashboard Stats:** `GET http://localhost:8080/api/admin/dashboard` -> Verify counts of users, accounts, transactions, deposits, withdrawals, transfers, and system balance.
    - **List All Users:** `GET http://localhost:8080/api/admin/users` -> Verify all users returned without password fields.
    - **List All Accounts:** `GET http://localhost:8080/api/admin/accounts` -> Verify all accounts returned.
    - **List All Transactions:** `GET http://localhost:8080/api/admin/transactions` -> Verify system-wide transaction history.
    - **Audit Activity Logs:** `GET http://localhost:8080/api/admin/logs` -> Verify tamper-evident log records.

13. **SOAP Web Service Account Statement:**
    - **Inspect WSDL in Browser:**
      - Open: `http://localhost:8080/ws/statement.wsdl`
      - Verify the dynamic WSDL XML document is returned with port types, bindings, operations, and schema.
    - **Test in Postman:**
      - Method: `POST`
      - URL: `http://localhost:8080/ws`
      - Header: `Content-Type: text/xml`
      - Body (`raw` -> `XML`):
        ```xml
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:tns="http://com.bank.mvp/soap/statement">
            <soapenv:Header/>
            <soapenv:Body>
                <tns:getStatementRequest>
                    <tns:accountNumber>ACC-ALICE-1</tns:accountNumber>
                </tns:getStatementRequest>
            </soapenv:Body>
        </soapenv:Envelope>
        ```
      - Verify: Returns `200 OK` with full SOAP XML response including account holder name, balance, and complete transaction history.
    - **Test in Terminal (cURL):**
      ```bash
      curl -X POST http://localhost:8080/ws \
        -H "Content-Type: text/xml;charset=UTF-8" \
        -d "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tns=\"http://com.bank.mvp/soap/statement\"><soapenv:Header/><soapenv:Body><tns:getStatementRequest><tns:accountId>1</tns:accountId></tns:getStatementRequest></soapenv:Body></soapenv:Envelope>"
      ```
    - **Test in Web Browser UI:**
      - Open: `http://localhost:8080/`
      - Scroll to **Section 9: SOAP Web Service: Account Statement**
      - Enter Account ID or Account Number and click **"Fetch Statement (SOAP)"**
      - See the account holder, balance, transaction table, and expand the raw SOAP XML response viewer.