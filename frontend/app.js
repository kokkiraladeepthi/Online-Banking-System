// ========================================================
// Online Banking System - Frontend Application Logic
// ========================================================

// Base API URL configuration
const API_BASE = (window.location.protocol === 'http:' || window.location.protocol === 'https:')
    ? (window.location.port === '8080' ? '/api' : 'http://localhost:8080/api')
    : 'http://localhost:8080/api';

// Global State
let currentAccount = null;
let loggedInUser = null;
let currentAdminTab = 'users';

// Notification banner helpers
const statusBanner = document.getElementById('statusBanner');
const statusText = document.getElementById('statusText');

function showStatus(message, type = 'success') {
    if (!statusBanner || !statusText) return;
    statusBanner.className = `status-banner ${type}`;
    statusText.textContent = message;
    statusBanner.style.display = 'flex';
    // Auto-scroll to top so status is immediately visible
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function hideStatus() {
    if (!statusBanner) return;
    statusBanner.style.display = 'none';
    if (statusText) statusText.textContent = '';
}

// Button Loading State Helper
function setButtonLoading(button, isLoading, normalText = null) {
    if (!button) return;
    if (isLoading) {
        button.dataset.originalText = button.textContent;
        button.textContent = 'Processing...';
        button.disabled = true;
        button.classList.add('btn-loading');
    } else {
        button.textContent = normalText || button.dataset.originalText || 'Submit';
        button.disabled = false;
        button.classList.remove('btn-loading');
    }
}

// Formatters
function formatCurrency(val) {
    if (val === null || val === undefined || isNaN(Number(val))) return '—';
    const num = Number(val);
    return '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDateTime(isoString) {
    if (!isoString) return '—';
    const date = new Date(isoString);
    if (isNaN(date.getTime())) return isoString;
    return date.toLocaleString();
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>'"]/g, 
        tag => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[tag] || tag));
}

// ========================================================
// 1. Navigation Controller (Switches between all 11 sections)
// ========================================================
function navigateToSection(sectionId) {
    hideStatus();
    // Hide all section views
    document.querySelectorAll('.section-view').forEach(view => {
        view.classList.remove('active-section');
    });

    // Show target section view
    const target = document.getElementById(sectionId);
    if (target) {
        target.classList.add('active-section');
    }

    // Update active nav button
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.section === sectionId);
    });

    // Lazy load data for specific sections
    if (sectionId === 'secAdmin') {
        loadAdminDashboardData();
    } else if (sectionId === 'secHistory' && currentAccount) {
        loadTransactions(currentAccount.id);
    } else if (sectionId === 'secAnalytics' && currentAccount) {
        loadAnalytics(currentAccount.id);
    } else if (sectionId === 'secGoals' && currentAccount) {
        loadSavingsGoals(currentAccount.id);
    }
}

// ========================================================
// 2. Active Account Management & UI Synchronization
// ========================================================
function updateAllAccountViews(account) {
    currentAccount = account;

    if (!account) {
        // Customer Dashboard Hero
        setText('dashAccountNo', 'ACC-NONE');
        setText('dashHolder', 'No account loaded');
        setText('dashAccountIdBadge', 'Account ID: —');
        setText('dashBalance', '₹0.00');

        // Quick Action targets
        setText('quickDepositTargetAcc', '—');
        setText('quickWithdrawTargetAcc', '—');
        setText('quickTransferSourceAcc', '—');

        // Section 4: Details
        setText('detailAccountId', '—');
        setText('detailName', '—');
        setText('detailAccountNumber', '—');
        setText('detailUserId', '—');
        setText('detailBalance', '—');

        // Section 5 & 6
        setText('depositAccDisplay', '—');
        setText('depositBalDisplay', '—');
        setText('withdrawAccDisplay', '—');
        setText('withdrawBalDisplay', '—');

        // Section 7
        const tf = document.getElementById('transferFrom');
        if (tf) tf.value = '';

        // Reset widgets
        resetDashboardWidgets();
        return;
    }

    const accIdentifier = `${account.accountNumber} (#${account.id})`;
    const balFormatted = formatCurrency(account.balance);

    // 1. Customer Dashboard Hero
    setText('dashAccountNo', account.accountNumber);
    setText('dashHolder', account.name || 'Account Holder');
    setText('dashAccountIdBadge', `Account ID: ${account.id}${account.userId ? ' | User ID: ' + account.userId : ''}`);
    setText('dashBalance', balFormatted);

    // 2. Quick Action target indicators
    setText('quickDepositTargetAcc', `${account.accountNumber} (Bal: ${balFormatted})`);
    setText('quickWithdrawTargetAcc', `${account.accountNumber} (Bal: ${balFormatted})`);
    setText('quickTransferSourceAcc', `${account.accountNumber} (Bal: ${balFormatted})`);

    // 3. Section 4: Dedicated Account Details
    setText('detailAccountId', account.id);
    setText('detailName', account.name);
    setText('detailAccountNumber', account.accountNumber);
    setText('detailUserId', account.userId !== undefined && account.userId !== null ? account.userId : 'None');
    setText('detailBalance', balFormatted);

    // 4. Section 5 & 6: Dedicated Deposit & Withdraw Cards
    setText('depositAccDisplay', accIdentifier);
    setText('depositBalDisplay', balFormatted);
    setText('withdrawAccDisplay', accIdentifier);
    setText('withdrawBalDisplay', balFormatted);

    // 5. Section 7: Fund Transfer From
    const transferFrom = document.getElementById('transferFrom');
    if (transferFrom) transferFrom.value = account.accountNumber || account.id;

    // 6. Section 12: SOAP statement input
    const soapAccountInput = document.getElementById('soapAccountInput');
    if (soapAccountInput) soapAccountInput.value = account.accountNumber || account.id;

    // 7. Load associated live records for active account
    loadTransactions(account.id);
    loadAnalytics(account.id);
    loadSavingsGoals(account.id);
}

function setText(elementId, text) {
    const el = document.getElementById(elementId);
    if (el) el.textContent = text;
}

function resetDashboardWidgets() {
    setText('dashAnalyticsDeposits', '₹0.00');
    setText('dashAnalyticsWithdrawals', '₹0.00');
    setText('dashAnalyticsTransfers', '₹0.00');
    setText('dashAnalyticsNet', '₹0.00');

    setText('analyticsDeposits', '₹0.00');
    setText('analyticsWithdrawals', '₹0.00');
    setText('analyticsTransfers', '₹0.00');
    setText('analyticsCount', '0');
    setText('analyticsReceived', '₹0.00');
    setText('analyticsSpent', '₹0.00');
    setText('analyticsNet', '₹0.00');

    const goalsContainer = document.getElementById('dashGoalsContainer');
    if (goalsContainer) {
        goalsContainer.innerHTML = '<p class="empty-state" style="padding: 16px 0;">No active savings goals found.</p>';
    }

    const txBody = document.getElementById('transactionTableBody');
    if (txBody) {
        txBody.innerHTML = '<tr><td colspan="8" class="empty-state">No account selected. Create or load an account to view transactions.</td></tr>';
    }

    const dashTxBody = document.getElementById('dashRecentTxTableBody');
    if (dashTxBody) {
        dashTxBody.innerHTML = '<tr><td colspan="8" class="empty-state">No transactions recorded.</td></tr>';
    }

    const goalsTableBody = document.getElementById('savingsGoalTableBody');
    if (goalsTableBody) {
        goalsTableBody.innerHTML = '<tr><td colspan="8" class="empty-state">No account selected. Create or load an account to view savings goals.</td></tr>';
    }
}

// Fetch Account by ID or Account Number
async function loadAccountByQuery(query) {
    if (!query) return null;
    const cleanQuery = query.trim();
    if (!cleanQuery) return null;

    try {
        const isNumeric = /^\d+$/.test(cleanQuery);
        const url = isNumeric 
            ? `${API_BASE}/accounts/${cleanQuery}`
            : `${API_BASE}/accounts/number/${encodeURIComponent(cleanQuery)}`;

        const response = await fetch(url);
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || `Account "${cleanQuery}" not found.`);
        }

        updateAllAccountViews(data);
        return data;
    } catch (err) {
        showStatus(err.message, 'error');
        return null;
    }
}

// Dashboard Account Switcher handler
async function handleDashAccountSwitch(e) {
    e.preventDefault();
    hideStatus();
    const input = document.getElementById('dashSwitchInput');
    const btn = document.getElementById('dashSwitchBtn');
    if (!input || !input.value.trim()) return;

    setButtonLoading(btn, true);
    const acc = await loadAccountByQuery(input.value);
    setButtonLoading(btn, false, 'Switch');

    if (acc) {
        showStatus(`Switched active account to ${acc.accountNumber} (#${acc.id})`, 'success');
        input.value = '';
    }
}

// ========================================================
// 3. Customer Dashboard Quick Actions
// ========================================================
let activeQuickAction = null;

function toggleQuickAction(action) {
    const panels = {
        deposit: document.getElementById('quickDepositPanel'),
        withdraw: document.getElementById('quickWithdrawPanel'),
        transfer: document.getElementById('quickTransferPanel')
    };

    const buttons = {
        deposit: document.getElementById('btnQuickDepositToggle'),
        withdraw: document.getElementById('btnQuickWithdrawToggle'),
        transfer: document.getElementById('btnQuickTransferToggle')
    };

    if (activeQuickAction === action || !action) {
        // Close all
        Object.values(panels).forEach(p => p && p.classList.remove('active'));
        Object.values(buttons).forEach(b => b && b.classList.remove('active'));
        activeQuickAction = null;
        return;
    }

    if (!currentAccount) {
        showStatus('Please create or load an account first.', 'error');
        return;
    }

    activeQuickAction = action;
    Object.keys(panels).forEach(key => {
        if (panels[key]) panels[key].classList.toggle('active', key === action);
        if (buttons[key]) buttons[key].classList.toggle('active', key === action);
    });
}

// Quick Deposit
async function handleQuickDeposit(e) {
    e.preventDefault();
    hideStatus();

    if (!currentAccount) {
        showStatus('Please create or load an account first.', 'error');
        return;
    }

    const amountInput = document.getElementById('quickDepositAmount');
    const submitBtn = document.getElementById('quickDepositSubmitBtn');
    const amount = parseFloat(amountInput.value);

    if (isNaN(amount) || amount <= 0) {
        showStatus('Deposit amount must be greater than zero.', 'error');
        return;
    }

    setButtonLoading(submitBtn, true);

    try {
        const res = await fetch(`${API_BASE}/accounts/${currentAccount.id}/deposit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ amount: amount })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Deposit failed.');

        updateAllAccountViews(data);
        showStatus(`Deposited ${formatCurrency(amount)} successfully! New Balance: ${formatCurrency(data.balance)}`, 'success');
        amountInput.value = '';
        toggleQuickAction(null);
    } catch (err) {
        showStatus(err.message, 'error');
    } finally {
        setButtonLoading(submitBtn, false, 'Deposit');
    }
}

// Quick Withdraw
async function handleQuickWithdraw(e) {
    e.preventDefault();
    hideStatus();

    if (!currentAccount) {
        showStatus('Please create or load an account first.', 'error');
        return;
    }

    const amountInput = document.getElementById('quickWithdrawAmount');
    const submitBtn = document.getElementById('quickWithdrawSubmitBtn');
    const amount = parseFloat(amountInput.value);

    if (isNaN(amount) || amount <= 0) {
        showStatus('Withdraw amount must be greater than zero.', 'error');
        return;
    }

    setButtonLoading(submitBtn, true);

    try {
        const res = await fetch(`${API_BASE}/accounts/${currentAccount.id}/withdraw`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ amount: amount })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Withdrawal failed.');

        updateAllAccountViews(data);
        showStatus(`Withdrew ${formatCurrency(amount)} successfully! New Balance: ${formatCurrency(data.balance)}`, 'success');
        amountInput.value = '';
        toggleQuickAction(null);
    } catch (err) {
        showStatus(err.message, 'error');
    } finally {
        setButtonLoading(submitBtn, false, 'Withdraw');
    }
}

// Quick Transfer
async function handleQuickTransfer(e) {
    e.preventDefault();
    hideStatus();

    if (!currentAccount) {
        showStatus('Please create or load an account first.', 'error');
        return;
    }

    const toInput = document.getElementById('quickTransferTo');
    const amountInput = document.getElementById('quickTransferAmount');
    const descInput = document.getElementById('quickTransferDesc');
    const submitBtn = document.getElementById('quickTransferSubmitBtn');

    const toVal = toInput.value.trim();
    const amount = parseFloat(amountInput.value);
    const desc = descInput.value.trim();

    if (!toVal) {
        showStatus('Receiver account (ID or Number) is required.', 'error');
        return;
    }
    if (isNaN(amount) || amount <= 0) {
        showStatus('Transfer amount must be greater than zero.', 'error');
        return;
    }

    const payload = {
        fromAccountId: currentAccount.id,
        fromAccountNumber: currentAccount.accountNumber,
        amount: amount,
        description: desc || null
    };

    if (/^\d+$/.test(toVal)) {
        payload.toAccountId = parseInt(toVal, 10);
        payload.toAccountNumber = toVal;
    } else {
        payload.toAccountNumber = toVal;
    }

    setButtonLoading(submitBtn, true);

    try {
        const res = await fetch(`${API_BASE}/transactions/transfer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Transfer failed.');

        showStatus(`Transfer of ${formatCurrency(amount)} successful! Ref ID: ${data.transactionId}. Sender Balance: ${formatCurrency(data.senderBalance)}`, 'success');
        toInput.value = '';
        amountInput.value = '';
        descInput.value = '';
        toggleQuickAction(null);

        // Reload account details to reflect new balance
        await loadAccountByQuery(String(currentAccount.id));
    } catch (err) {
        showStatus(err.message, 'error');
    } finally {
        setButtonLoading(submitBtn, false, 'Send Money');
    }
}

// ========================================================
// 4. Sections 1 & 2: User Authentication (Login & Register)
// ========================================================
function switchAuthTab(tab) {
    const loginTabBtn = document.getElementById('loginTabBtn');
    const registerTabBtn = document.getElementById('registerTabBtn');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    if (tab === 'login') {
        if (loginTabBtn) loginTabBtn.classList.add('active');
        if (registerTabBtn) registerTabBtn.classList.remove('active');
        if (loginForm) loginForm.style.display = 'block';
        if (registerForm) registerForm.style.display = 'none';
    } else {
        if (registerTabBtn) registerTabBtn.classList.add('active');
        if (loginTabBtn) loginTabBtn.classList.remove('active');
        if (registerForm) registerForm.style.display = 'block';
        if (loginForm) loginForm.style.display = 'none';
    }
}

// User Registration
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideStatus();

        const nameInput = document.getElementById('regName');
        const emailInput = document.getElementById('regEmail');
        const passInput = document.getElementById('regPassword');
        const phoneInput = document.getElementById('regPhone');
        const submitBtn = document.getElementById('registerSubmitBtn');

        const payload = {
            name: nameInput.value.trim(),
            email: emailInput.value.trim(),
            password: passInput.value,
            phone: phoneInput.value.trim() || null
        };

        setButtonLoading(submitBtn, true);

        try {
            const res = await fetch(`${API_BASE}/users/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Registration failed.');

            showStatus(`Registration successful for ${data.name}! You can now login.`, 'success');
            registerForm.reset();
            const loginEmail = document.getElementById('loginEmail');
            if (loginEmail) loginEmail.value = payload.email;
            switchAuthTab('login');
        } catch (err) {
            showStatus(err.message, 'error');
        } finally {
            setButtonLoading(submitBtn, false, 'Create User Account');
        }
    });
}

// User Login
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideStatus();

        const emailInput = document.getElementById('loginEmail');
        const passInput = document.getElementById('loginPassword');
        const submitBtn = document.getElementById('loginSubmitBtn');

        const payload = {
            email: emailInput.value.trim(),
            password: passInput.value
        };

        setButtonLoading(submitBtn, true);

        try {
            const res = await fetch(`${API_BASE}/users/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Login failed.');

            loggedInUser = data.user;
            updateLoggedInUserUI();
            showStatus(`Welcome, ${loggedInUser.name}! (${loggedInUser.role})`, 'success');
            loginForm.reset();

            // Auto-load customer account if exists
            await loadUserAccounts(loggedInUser.id);

            // Navigate to Dashboard
            if (loggedInUser.role === 'ADMIN') {
                navigateToSection('secAdmin');
            } else {
                navigateToSection('secDashboard');
            }
        } catch (err) {
            showStatus(err.message, 'error');
        } finally {
            setButtonLoading(submitBtn, false, 'Login');
        }
    });
}

function updateLoggedInUserUI() {
    const loggedInUserBar = document.getElementById('loggedInUserBar');
    const loggedInUserInfo = document.getElementById('loggedInUserInfo');
    const loggedOutBar = document.getElementById('loggedOutBar');
    const navAdminBtn = document.getElementById('navAdminBtn');

    if (loggedInUser) {
        if (loggedInUserBar) loggedInUserBar.style.display = 'flex';
        if (loggedOutBar) loggedOutBar.style.display = 'none';
        const roleIcon = loggedInUser.role === 'ADMIN' ? '🛡️ [ADMIN] ' : '👤 ';
        if (loggedInUserInfo) loggedInUserInfo.textContent = `${roleIcon}${loggedInUser.name}`;
        
        // Auto-fill user ID in account creation form
        const createUserId = document.getElementById('createUserId');
        const createName = document.getElementById('createName');
        const createEmail = document.getElementById('createEmail');
        if (createUserId) createUserId.value = loggedInUser.id;
        if (createName && !createName.value) createName.value = loggedInUser.name;
        if (createEmail && !createEmail.value) createEmail.value = loggedInUser.email;

        if (navAdminBtn && loggedInUser.role === 'ADMIN') {
            navAdminBtn.style.display = 'inline-block';
        }
    } else {
        if (loggedInUserBar) loggedInUserBar.style.display = 'none';
        if (loggedOutBar) loggedOutBar.style.display = 'block';
        const createUserId = document.getElementById('createUserId');
        if (createUserId) createUserId.value = '';
    }
}

function logoutUser() {
    loggedInUser = null;
    updateLoggedInUserUI();
    showStatus('Logged out successfully.', 'success');
    navigateToSection('secDashboard');
}

async function loadUserAccounts(userId) {
    try {
        const res = await fetch(`${API_BASE}/users/${userId}/accounts`);
        const accounts = await res.json();
        if (res.ok && Array.isArray(accounts) && accounts.length > 0) {
            updateAllAccountViews(accounts[0]);
        }
    } catch (e) {
        console.error('Error fetching user accounts', e);
    }
}

// ========================================================
// 5. Section 4: Create Account & Lookup
// ========================================================
const createAccountForm = document.getElementById('createAccountForm');
if (createAccountForm) {
    createAccountForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideStatus();

        const nameInput = document.getElementById('createName');
        const emailInput = document.getElementById('createEmail');
        const accNoInput = document.getElementById('createAccountNumber');
        const initBalInput = document.getElementById('createInitialBalance');
        const userIdInput = document.getElementById('createUserId');
        const createBtn = document.getElementById('createBtn');

        const initialBalance = parseFloat(initBalInput.value);
        if (isNaN(initialBalance) || initialBalance < 0) {
            showStatus('Initial balance cannot be negative.', 'error');
            return;
        }

        const payload = {
            name: nameInput.value.trim(),
            email: emailInput.value.trim(),
            initialBalance: initialBalance
        };

        const accNo = accNoInput.value.trim();
        if (accNo) payload.accountNumber = accNo;

        const uId = userIdInput.value ? parseInt(userIdInput.value, 10) : null;
        if (uId && !isNaN(uId)) payload.userId = uId;

        setButtonLoading(createBtn, true);

        try {
            const res = await fetch(`${API_BASE}/accounts`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Failed to create account.');

            updateAllAccountViews(data);
            showStatus(`Account created! Account ID: ${data.id}, No: ${data.accountNumber}`, 'success');
            createAccountForm.reset();
            if (loggedInUser && userIdInput) userIdInput.value = loggedInUser.id;
        } catch (err) {
            showStatus(err.message, 'error');
        } finally {
            setButtonLoading(createBtn, false, 'Open Account');
        }
    });
}

const fetchAccountForm = document.getElementById('fetchAccountForm');
if (fetchAccountForm) {
    fetchAccountForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideStatus();

        const input = document.getElementById('lookupAccountId');
        const btn = document.getElementById('lookupAccountBtn');
        if (!input || !input.value.trim()) return;

        setButtonLoading(btn, true);
        const acc = await loadAccountByQuery(input.value);
        setButtonLoading(btn, false, 'Load');

        if (acc) {
            showStatus(`Account #${acc.id} (${acc.accountNumber}) loaded successfully.`, 'success');
        }
    });
}

// ========================================================
// 6. Section 5: Dedicated Deposit Form
// ========================================================
const depositForm = document.getElementById('depositForm');
if (depositForm) {
    depositForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideStatus();

        if (!currentAccount) {
            showStatus('Please create or load an account first before depositing.', 'error');
            return;
        }

        const amountInput = document.getElementById('depositAmount');
        const depositBtn = document.getElementById('depositBtn');
        const amount = parseFloat(amountInput.value);

        if (isNaN(amount) || amount <= 0) {
            showStatus('Deposit amount must be greater than zero.', 'error');
            return;
        }

        setButtonLoading(depositBtn, true);

        try {
            const res = await fetch(`${API_BASE}/accounts/${currentAccount.id}/deposit`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ amount: amount })
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Deposit failed.');

            updateAllAccountViews(data);
            showStatus(`Deposited ${formatCurrency(amount)} successfully! New Balance: ${formatCurrency(data.balance)}`, 'success');
            depositForm.reset();
        } catch (err) {
            showStatus(err.message, 'error');
        } finally {
            setButtonLoading(depositBtn, false, 'Deposit Funds');
        }
    });
}

// ========================================================
// 7. Section 6: Dedicated Withdraw Form
// ========================================================
const withdrawForm = document.getElementById('withdrawForm');
if (withdrawForm) {
    withdrawForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideStatus();

        if (!currentAccount) {
            showStatus('Please create or load an account first before withdrawing.', 'error');
            return;
        }

        const amountInput = document.getElementById('withdrawAmount');
        const withdrawBtn = document.getElementById('withdrawBtn');
        const amount = parseFloat(amountInput.value);

        if (isNaN(amount) || amount <= 0) {
            showStatus('Withdraw amount must be greater than zero.', 'error');
            return;
        }

        setButtonLoading(withdrawBtn, true);

        try {
            const res = await fetch(`${API_BASE}/accounts/${currentAccount.id}/withdraw`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ amount: amount })
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Withdrawal failed.');

            updateAllAccountViews(data);
            showStatus(`Withdrew ${formatCurrency(amount)} successfully! New Balance: ${formatCurrency(data.balance)}`, 'success');
            withdrawForm.reset();
        } catch (err) {
            showStatus(err.message, 'error');
        } finally {
            setButtonLoading(withdrawBtn, false, 'Withdraw Funds');
        }
    });
}

// ========================================================
// 8. Section 7: Dedicated Fund Transfer Form
// ========================================================
const transferForm = document.getElementById('transferForm');
if (transferForm) {
    transferForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideStatus();

        const fromInput = document.getElementById('transferFrom');
        const toInput = document.getElementById('transferTo');
        const amountInput = document.getElementById('transferAmount');
        const descInput = document.getElementById('transferDescription');
        const transferBtn = document.getElementById('transferBtn');

        const fromVal = fromInput.value.trim();
        const toVal = toInput.value.trim();
        const amount = parseFloat(amountInput.value);
        const desc = descInput.value.trim();

        if (!fromVal) {
            showStatus('Sender account (ID or Number) is required.', 'error');
            return;
        }
        if (!toVal) {
            showStatus('Recipient account (ID or Number) is required.', 'error');
            return;
        }
        if (isNaN(amount) || amount <= 0) {
            showStatus('Transfer amount must be greater than zero.', 'error');
            return;
        }

        const payload = {
            amount: amount,
            description: desc || null
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

        setButtonLoading(transferBtn, true);

        try {
            const res = await fetch(`${API_BASE}/transactions/transfer`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Transfer failed.');

            showStatus(`Transfer of ${formatCurrency(amount)} successful! Ref ID: ${data.transactionId}. Sender Balance: ${formatCurrency(data.senderBalance)}`, 'success');
            amountInput.value = '';
            descInput.value = '';

            // Reload active account details
            if (currentAccount) {
                await loadAccountByQuery(String(currentAccount.id));
            }
        } catch (err) {
            showStatus(err.message, 'error');
        } finally {
            setButtonLoading(transferBtn, false, 'Send Transfer');
        }
    });
}

// ========================================================
// 9. Section 8: Transaction History & Customer Dashboard Recent Tx
// ========================================================
async function loadTransactions(accountId) {
    if (!accountId) return;

    try {
        const res = await fetch(`${API_BASE}/transactions/account/${accountId}`);
        const transactions = await res.json();

        if (!res.ok) throw new Error('Failed to load transaction history.');

        renderTransactionTables(transactions);
    } catch (err) {
        console.error('Error fetching transactions:', err);
    }
}

function renderTransactionTables(transactions) {
    const fullBody = document.getElementById('transactionTableBody');
    const recentBody = document.getElementById('dashRecentTxTableBody');

    if (!Array.isArray(transactions) || transactions.length === 0) {
        const emptyRow = '<tr><td colspan="8" class="empty-state">No transactions recorded for this account.</td></tr>';
        if (fullBody) fullBody.innerHTML = emptyRow;
        if (recentBody) recentBody.innerHTML = emptyRow;
        return;
    }

    // 1. Render Full Transaction Table (Section 8)
    if (fullBody) {
        fullBody.innerHTML = transactions.map(tx => formatTxRow(tx)).join('');
    }

    // 2. Render Recent Transactions (Customer Dashboard - Top 5)
    if (recentBody) {
        const top5 = transactions.slice(0, 5);
        recentBody.innerHTML = top5.map(tx => formatTxRow(tx)).join('');
    }
}

function formatTxRow(tx) {
    let badgeClass = 'DEPOSIT';
    if (tx.type === 'WITHDRAW') badgeClass = 'WITHDRAW';
    else if (tx.type === 'TRANSFER_OUT') badgeClass = 'TRANSFER_OUT';
    else if (tx.type === 'TRANSFER_IN') badgeClass = 'TRANSFER_IN';

    return `
        <tr>
            <td>${tx.id}</td>
            <td><span class="badge ${badgeClass}">${tx.type}</span></td>
            <td><strong>${formatCurrency(tx.amount)}</strong></td>
            <td>${escapeHtml(tx.senderAccount) || '—'}</td>
            <td>${escapeHtml(tx.receiverAccount) || '—'}</td>
            <td>${escapeHtml(tx.description) || '—'}</td>
            <td><span class="badge SUCCESS">${tx.status}</span></td>
            <td>${formatDateTime(tx.createdAt)}</td>
        </tr>
    `;
}

function reloadCurrentTransactions() {
    if (!currentAccount) {
        showStatus('Please create or load an account first.', 'error');
        return;
    }
    loadTransactions(currentAccount.id);
    showStatus('Transaction history refreshed.', 'success');
}

// ========================================================
// 10. Section 9: Personal Finance Analytics
// ========================================================
async function loadAnalytics(accountId) {
    if (!accountId) return;

    try {
        const res = await fetch(`${API_BASE}/analytics/account/${accountId}`);
        if (!res.ok) return;
        const data = await res.json();

        // 1. Dedicated Analytics Section (Section 9)
        setText('analyticsDeposits', formatCurrency(data.totalDeposits));
        setText('analyticsWithdrawals', formatCurrency(data.totalWithdrawals));
        setText('analyticsTransfers', formatCurrency(data.totalTransfers));
        setText('analyticsCount', data.transactionCount !== undefined ? data.transactionCount : 0);
        setText('analyticsReceived', formatCurrency(data.totalMoneyReceived));
        setText('analyticsSpent', formatCurrency(data.totalMoneySpent));
        setText('analyticsNet', formatCurrency(data.netSavings));

        // 2. Customer Dashboard Analytics Widget (Section 3)
        setText('dashAnalyticsDeposits', formatCurrency(data.totalDeposits));
        setText('dashAnalyticsWithdrawals', formatCurrency(data.totalWithdrawals));
        setText('dashAnalyticsTransfers', formatCurrency(data.totalTransfers));
        setText('dashAnalyticsNet', formatCurrency(data.netSavings));
    } catch (e) {
        console.error('Error fetching analytics:', e);
    }
}

function reloadCurrentAnalytics() {
    if (!currentAccount) {
        showStatus('Please create or load an account first.', 'error');
        return;
    }
    loadAnalytics(currentAccount.id);
    showStatus('Analytics refreshed.', 'success');
}

// ========================================================
// 11. Section 10: Savings Goals
// ========================================================
const createGoalForm = document.getElementById('createGoalForm');
if (createGoalForm) {
    createGoalForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideStatus();

        if (!currentAccount) {
            showStatus('Please create or load an account first before creating a savings goal.', 'error');
            return;
        }

        const nameInput = document.getElementById('goalName');
        const targetInput = document.getElementById('goalTargetAmount');
        const currentInput = document.getElementById('goalCurrentAmount');
        const dateInput = document.getElementById('goalTargetDate');
        const submitBtn = document.getElementById('createGoalBtn');

        const name = nameInput.value.trim();
        const target = parseFloat(targetInput.value);
        const current = currentInput.value ? parseFloat(currentInput.value) : 0;
        const targetDate = dateInput.value || null;

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
            accountId: currentAccount.id,
            goalName: name,
            targetAmount: target,
            currentAmount: current,
            targetDate: targetDate
        };

        setButtonLoading(submitBtn, true);

        try {
            const res = await fetch(`${API_BASE}/savings-goals`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Failed to create savings goal.');

            showStatus(`Savings goal "${data.goalName}" created successfully! Status: ${data.status} (${data.progressPercentage.toFixed(1)}%)`, 'success');
            createGoalForm.reset();
            await loadSavingsGoals(currentAccount.id);
        } catch (err) {
            showStatus(err.message, 'error');
        } finally {
            setButtonLoading(submitBtn, false, 'Create Goal');
        }
    });
}

async function loadSavingsGoals(accountId) {
    if (!accountId) return;

    try {
        const res = await fetch(`${API_BASE}/savings-goals/account/${accountId}`);
        if (!res.ok) return;
        const goals = await res.json();

        renderSavingsGoals(goals);
    } catch (err) {
        console.error('Error fetching savings goals:', err);
    }
}

function renderSavingsGoals(goals) {
    const tableBody = document.getElementById('savingsGoalTableBody');
    const dashContainer = document.getElementById('dashGoalsContainer');

    if (!Array.isArray(goals) || goals.length === 0) {
        const emptyMsg = '<tr><td colspan="8" class="empty-state">No savings goals created for this account yet.</td></tr>';
        if (tableBody) tableBody.innerHTML = emptyMsg;
        if (dashContainer) dashContainer.innerHTML = '<p class="empty-state" style="padding: 16px 0;">No active savings goals found.</p>';
        return;
    }

    // 1. Render Dedicated Savings Goals Table (Section 10)
    if (tableBody) {
        tableBody.innerHTML = goals.map(goal => {
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
    }

    // 2. Render Customer Dashboard Savings Goals Progress Widget (Section 3)
    if (dashContainer) {
        dashContainer.innerHTML = goals.map(goal => {
            const pct = typeof goal.progressPercentage === 'number' ? goal.progressPercentage : 0;
            const isCompleted = goal.status === 'COMPLETED' || pct >= 100;
            const cappedWidth = Math.min(Math.max(pct, 0), 100);

            return `
                <div style="margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #f3f4f6;">
                    <div style="display: flex; justify-content: space-between; font-size: 0.9rem; margin-bottom: 4px;">
                        <strong>${escapeHtml(goal.goalName)}</strong>
                        <span class="badge ${goal.status}">${pct.toFixed(1)}%</span>
                    </div>
                    <div class="progress-bar-bg" style="height: 8px; margin-bottom: 4px;">
                        <div class="progress-bar-fill ${isCompleted ? 'completed' : ''}" style="width: ${cappedWidth}%;"></div>
                    </div>
                    <div style="display: flex; justify-content: space-between; font-size: 0.8rem; color: #6b7280;">
                        <span>Saved: ${formatCurrency(goal.currentAmount)}</span>
                        <span>Target: ${formatCurrency(goal.targetAmount)}</span>
                    </div>
                </div>
            `;
        }).join('');
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
        const res = await fetch(`${API_BASE}/savings-goals/${goalId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ currentAmount: newAmount })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Failed to update savings goal.');

        showStatus(`Added ${formatCurrency(additional)} to goal "${data.goalName}". Current: ${formatCurrency(data.currentAmount)} (${data.status})`, 'success');
        if (currentAccount) {
            await loadSavingsGoals(currentAccount.id);
        }
    } catch (err) {
        showStatus(err.message, 'error');
    }
}

async function deleteSavingsGoal(goalId) {
    if (!confirm(`Are you sure you want to delete Savings Goal #${goalId}?`)) return;

    try {
        const res = await fetch(`${API_BASE}/savings-goals/${goalId}`, {
            method: 'DELETE'
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Failed to delete savings goal.');

        showStatus('Savings goal deleted successfully.', 'success');
        if (currentAccount) {
            await loadSavingsGoals(currentAccount.id);
        }
    } catch (err) {
        showStatus(err.message, 'error');
    }
}

function reloadCurrentSavingsGoals() {
    if (!currentAccount) {
        showStatus('Please create or load an account first.', 'error');
        return;
    }
    loadSavingsGoals(currentAccount.id);
    showStatus('Savings goals refreshed.', 'success');
}

// ========================================================
// 12. Section 11: Admin Dashboard
// ========================================================
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

        setText('adminTotalUsers', stats.totalUsers);
        setText('adminTotalAccounts', stats.totalAccounts);
        setText('adminTotalTransactions', stats.totalTransactions);
        setText('adminTotalBalance', formatCurrency(stats.totalSystemBalance));
        setText('adminTotalDeposits', formatCurrency(stats.totalDeposits));
        setText('adminTotalWithdrawals', formatCurrency(stats.totalWithdrawals));
        setText('adminTotalTransfers', formatCurrency(stats.totalTransfers));
        setText('adminTotalGoals', stats.totalSavingsGoals);

        // Load active subtab table
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
            tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No users registered in system.</td></tr>';
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
            tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No accounts found in system.</td></tr>';
            return;
        }

        tbody.innerHTML = accounts.map(a => `
            <tr>
                <td>${a.id}</td>
                <td><strong>${escapeHtml(a.accountNumber)}</strong></td>
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
            tbody.innerHTML = '<tr><td colspan="9" class="empty-state">No transactions recorded in system.</td></tr>';
            return;
        }

        tbody.innerHTML = txs.map(tx => `
            <tr>
                <td>${tx.id}</td>
                <td>${tx.accountId || '—'}</td>
                <td><span class="badge ${tx.type}">${tx.type}</span></td>
                <td><strong>${formatCurrency(tx.amount)}</strong></td>
                <td>${escapeHtml(tx.senderAccount) || '—'}</td>
                <td>${escapeHtml(tx.receiverAccount) || '—'}</td>
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
            tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No activity logs recorded.</td></tr>';
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

// ========================================================
// 13. Section 12: SOAP Web Service Statement Logic
// ========================================================
const soapStatementForm = document.getElementById('soapStatementForm');
if (soapStatementForm) {
    soapStatementForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideStatus();

        const input = document.getElementById('soapAccountInput');
        const submitBtn = document.getElementById('soapSubmitBtn');
        const inputVal = input.value.trim();

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

        const rawReqPre = document.getElementById('soapRawRequest');
        const rawResPre = document.getElementById('soapRawResponse');
        const resultContainer = document.getElementById('soapStatementResult');

        if (rawReqPre) rawReqPre.textContent = soapRequestXml;
        setButtonLoading(submitBtn, true);

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
            if (rawResPre) rawResPre.textContent = responseText;

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

            setText('soapResAccountNo', accNoEl.textContent);
            setText('soapResHolder', holderEl.textContent);
            setText('soapResBalance', formatCurrency(parseFloat(balEl.textContent)));

            const txEls = xmlDoc.getElementsByTagName('transactionDetails').length > 0
                ? xmlDoc.getElementsByTagName('transactionDetails')
                : xmlDoc.getElementsByTagNameNS('http://com.bank.mvp/soap/statement', 'transactionDetails');

            const txTableBody = document.getElementById('soapTransactionsTableBody');
            if (txTableBody) {
                if (txEls.length === 0) {
                    txTableBody.innerHTML = '<tr><td colspan="8" class="empty-state">No transactions in statement.</td></tr>';
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
                                <td><strong>${formatCurrency(amount)}</strong></td>
                                <td><span class="badge ${status}">${status}</span></td>
                                <td>${formatDateTime(date)}</td>
                                <td>${sender || '—'}</td>
                                <td>${receiver || '—'}</td>
                                <td>${escapeHtml(desc) || '—'}</td>
                            </tr>
                        `;
                    }
                    txTableBody.innerHTML = rowsHtml;
                }
            }

            if (resultContainer) resultContainer.style.display = 'block';
            showStatus(`SOAP statement generated successfully for Account ${accNoEl.textContent}!`, 'success');
        } catch (err) {
            if (resultContainer) resultContainer.style.display = 'none';
            showStatus('SOAP Error: ' + err.message, 'error');
        } finally {
            setButtonLoading(submitBtn, false, 'Generate SOAP Statement');
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
    return String(unsafe).replace(/[<>&'"]/g, c => {
        switch (c) {
            case '<': return '&lt;';
            case '>': return '&gt;';
            case '&': return '&amp;';
            case '\'': return '&apos;';
            case '"': return '&quot;';
            default: return c;
        }
    });
}

// ========================================================
// 14. Initialization on Page Load
// ========================================================
document.addEventListener('DOMContentLoaded', async () => {
    // 1. Initial view is Customer Dashboard
    navigateToSection('secDashboard');

    // 2. Try loading default demo account (ACC-SOAP-777 or Account ID 1)
    const initialAcc = await loadAccountByQuery('ACC-SOAP-777');
    if (!initialAcc) {
        await loadAccountByQuery('1');
    }
});
