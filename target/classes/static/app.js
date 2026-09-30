// Base API URL configuration
const API_BASE = (window.location.protocol === 'http:' || window.location.protocol === 'https:')
    ? (window.location.port === '8080' ? '/api' : 'http://localhost:8080/api')
    : 'http://localhost:8080/api';

// Current active account ID state
let currentAccountId = null;

// DOM Elements
const statusBanner = document.getElementById('statusBanner');
const statusText = document.getElementById('statusText');

const createAccountForm = document.getElementById('createAccountForm');
const createName = document.getElementById('createName');
const createEmail = document.getElementById('createEmail');
const createAccountNumber = document.getElementById('createAccountNumber');
const createInitialBalance = document.getElementById('createInitialBalance');

const fetchAccountForm = document.getElementById('fetchAccountForm');
const lookupAccountId = document.getElementById('lookupAccountId');

const detailAccountId = document.getElementById('detailAccountId');
const detailName = document.getElementById('detailName');
const detailAccountNumber = document.getElementById('detailAccountNumber');
const detailBalance = document.getElementById('detailBalance');

const depositForm = document.getElementById('depositForm');
const depositAmount = document.getElementById('depositAmount');

const withdrawForm = document.getElementById('withdrawForm');
const withdrawAmount = document.getElementById('withdrawAmount');

const transactionTableBody = document.getElementById('transactionTableBody');

// Helper to show notification banner
function showStatus(message, type = 'success') {
    statusBanner.className = `status-banner ${type}`;
    statusText.textContent = message;
    statusBanner.style.display = 'flex';
}

function hideStatus() {
    statusBanner.style.display = 'none';
    statusText.textContent = '';
}

// Helper to format currency
function formatCurrency(val) {
    if (val === null || val === undefined) return '—';
    const num = Number(val);
    return isNaN(num) ? '—' : '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// Helper to format ISO date
function formatDateTime(isoString) {
    if (!isoString) return '—';
    const date = new Date(isoString);
    if (isNaN(date.getTime())) return isoString;
    return date.toLocaleString();
}

// Update Account Details UI
function updateAccountDetailsView(account) {
    if (!account) {
        detailAccountId.textContent = '—';
        detailName.textContent = '—';
        detailAccountNumber.textContent = '—';
        detailBalance.textContent = '—';
        return;
    }

    currentAccountId = account.id;
    lookupAccountId.value = account.id;
    detailAccountId.textContent = account.id;
    detailName.textContent = account.name;
    detailAccountNumber.textContent = account.accountNumber;
    detailBalance.textContent = formatCurrency(account.balance);
}

// 1. Create Account
createAccountForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideStatus();

    const name = createName.value.trim();
    const email = createEmail.value.trim();
    const accountNumber = createAccountNumber.value.trim();
    const initialBalance = parseFloat(createInitialBalance.value);

    if (isNaN(initialBalance) || initialBalance < 0) {
        showStatus('Initial balance cannot be negative.', 'error');
        return;
    }

    const payload = {
        name: name,
        email: email,
        initialBalance: initialBalance
    };

    if (accountNumber) {
        payload.accountNumber = accountNumber;
    }

    try {
        const response = await fetch(`${API_BASE}/accounts`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Failed to create account.');
        }

        updateAccountDetailsView(data);
        showStatus(`Account created successfully! Account ID: ${data.id}, Account No: ${data.accountNumber}`, 'success');

        // Reset form inputs
        createAccountForm.reset();

        // Load transactions
        await loadTransactions(data.id);
    } catch (error) {
        showStatus(error.message || 'Error connecting to backend API.', 'error');
    }
});

// 2. Load Account Details by ID
fetchAccountForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideStatus();

    const id = lookupAccountId.value.trim();
    if (!id) {
        showStatus('Please enter a valid Account ID.', 'error');
        return;
    }

    await loadAccountById(id);
});

async function loadAccountById(id) {
    try {
        const response = await fetch(`${API_BASE}/accounts/${id}`);
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || `Account with ID ${id} not found.`);
        }

        updateAccountDetailsView(data);
        showStatus(`Account #${data.id} loaded successfully.`, 'success');
        await loadTransactions(data.id);
    } catch (error) {
        showStatus(error.message || 'Error fetching account details.', 'error');
    }
}

// 3. Deposit
depositForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideStatus();

    if (!currentAccountId) {
        showStatus('Please create or load an account first before depositing.', 'error');
        return;
    }

    const amount = parseFloat(depositAmount.value);
    if (isNaN(amount) || amount <= 0) {
        showStatus('Deposit amount must be greater than zero.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/accounts/${currentAccountId}/deposit`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ amount: amount })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Deposit failed.');
        }

        updateAccountDetailsView(data);
        showStatus(`Deposited ${formatCurrency(amount)} successfully! New Balance: ${formatCurrency(data.balance)}`, 'success');
        depositForm.reset();

        await loadTransactions(currentAccountId);
    } catch (error) {
        showStatus(error.message || 'Deposit transaction failed.', 'error');
    }
});

// 4. Withdraw
withdrawForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideStatus();

    if (!currentAccountId) {
        showStatus('Please create or load an account first before withdrawing.', 'error');
        return;
    }

    const amount = parseFloat(withdrawAmount.value);
    if (isNaN(amount) || amount <= 0) {
        showStatus('Withdraw amount must be greater than zero.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/accounts/${currentAccountId}/withdraw`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ amount: amount })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Withdrawal failed.');
        }

        updateAccountDetailsView(data);
        showStatus(`Withdrew ${formatCurrency(amount)} successfully! New Balance: ${formatCurrency(data.balance)}`, 'success');
        withdrawForm.reset();

        await loadTransactions(currentAccountId);
    } catch (error) {
        showStatus(error.message || 'Withdrawal transaction failed.', 'error');
    }
});

// 5. Transaction History
async function loadTransactions(accountId) {
    if (!accountId) {
        transactionTableBody.innerHTML = `
            <tr>
                <td colspan="5" class="empty-state">No account selected. Create or load an account to view transactions.</td>
            </tr>
        `;
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/transactions/account/${accountId}`);
        const transactions = await response.json();

        if (!response.ok) {
            throw new Error('Failed to load transaction history.');
        }

        if (!Array.isArray(transactions) || transactions.length === 0) {
            transactionTableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="empty-state">No transactions recorded for this account.</td>
                </tr>
            `;
            return;
        }

        transactionTableBody.innerHTML = transactions.map(tx => {
            const typeClass = tx.type === 'DEPOSIT' ? 'DEPOSIT' : 'WITHDRAW';
            return `
                <tr>
                    <td>${tx.id}</td>
                    <td><span class="badge ${typeClass}">${tx.type}</span></td>
                    <td>${formatCurrency(tx.amount)}</td>
                    <td><span class="badge SUCCESS">${tx.status}</span></td>
                    <td>${formatDateTime(tx.createdAt)}</td>
                </tr>
            `;
        }).join('');
    } catch (error) {
        transactionTableBody.innerHTML = `
            <tr>
                <td colspan="5" class="empty-state">Unable to load transaction history: ${error.message}</td>
            </tr>
        `;
    }
}

function reloadCurrentTransactions() {
    if (!currentAccountId) {
        showStatus('Please create or load an account first.', 'error');
        return;
    }
    loadTransactions(currentAccountId);
    showStatus('Transactions refreshed.', 'success');
}
