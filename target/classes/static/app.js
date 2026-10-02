// Base API URL configuration
const API_BASE = (window.location.protocol === 'http:' || window.location.protocol === 'https:')
    ? (window.location.port === '8080' ? '/api' : 'http://localhost:8080/api')
    : 'http://localhost:8080/api';

// State
let currentAccountId = null;
let loggedInUser = null;

// DOM Elements
const statusBanner = document.getElementById('statusBanner');
const statusText = document.getElementById('statusText');

// Auth DOM Elements
const loggedInUserBar = document.getElementById('loggedInUserBar');
const loggedInUserInfo = document.getElementById('loggedInUserInfo');
const authFormsContainer = document.getElementById('authFormsContainer');
const loginTabBtn = document.getElementById('loginTabBtn');
const registerTabBtn = document.getElementById('registerTabBtn');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const loginEmail = document.getElementById('loginEmail');
const loginPassword = document.getElementById('loginPassword');
const regName = document.getElementById('regName');
const regEmail = document.getElementById('regEmail');
const regPassword = document.getElementById('regPassword');
const regPhone = document.getElementById('regPhone');

// Account DOM Elements
const createAccountForm = document.getElementById('createAccountForm');
const createName = document.getElementById('createName');
const createEmail = document.getElementById('createEmail');
const createAccountNumber = document.getElementById('createAccountNumber');
const createInitialBalance = document.getElementById('createInitialBalance');
const createUserId = document.getElementById('createUserId');

const fetchAccountForm = document.getElementById('fetchAccountForm');
const lookupAccountId = document.getElementById('lookupAccountId');

const detailAccountId = document.getElementById('detailAccountId');
const detailName = document.getElementById('detailName');
const detailAccountNumber = document.getElementById('detailAccountNumber');
const detailUserId = document.getElementById('detailUserId');
const detailBalance = document.getElementById('detailBalance');

const depositForm = document.getElementById('depositForm');
const depositAmount = document.getElementById('depositAmount');

const withdrawForm = document.getElementById('withdrawForm');
const withdrawAmount = document.getElementById('withdrawAmount');

const transferForm = document.getElementById('transferForm');
const transferFrom = document.getElementById('transferFrom');
const transferTo = document.getElementById('transferTo');
const transferAmount = document.getElementById('transferAmount');
const transferDescription = document.getElementById('transferDescription');

const transactionTableBody = document.getElementById('transactionTableBody');

// Notification banner helpers
function showStatus(message, type = 'success') {
    statusBanner.className = `status-banner ${type}`;
    statusText.textContent = message;
    statusBanner.style.display = 'flex';
}

function hideStatus() {
    statusBanner.style.display = 'none';
    statusText.textContent = '';
}

// Formatters
function formatCurrency(val) {
    if (val === null || val === undefined) return '—';
    const num = Number(val);
    return isNaN(num) ? '—' : '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDateTime(isoString) {
    if (!isoString) return '—';
    const date = new Date(isoString);
    if (isNaN(date.getTime())) return isoString;
    return date.toLocaleString();
}

// Auth Tab Switching
function switchAuthTab(tab) {
    if (tab === 'login') {
        loginTabBtn.classList.add('active');
        registerTabBtn.classList.remove('active');
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
    } else {
        registerTabBtn.classList.add('active');
        loginTabBtn.classList.remove('active');
        registerForm.style.display = 'block';
        loginForm.style.display = 'none';
    }
}

// User Registration
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideStatus();

    const payload = {
        name: regName.value.trim(),
        email: regEmail.value.trim(),
        password: regPassword.value,
        phone: regPhone.value.trim() || null
    };

    try {
        const response = await fetch(`${API_BASE}/users/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'Registration failed.');
        }

        showStatus(`Registration successful for ${data.name}! You can now login.`, 'success');
        registerForm.reset();
        loginEmail.value = payload.email;
        switchAuthTab('login');
    } catch (err) {
        showStatus(err.message, 'error');
    }
});

// User Login
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideStatus();

    const payload = {
        email: loginEmail.value.trim(),
        password: loginPassword.value
    };

    try {
        const response = await fetch(`${API_BASE}/users/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'Login failed.');
        }

        loggedInUser = data.user;
        updateLoggedInUserView();
        showStatus(`Welcome back, ${loggedInUser.name}!`, 'success');
        loginForm.reset();

        // Check if user has accounts and load first one
        await loadUserAccounts(loggedInUser.id);
    } catch (err) {
        showStatus(err.message, 'error');
    }
});

function updateLoggedInUserView() {
    if (loggedInUser) {
        loggedInUserInfo.textContent = `👤 ${loggedInUser.name} (User ID: ${loggedInUser.id})`;
        loggedInUserBar.style.display = 'flex';
        authFormsContainer.style.display = 'none';
        createUserId.value = loggedInUser.id;
        createName.value = loggedInUser.name;
        createEmail.value = loggedInUser.email;
    } else {
        loggedInUserBar.style.display = 'none';
        authFormsContainer.style.display = 'block';
        createUserId.value = '';
    }
}

function logoutUser() {
    loggedInUser = null;
    updateLoggedInUserView();
    showStatus('Logged out successfully.', 'success');
}

async function loadUserAccounts(userId) {
    try {
        const res = await fetch(`${API_BASE}/users/${userId}/accounts`);
        const accounts = await res.json();
        if (res.ok && Array.isArray(accounts) && accounts.length > 0) {
            updateAccountDetailsView(accounts[0]);
            await loadTransactions(accounts[0].id);
        }
    } catch (e) {
        console.error('Error fetching user accounts', e);
    }
}

// Update Account Details UI
function updateAccountDetailsView(account) {
    if (!account) {
        detailAccountId.textContent = '—';
        detailName.textContent = '—';
        detailAccountNumber.textContent = '—';
        detailUserId.textContent = '—';
        detailBalance.textContent = '—';
        if (transferFrom) transferFrom.value = '';
        return;
    }

    currentAccountId = account.id;
    lookupAccountId.value = account.id;
    detailAccountId.textContent = account.id;
    detailName.textContent = account.name;
    detailAccountNumber.textContent = account.accountNumber;
    detailUserId.textContent = account.userId !== undefined && account.userId !== null ? account.userId : 'None';
    detailBalance.textContent = formatCurrency(account.balance);
    if (transferFrom) transferFrom.value = account.accountNumber || account.id;
}

// 1. Create Account
createAccountForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideStatus();

    const name = createName.value.trim();
    const email = createEmail.value.trim();
    const accountNumber = createAccountNumber.value.trim();
    const initialBalance = parseFloat(createInitialBalance.value);
    const userIdVal = createUserId.value ? parseInt(createUserId.value, 10) : null;

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
    if (userIdVal && !isNaN(userIdVal)) {
        payload.userId = userIdVal;
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

        createAccountForm.reset();
        if (loggedInUser) {
            createUserId.value = loggedInUser.id;
        }

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

// 5. Fund Transfer
if (transferForm) {
    transferForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideStatus();

        const fromVal = transferFrom.value.trim();
        const toVal = transferTo.value.trim();
        const amount = parseFloat(transferAmount.value);
        const description = transferDescription.value.trim();

        if (!fromVal) {
            showStatus('Sender account (ID or Number) is required.', 'error');
            return;
        }
        if (!toVal) {
            showStatus('Receiver account (ID or Number) is required.', 'error');
            return;
        }
        if (isNaN(amount) || amount <= 0) {
            showStatus('Transfer amount must be greater than zero.', 'error');
            return;
        }

        const payload = {
            amount: amount,
            description: description || null
        };

        if (/^\d+$/.test(fromVal)) {
            payload.fromAccountId = parseInt(fromVal, 10);
            payload.fromAccountNumber = fromVal;
        } else {
            payload.fromAccountNumber = fromVal;
        }

        if (/^\d+$/.test(toVal)) {
            payload.toAccountId = parseInt(toVal, 10);
            payload.toAccountNumber = toVal;
        } else {
            payload.toAccountNumber = toVal;
        }

        try {
            const response = await fetch(`${API_BASE}/transactions/transfer`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Transfer failed.');
            }

            showStatus(`Transfer of ${formatCurrency(amount)} successful! Ref ID: ${data.transactionId}. Sender Balance: ${formatCurrency(data.senderBalance)}`, 'success');
            transferAmount.value = '';
            transferDescription.value = '';

            // Reload active account details and transactions
            if (currentAccountId) {
                await loadAccountById(currentAccountId);
            }
        } catch (error) {
            showStatus(error.message || 'Transfer transaction failed.', 'error');
        }
    });
}

// 6. Transaction History
async function loadTransactions(accountId) {
    if (!accountId) {
        transactionTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="empty-state">No account selected. Create or load an account to view transactions.</td>
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
                    <td colspan="8" class="empty-state">No transactions recorded for this account.</td>
                </tr>
            `;
            return;
        }

        transactionTableBody.innerHTML = transactions.map(tx => {
            let badgeClass = 'DEPOSIT';
            if (tx.type === 'WITHDRAW') badgeClass = 'WITHDRAW';
            else if (tx.type === 'TRANSFER_OUT') badgeClass = 'TRANSFER_OUT';
            else if (tx.type === 'TRANSFER_IN') badgeClass = 'TRANSFER_IN';

            return `
                <tr>
                    <td>${tx.id}</td>
                    <td><span class="badge ${badgeClass}">${tx.type}</span></td>
                    <td>${formatCurrency(tx.amount)}</td>
                    <td>${tx.senderAccount || '—'}</td>
                    <td>${tx.receiverAccount || '—'}</td>
                    <td>${tx.description || '—'}</td>
                    <td><span class="badge SUCCESS">${tx.status}</span></td>
                    <td>${formatDateTime(tx.createdAt)}</td>
                </tr>
            `;
        }).join('');
    } catch (error) {
        transactionTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="empty-state">Unable to load transaction history: ${error.message}</td>
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
