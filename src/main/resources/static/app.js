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

// Analytics DOM Elements
const analyticsDeposits = document.getElementById('analyticsDeposits');
const analyticsWithdrawals = document.getElementById('analyticsWithdrawals');
const analyticsTransfers = document.getElementById('analyticsTransfers');
const analyticsCount = document.getElementById('analyticsCount');
const analyticsReceived = document.getElementById('analyticsReceived');
const analyticsSpent = document.getElementById('analyticsSpent');
const analyticsNet = document.getElementById('analyticsNet');

// Savings Goals DOM Elements
const createGoalForm = document.getElementById('createGoalForm');
const goalName = document.getElementById('goalName');
const goalTargetAmount = document.getElementById('goalTargetAmount');
const goalCurrentAmount = document.getElementById('goalCurrentAmount');
const goalTargetDate = document.getElementById('goalTargetDate');
const savingsGoalTableBody = document.getElementById('savingsGoalTableBody');

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
        const roleLabel = loggedInUser.role === 'ADMIN' ? '🛡️ [ADMIN] ' : '👤 ';
        loggedInUserInfo.textContent = `${roleLabel}${loggedInUser.name} (User ID: ${loggedInUser.id})`;
        loggedInUserBar.style.display = 'flex';
        authFormsContainer.style.display = 'none';
        createUserId.value = loggedInUser.id;
        createName.value = loggedInUser.name;
        createEmail.value = loggedInUser.email;

        // Auto-open admin view if admin logged in
        if (loggedInUser.role === 'ADMIN') {
            const adminSection = document.getElementById('adminSection');
            if (adminSection && adminSection.style.display === 'none') {
                toggleAdminView();
            }
        }
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
            await loadSavingsGoals(accounts[0].id);
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
        resetAnalyticsView();
        if (savingsGoalTableBody) {
            savingsGoalTableBody.innerHTML = `<tr><td colspan="8" class="empty-state">No account selected. Create or load an account to view savings goals.</td></tr>`;
        }
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
    const soapAccountInput = document.getElementById('soapAccountInput');
    if (soapAccountInput) soapAccountInput.value = account.accountNumber || account.id;
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
        await loadSavingsGoals(data.id);
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
        await loadSavingsGoals(data.id);
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
        loadAnalytics(accountId);
    } catch (error) {
        transactionTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="empty-state">Unable to load transaction history: ${error.message}</td>
            </tr>
        `;
    }
}

async function loadAnalytics(accountId) {
    if (!accountId) {
        resetAnalyticsView();
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/analytics/account/${accountId}`);
        const data = await response.json();

        if (!response.ok) {
            resetAnalyticsView();
            return;
        }

        if (analyticsDeposits) analyticsDeposits.textContent = formatCurrency(data.totalDeposits);
        if (analyticsWithdrawals) analyticsWithdrawals.textContent = formatCurrency(data.totalWithdrawals);
        if (analyticsTransfers) analyticsTransfers.textContent = formatCurrency(data.totalTransfers);
        if (analyticsCount) analyticsCount.textContent = data.transactionCount !== undefined ? data.transactionCount : 0;
        if (analyticsReceived) analyticsReceived.textContent = formatCurrency(data.totalMoneyReceived);
        if (analyticsSpent) analyticsSpent.textContent = formatCurrency(data.totalMoneySpent);
        if (analyticsNet) analyticsNet.textContent = formatCurrency(data.netSavings);
    } catch (e) {
        console.error('Error fetching analytics:', e);
        resetAnalyticsView();
    }
}

function resetAnalyticsView() {
    if (analyticsDeposits) analyticsDeposits.textContent = '₹0.00';
    if (analyticsWithdrawals) analyticsWithdrawals.textContent = '₹0.00';
    if (analyticsTransfers) analyticsTransfers.textContent = '₹0.00';
    if (analyticsCount) analyticsCount.textContent = '0';
    if (analyticsReceived) analyticsReceived.textContent = '₹0.00';
    if (analyticsSpent) analyticsSpent.textContent = '₹0.00';
    if (analyticsNet) analyticsNet.textContent = '₹0.00';
}

function reloadCurrentAnalytics() {
    if (!currentAccountId) {
        showStatus('Please create or load an account first.', 'error');
        return;
    }
    loadAnalytics(currentAccountId);
    showStatus('Analytics refreshed.', 'success');
}

function reloadCurrentTransactions() {
    if (!currentAccountId) {
        showStatus('Please create or load an account first.', 'error');
        return;
    }
    loadTransactions(currentAccountId);
    showStatus('Transactions refreshed.', 'success');
}

// 7. Savings Goals Logic
if (createGoalForm) {
    createGoalForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideStatus();

        if (!currentAccountId) {
            showStatus('Please create or load an account first before creating a savings goal.', 'error');
            return;
        }

        const name = goalName.value.trim();
        const target = parseFloat(goalTargetAmount.value);
        const current = goalCurrentAmount.value ? parseFloat(goalCurrentAmount.value) : 0;
        const targetDateVal = goalTargetDate.value || null;

        if (!name) {
            showStatus('Goal name is required.', 'error');
            return;
        }
        if (isNaN(target) || target <= 0) {
            showStatus('Target amount must be greater than zero.', 'error');
            return;
        }
        if (isNaN(current) || current < 0) {
            showStatus('Current amount cannot be negative.', 'error');
            return;
        }

        const payload = {
            accountId: currentAccountId,
            goalName: name,
            targetAmount: target,
            currentAmount: current,
            targetDate: targetDateVal
        };

        try {
            const response = await fetch(`${API_BASE}/savings-goals`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to create savings goal.');
            }

            showStatus(`Savings goal "${data.goalName}" created successfully! Status: ${data.status} (${data.progressPercentage.toFixed(1)}%)`, 'success');
            createGoalForm.reset();
            await loadSavingsGoals(currentAccountId);
        } catch (error) {
            showStatus(error.message || 'Error creating savings goal.', 'error');
        }
    });
}

async function loadSavingsGoals(accountId) {
    if (!savingsGoalTableBody) return;
    if (!accountId) {
        savingsGoalTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="empty-state">No account selected. Create or load an account to view savings goals.</td>
            </tr>
        `;
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/savings-goals/account/${accountId}`);
        const goals = await response.json();

        if (!response.ok) {
            throw new Error('Failed to load savings goals.');
        }

        if (!Array.isArray(goals) || goals.length === 0) {
            savingsGoalTableBody.innerHTML = `
                <tr>
                    <td colspan="8" class="empty-state">No savings goals created for this account yet.</td>
                </tr>
            `;
            return;
        }

        savingsGoalTableBody.innerHTML = goals.map(goal => {
            const pct = typeof goal.progressPercentage === 'number' ? goal.progressPercentage : 0;
            const isCompleted = goal.status === 'COMPLETED' || pct >= 100;
            const cappedWidth = Math.min(Math.max(pct, 0), 100);

            return `
                <tr>
                    <td>${goal.id}</td>
                    <td><strong>${escapeHtml(goal.goalName)}</strong></td>
                    <td>${formatCurrency(goal.targetAmount)}</td>
                    <td>${formatCurrency(goal.currentAmount)}</td>
                    <td class="progress-cell">
                        <div class="progress-bar-bg">
                            <div class="progress-bar-fill ${isCompleted ? 'completed' : ''}" style="width: ${cappedWidth}%;"></div>
                        </div>
                        <span class="progress-text">${pct.toFixed(1)}%</span>
                    </td>
                    <td>${goal.targetDate || '—'}</td>
                    <td><span class="badge ${goal.status}">${goal.status}</span></td>
                    <td>
                        <div class="action-btns">
                            <button type="button" class="btn-action-sm btn-add-fund" onclick="addFundsToGoal(${goal.id}, ${goal.currentAmount})">+ Save</button>
                            <button type="button" class="btn-action-sm btn-delete-goal" onclick="deleteSavingsGoal(${goal.id})">Delete</button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    } catch (error) {
        savingsGoalTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="empty-state">Unable to load savings goals: ${error.message}</td>
            </tr>
        `;
    }
}

async function addFundsToGoal(goalId, currentAmount) {
    const input = prompt(`Enter additional amount to save towards Goal #${goalId}:`);
    if (input === null) return;
    const additional = parseFloat(input);
    if (isNaN(additional) || additional <= 0) {
        showStatus('Please enter a valid positive amount.', 'error');
        return;
    }

    const newAmount = Number((currentAmount + additional).toFixed(2));
    try {
        const response = await fetch(`${API_BASE}/savings-goals/${goalId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ currentAmount: newAmount })
        });
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'Failed to update savings goal.');
        }
        showStatus(`Added ${formatCurrency(additional)} to goal "${data.goalName}". Current: ${formatCurrency(data.currentAmount)} (${data.status})`, 'success');
        if (currentAccountId) {
            await loadSavingsGoals(currentAccountId);
        }
    } catch (err) {
        showStatus(err.message, 'error');
    }
}

async function deleteSavingsGoal(goalId) {
    if (!confirm(`Are you sure you want to delete Savings Goal #${goalId}?`)) return;

    try {
        const response = await fetch(`${API_BASE}/savings-goals/${goalId}`, {
            method: 'DELETE'
        });
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'Failed to delete savings goal.');
        }
        showStatus('Savings goal deleted successfully.', 'success');
        if (currentAccountId) {
            await loadSavingsGoals(currentAccountId);
        }
    } catch (err) {
        showStatus(err.message, 'error');
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>'"]/g, 
        tag => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[tag] || tag));
}

function reloadCurrentSavingsGoals() {
    if (!currentAccountId) {
        showStatus('Please create or load an account first.', 'error');
        return;
    }
    loadSavingsGoals(currentAccountId);
    showStatus('Savings goals refreshed.', 'success');
}

// 8. Admin Module Logic
let currentAdminTab = 'users';

function toggleAdminView() {
    const adminSection = document.getElementById('adminSection');
    if (!adminSection) return;

    if (adminSection.style.display === 'none' || !adminSection.style.display) {
        adminSection.style.display = 'block';
        loadAdminDashboardData();
        adminSection.scrollIntoView({ behavior: 'smooth' });
    } else {
        adminSection.style.display = 'none';
    }
}

function switchAdminTab(tab) {
    currentAdminTab = tab;
    const tabs = ['users', 'accounts', 'transactions', 'logs'];

    tabs.forEach(t => {
        const btn = document.getElementById(`admin${capitalize(t)}TabBtn`);
        const view = document.getElementById(`admin${capitalize(t)}View`);
        if (btn) btn.classList.toggle('active', t === tab);
        if (view) view.style.display = (t === tab) ? 'block' : 'none';
    });

    if (tab === 'users') loadAdminUsers();
    else if (tab === 'accounts') loadAdminAccounts();
    else if (tab === 'transactions') loadAdminTransactions();
    else if (tab === 'logs') loadAdminLogs();
}

function capitalize(s) {
    return s.charAt(0).toUpperCase() + s.slice(1);
}

async function loadAdminDashboardData() {
    try {
        const res = await fetch(`${API_BASE}/admin/dashboard`);
        if (!res.ok) throw new Error('Failed to load admin dashboard stats');
        const stats = await res.json();

        const uEl = document.getElementById('adminTotalUsers');
        const aEl = document.getElementById('adminTotalAccounts');
        const tEl = document.getElementById('adminTotalTransactions');
        const bEl = document.getElementById('adminTotalBalance');
        const dEl = document.getElementById('adminTotalDeposits');
        const wEl = document.getElementById('adminTotalWithdrawals');
        const trEl = document.getElementById('adminTotalTransfers');
        const gEl = document.getElementById('adminTotalGoals');

        if (uEl) uEl.textContent = stats.totalUsers;
        if (aEl) aEl.textContent = stats.totalAccounts;
        if (tEl) tEl.textContent = stats.totalTransactions;
        if (bEl) bEl.textContent = formatCurrency(stats.totalSystemBalance);
        if (dEl) dEl.textContent = formatCurrency(stats.totalDeposits);
        if (wEl) wEl.textContent = formatCurrency(stats.totalWithdrawals);
        if (trEl) trEl.textContent = formatCurrency(stats.totalTransfers);
        if (gEl) gEl.textContent = stats.totalSavingsGoals;

        // Load active sub-tab data
        switchAdminTab(currentAdminTab);
    } catch (err) {
        showStatus('Error loading admin dashboard: ' + err.message, 'error');
    }
}

async function loadAdminUsers() {
    const tbody = document.getElementById('adminUsersTableBody');
    if (!tbody) return;

    try {
        const res = await fetch(`${API_BASE}/admin/users`);
        if (!res.ok) throw new Error('Failed to load users');
        const users = await res.json();

        if (!users || users.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No users registered in system.</td></tr>`;
            return;
        }

        tbody.innerHTML = users.map(u => `
            <tr>
                <td>${u.id}</td>
                <td><strong>${escapeHtml(u.name)}</strong></td>
                <td>${escapeHtml(u.email)}</td>
                <td>${escapeHtml(u.phone) || '—'}</td>
                <td><span class="badge ${u.role}">${u.role}</span></td>
                <td>${formatDateTime(u.createdAt)}</td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state">Error: ${err.message}</td></tr>`;
    }
}

async function loadAdminAccounts() {
    const tbody = document.getElementById('adminAccountsTableBody');
    if (!tbody) return;

    try {
        const res = await fetch(`${API_BASE}/admin/accounts`);
        if (!res.ok) throw new Error('Failed to load accounts');
        const accounts = await res.json();

        if (!accounts || accounts.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No accounts found in system.</td></tr>`;
            return;
        }

        tbody.innerHTML = accounts.map(a => `
            <tr>
                <td>${a.id}</td>
                <td><strong>${a.accountNumber}</strong></td>
                <td>${escapeHtml(a.name)}</td>
                <td>${escapeHtml(a.email)}</td>
                <td class="balance-value" style="font-size: 0.95rem;">${formatCurrency(a.balance)}</td>
                <td>${a.userId !== undefined && a.userId !== null ? a.userId : 'None'}</td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state">Error: ${err.message}</td></tr>`;
    }
}

async function loadAdminTransactions() {
    const tbody = document.getElementById('adminTransactionsTableBody');
    if (!tbody) return;

    try {
        const res = await fetch(`${API_BASE}/admin/transactions`);
        if (!res.ok) throw new Error('Failed to load transactions');
        const txs = await res.json();

        if (!txs || txs.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="empty-state">No transactions recorded in system.</td></tr>`;
            return;
        }

        tbody.innerHTML = txs.map(tx => `
            <tr>
                <td>${tx.id}</td>
                <td>${tx.accountId || '—'}</td>
                <td><span class="badge ${tx.type}">${tx.type}</span></td>
                <td>${formatCurrency(tx.amount)}</td>
                <td>${tx.senderAccount || '—'}</td>
                <td>${tx.receiverAccount || '—'}</td>
                <td>${escapeHtml(tx.description) || '—'}</td>
                <td><span class="badge ${tx.status}">${tx.status}</span></td>
                <td>${formatDateTime(tx.createdAt)}</td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="9" class="empty-state">Error: ${err.message}</td></tr>`;
    }
}

async function loadAdminLogs() {
    const tbody = document.getElementById('adminLogsTableBody');
    if (!tbody) return;

    try {
        const res = await fetch(`${API_BASE}/admin/logs`);
        if (!res.ok) throw new Error('Failed to load admin activity logs');
        const logs = await res.json();

        if (!logs || logs.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="empty-state">No activity logs recorded.</td></tr>`;
            return;
        }

        tbody.innerHTML = logs.map(l => `
            <tr>
                <td>${l.id}</td>
                <td><span class="badge ${l.action}">${l.action}</span></td>
                <td><strong>${escapeHtml(l.performedBy)}</strong></td>
                <td>${escapeHtml(l.details) || '—'}</td>
                <td>${formatDateTime(l.timestamp)}</td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty-state">Error: ${err.message}</td></tr>`;
    }
}

// 9. SOAP Account Statement Logic
const soapStatementForm = document.getElementById('soapStatementForm');
const soapAccountInput = document.getElementById('soapAccountInput');
const soapStatementResult = document.getElementById('soapStatementResult');
const soapResAccountNo = document.getElementById('soapResAccountNo');
const soapResHolder = document.getElementById('soapResHolder');
const soapResBalance = document.getElementById('soapResBalance');
const soapTransactionsTableBody = document.getElementById('soapTransactionsTableBody');
const soapRawRequest = document.getElementById('soapRawRequest');
const soapRawResponse = document.getElementById('soapRawResponse');

if (soapStatementForm) {
    soapStatementForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideStatus();

        const inputVal = soapAccountInput.value.trim();
        if (!inputVal) {
            showStatus('Please enter an Account ID or Account Number for the SOAP statement.', 'error');
            return;
        }

        const isNumeric = /^\d+$/.test(inputVal);
        const innerElement = isNumeric
            ? `<tns:accountId>${inputVal}</tns:accountId>`
            : `<tns:accountNumber>${escapeXml(inputVal)}</tns:accountNumber>`;

        const soapRequestXml = `<?xml version="1.0" encoding="utf-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:tns="http://com.bank.mvp/soap/statement">
    <soapenv:Header/>
    <soapenv:Body>
        <tns:getStatementRequest>
            ${innerElement}
        </tns:getStatementRequest>
    </soapenv:Body>
</soapenv:Envelope>`;

        if (soapRawRequest) soapRawRequest.textContent = soapRequestXml;

        try {
            const response = await fetch('/ws', {
                method: 'POST',
                headers: {
                    'Content-Type': 'text/xml;charset=UTF-8',
                    'SOAPAction': ''
                },
                body: soapRequestXml
            });

            const responseText = await response.text();
            if (soapRawResponse) soapRawResponse.textContent = responseText;

            const parser = new DOMParser();
            const xmlDoc = parser.parseFromString(responseText, 'text/xml');

            const fault = xmlDoc.getElementsByTagName('faultstring')[0]
                || xmlDoc.getElementsByTagName('SOAP-ENV:faultstring')[0];
            if (fault) {
                throw new Error(fault.textContent || 'SOAP Fault returned by server.');
            }

            const accNoEl = xmlDoc.getElementsByTagName('accountNumber')[0]
                || xmlDoc.getElementsByTagNameNS('http://com.bank.mvp/soap/statement', 'accountNumber')[0];
            const holderEl = xmlDoc.getElementsByTagName('accountHolder')[0]
                || xmlDoc.getElementsByTagNameNS('http://com.bank.mvp/soap/statement', 'accountHolder')[0];
            const balEl = xmlDoc.getElementsByTagName('currentBalance')[0]
                || xmlDoc.getElementsByTagNameNS('http://com.bank.mvp/soap/statement', 'currentBalance')[0];

            if (!accNoEl || !holderEl || !balEl) {
                throw new Error('Invalid SOAP response structure.');
            }

            soapResAccountNo.textContent = accNoEl.textContent;
            soapResHolder.textContent = holderEl.textContent;
            soapResBalance.textContent = formatCurrency(parseFloat(balEl.textContent));

            const txEls = xmlDoc.getElementsByTagName('transactionDetails').length > 0
                ? xmlDoc.getElementsByTagName('transactionDetails')
                : xmlDoc.getElementsByTagNameNS('http://com.bank.mvp/soap/statement', 'transactionDetails');

            if (txEls.length === 0) {
                soapTransactionsTableBody.innerHTML = `<tr><td colspan="8" class="empty-state">No transactions found for this account.</td></tr>`;
            } else {
                let rowsHtml = '';
                for (let i = 0; i < txEls.length; i++) {
                    const tx = txEls[i];
                    const id = getXmlVal(tx, 'id');
                    const type = getXmlVal(tx, 'type');
                    const amount = parseFloat(getXmlVal(tx, 'amount'));
                    const status = getXmlVal(tx, 'status');
                    const date = getXmlVal(tx, 'date');
                    const sender = getXmlVal(tx, 'senderAccount');
                    const receiver = getXmlVal(tx, 'receiverAccount');
                    const desc = getXmlVal(tx, 'description');

                    rowsHtml += `
                        <tr>
                            <td>${id}</td>
                            <td><span class="badge ${type}">${type}</span></td>
                            <td>${formatCurrency(amount)}</td>
                            <td><span class="badge ${status}">${status}</span></td>
                            <td>${formatDateTime(date)}</td>
                            <td>${sender || '—'}</td>
                            <td>${receiver || '—'}</td>
                            <td>${escapeHtml(desc) || '—'}</td>
                        </tr>
                    `;
                }
                soapTransactionsTableBody.innerHTML = rowsHtml;
            }

            soapStatementResult.style.display = 'block';
            showStatus(`SOAP statement generated successfully for Account ${accNoEl.textContent}!`, 'success');
        } catch (err) {
            soapStatementResult.style.display = 'none';
            showStatus('SOAP Error: ' + err.message, 'error');
        }
    });
}

function getXmlVal(parent, tag) {
    const el = parent.getElementsByTagName(tag)[0]
        || parent.getElementsByTagNameNS('http://com.bank.mvp/soap/statement', tag)[0];
    return el ? el.textContent : '';
}

function escapeXml(unsafe) {
    if (!unsafe) return '';
    return unsafe.replace(/[<>&'"]/g, c => {
        switch (c) {
            case '<': return '&lt;';
            case '>': return '&gt;';
            case '&': return '&amp;';
            case '\'': return '&apos;';
            case '"': return '&quot;';
        }
    });
}



