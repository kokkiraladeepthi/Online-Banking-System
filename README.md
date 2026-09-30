# Online Banking System (MVP)

A simple, lightweight Online Banking MVP application built with **Spring Boot** and **MySQL/H2**, featuring a clean **HTML, CSS, and Vanilla JavaScript** frontend.

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
- **Frontend:** HTML5, CSS3, Vanilla JavaScript (No frameworks)
- **Build Tool:** Apache Maven
- **API Testing:** Postman / cURL / REST Client

---

## 3. Core Features

1. **Create Account:** Open an account with name, email, optional custom account number (auto-generated if empty), and initial balance.
2. **Account Details:** View account ID, account holder name, account number, and current balance.
3. **Deposit Money:** Add funds to the account, immediately updating balance and recording a transaction.
4. **Withdraw Money:** Withdraw funds with automated balance check. Rejects attempts to withdraw more than the available balance with an `Insufficient balance for withdrawal` error.
5. **Transaction History:** Stores and lists every deposit and withdrawal chronologically with type, amount, status, and timestamp.
6. **Frontend Web Interface:** Clean and responsive UI accessible directly at `http://localhost:8080/`.

---

## 4. Database Schema

The database consists of two core tables:

### `accounts` Table
| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for account |
| `account_number` | `VARCHAR(20)` | UNIQUE, NOT NULL | Account number (e.g. `ACC-12345678`) |
| `name` | `VARCHAR(100)` | NOT NULL | Account holder full name |
| `email` | `VARCHAR(150)` | UNIQUE, NOT NULL | Account holder email address |
| `balance` | `DECIMAL(19, 2)` | NOT NULL | Current account balance |

### `transactions` Table
| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique transaction identifier |
| `account_id` | `BIGINT` | FOREIGN KEY (`accounts.id`), NOT NULL | Associated account ID |
| `type` | `VARCHAR(20)` | NOT NULL | `DEPOSIT` or `WITHDRAW` |
| `amount` | `DECIMAL(19, 2)` | NOT NULL | Transaction amount |
| `status` | `VARCHAR(20)` | NOT NULL | `SUCCESS` |
| `created_at` | `TIMESTAMP` | NOT NULL | Timestamp when transaction occurred |

---

## 5. API Endpoints

Base URL: `http://localhost:8080/api`

### 5.1 Create Account
- **Method:** `POST`
- **Endpoint:** `/accounts`
- **Request Body:**
  ```json
  {
    "name": "John Doe",
    "email": "john.doe@example.com",
    "accountNumber": "ACC-1001",
    "initialBalance": 5000.00
  }
  ```
  *(Note: `accountNumber` is optional; if omitted, the system generates an `ACC-XXXXXXXX` code).*
- **Response (201 Created):**
  ```json
  {
    "id": 1,
    "accountNumber": "ACC-1001",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "balance": 5000.00
  }
  ```

### 5.2 View Account Details
- **Method:** `GET`
- **Endpoint:** `/accounts/{id}`
- **Response (200 OK):**
  ```json
  {
    "id": 1,
    "accountNumber": "ACC-1001",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "balance": 5000.00
  }
  ```

### 5.3 Deposit Money
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
    "balance": 7000.00
  }
  ```

### 5.4 Withdraw Money
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
    "balance": 5500.00
  }
  ```
- **Response (400 Bad Request - Insufficient Funds):**
  ```json
  {
    "message": "Insufficient balance for withdrawal"
  }
  ```

### 5.5 Transaction History
- **Method:** `GET`
- **Endpoint:** `/transactions/account/{accountId}`
- **Response (200 OK):**
  ```json
  [
    {
      "id": 2,
      "accountId": 1,
      "type": "WITHDRAW",
      "amount": 1500.00,
      "status": "SUCCESS",
      "createdAt": "2026-09-30T20:51:01.6705"
    },
    {
      "id": 1,
      "accountId": 1,
      "type": "DEPOSIT",
      "amount": 2000.00,
      "status": "SUCCESS",
      "createdAt": "2026-09-30T20:51:01.6384"
    }
  ]
  ```

---

## 6. How to Run the Project

### Prerequisites
- **Java JDK 17 or higher** installed (`java -version`).
- **Maven 3.8+** (optional if running via the pre-built JAR or IDE).
- **MySQL 8.x** (optional; the project is pre-configured with zero-setup embedded H2).

---

### Option A: One-Click / Pre-Built JAR (Easiest & Fastest)
You can run the packaged application immediately with just Java:

- **Windows (One-Click):**
  Double-click [`run.bat`](file:///d:/Deskpot/BANK/run.bat) in the project root folder.

- **Command Line (Terminal / PowerShell / Command Prompt):**
  ```bash
  java -jar target/online-banking-mvp-0.0.1-SNAPSHOT.jar
  ```

---

### Option B: Using Maven
If you prefer running through Maven:
```bash
mvn spring-boot:run
```
> **Tip for Windows users:** If your terminal shows `'mvn' is not recognized`, either add Maven to your system `PATH`, or use the packaged JAR / VS Code option below.

---

### Option C: Inside Visual Studio Code
1. Open the project folder in VS Code.
2. In the file explorer, navigate to:  
   `src/main/java/com/bank/mvp/OnlineBankingMvpApplication.java`
3. Click the **Run** button above the `main` method (or press `F5`).

---

### Step 2: Access the Application

Once the server has started (showing `Started OnlineBankingMvpApplication` on port `8080`):

1. **Frontend Web UI:**  
   Open your web browser and go to:  
   👉 **[http://localhost:8080/](http://localhost:8080/)**

2. **H2 Database Web Console (Optional):**  
   Visit: **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**  
   - JDBC URL: `jdbc:h2:mem:bankdb`  
   - User Name: `sa`  
   - Password: *(leave blank)*

---

### Step 3: (Optional) Switching to Local MySQL
The application runs out of the box using embedded H2 in MySQL compatibility mode. When you want to store data in your local MySQL database:

1. Open `src/main/resources/application.properties`.
2. Comment out the H2 datasource and uncomment the MySQL lines with your local password:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/online_banking_mvp?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=YOUR_LOCAL_MYSQL_PASSWORD
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
   ```
3. Restart the application.

---

## 7. How to Test Using Postman

1. Open **Postman** and create a new collection called `Banking MVP`.
2. Set the request header `Content-Type: application/json` for all POST requests.

### Test Sequence:
1. **Create Account:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/accounts`
   - Body (Raw JSON):
     ```json
     {
       "name": "Alice Smith",
       "email": "alice@example.com",
       "initialBalance": 5000.00
     }
     ```
   - Verify: Status `201 Created` with generated `id` (e.g., `1`).

2. **View Account:**
   - Method: `GET`
   - URL: `http://localhost:8080/api/accounts/1`
   - Verify: Status `200 OK` with balance `5000.00`.

3. **Deposit Money:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/accounts/1/deposit`
   - Body (Raw JSON):
     ```json
     {
       "amount": 2000.00
     }
     ```
   - Verify: Status `200 OK` with updated balance `7000.00`.

4. **Withdraw Money:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/accounts/1/withdraw`
   - Body (Raw JSON):
     ```json
     {
       "amount": 1000.00
     }
     ```
   - Verify: Status `200 OK` with updated balance `6000.00`.

5. **Test Insufficient Balance:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/accounts/1/withdraw`
   - Body (Raw JSON):
     ```json
     {
       "amount": 99999.00
     }
     ```
   - Verify: Status `400 Bad Request` with error message `"Insufficient balance for withdrawal"`.

6. **View Transaction History:**
   - Method: `GET`
   - URL: `http://localhost:8080/api/transactions/account/1`
   - Verify: Status `200 OK` returning an array of transactions in descending order of creation.