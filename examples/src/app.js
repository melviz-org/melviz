// Melviz Examples Gallery Application
let samplesData = null;
let currentDashboard = null;

// DOM Elements
const categoriesNav = document.getElementById('categories-nav');
const searchInput = document.getElementById('search');
const welcomeScreen = document.getElementById('welcome-screen');
const dashboardContainer = document.getElementById('dashboard-container');
const dashboardIframe = document.getElementById('dashboard-iframe');
const currentDashboardName = document.getElementById('current-dashboard-name');
const dashboardCount = document.getElementById('dashboard-count');
const statsContainer = document.getElementById('stats');
const openNewWindowBtn = document.getElementById('open-new-window');
const reloadDashboardBtn = document.getElementById('reload-dashboard');

// Load samples.json
async function loadSamples() {
    try {
        const response = await fetch('samples.json');
        samplesData = await response.json();
        initializeApp();
    } catch (error) {
        console.error('Error loading samples:', error);
        categoriesNav.innerHTML = '<div style="padding: 20px; color: red;">Error loading samples.json</div>';
    }
}

// Initialize the application
function initializeApp() {
    dashboardCount.textContent = `${samplesData.totalDashboards} dashboards`;
    renderCategories();
    renderStats();
    setupEventListeners();

    // Check if there's a dashboard in the URL hash
    const hash = window.location.hash.slice(1);
    if (hash) {
        const [category, dashboardPath] = hash.split('/');
        loadDashboardFromHash(category, dashboardPath);
    }
}

// Render categories and dashboards
function renderCategories() {
    categoriesNav.innerHTML = '';

    samplesData.categories.forEach(category => {
        const categoryDiv = document.createElement('div');
        categoryDiv.className = 'category';

        const categoryHeader = document.createElement('div');
        categoryHeader.className = 'category-header';
        categoryHeader.innerHTML = `
            <span>${category.category}</span>
            <span class="category-toggle">▼</span>
        `;

        const categoryItems = document.createElement('div');
        categoryItems.className = 'category-items';

        category.dashboards.forEach(dashboard => {
            const dashboardItem = document.createElement('div');
            dashboardItem.className = 'dashboard-item';
            dashboardItem.textContent = dashboard.name;
            dashboardItem.dataset.path = dashboard.path;
            dashboardItem.dataset.name = dashboard.name;
            dashboardItem.dataset.category = category.category;

            dashboardItem.addEventListener('click', () => {
                loadDashboard(dashboard);
            });

            categoryItems.appendChild(dashboardItem);
        });

        categoryHeader.addEventListener('click', () => {
            categoryDiv.classList.toggle('collapsed');
        });

        categoryDiv.appendChild(categoryHeader);
        categoryDiv.appendChild(categoryItems);
        categoriesNav.appendChild(categoryDiv);
    });
}

// Render statistics
function renderStats() {
    const categoryCount = samplesData.categories.length;
    const totalDashboards = samplesData.totalDashboards;

    statsContainer.innerHTML = `
        <div class="stat-card">
            <h3>Total Dashboards</h3>
            <div class="value">${totalDashboards}</div>
        </div>
        <div class="stat-card">
            <h3>Categories</h3>
            <div class="value">${categoryCount}</div>
        </div>
    `;
}

// Load a dashboard
function loadDashboard(dashboard) {
    currentDashboard = dashboard;

    // Update active state
    document.querySelectorAll('.dashboard-item').forEach(item => {
        item.classList.remove('active');
    });
    const activeItem = document.querySelector(`[data-path="${dashboard.path}"]`);
    if (activeItem) {
        activeItem.classList.add('active');
    }

    // Update URL hash
    window.location.hash = `${dashboard.category}/${encodeURIComponent(dashboard.path)}`;

    // Show dashboard container
    welcomeScreen.style.display = 'none';
    dashboardContainer.style.display = 'flex';
    currentDashboardName.textContent = dashboard.name;

    // Load dashboard in iframe
    loadDashboardInIframe(dashboard.path);
}

// Load dashboard from URL hash
function loadDashboardFromHash(category, dashboardPath) {
    const decodedPath = decodeURIComponent(dashboardPath);

    for (const cat of samplesData.categories) {
        for (const dashboard of cat.dashboards) {
            if (dashboard.path === decodedPath) {
                loadDashboard(dashboard);
                return;
            }
        }
    }
}

// Load dashboard in iframe using Melviz
function loadDashboardInIframe(dashboardPath) {
    const dashboardUrl = `${window.location.origin}/dashboards/${dashboardPath}`;   
    dashboardIframe.src = "melviz-webapp?import=" + encodeURI(dashboardUrl);
}

// Setup event listeners
function setupEventListeners() {
    // Search functionality
    searchInput.addEventListener('input', (e) => {
        const searchTerm = e.target.value.toLowerCase();

        document.querySelectorAll('.dashboard-item').forEach(item => {
            const name = item.dataset.name.toLowerCase();
            const category = item.dataset.category.toLowerCase();

            if (name.includes(searchTerm) || category.includes(searchTerm)) {
                item.classList.remove('hidden');
            } else {
                item.classList.add('hidden');
            }
        });

        // Hide empty categories
        document.querySelectorAll('.category').forEach(category => {
            const visibleItems = category.querySelectorAll('.dashboard-item:not(.hidden)');
            if (visibleItems.length === 0 && searchTerm !== '') {
                category.style.display = 'none';
            } else {
                category.style.display = 'block';
            }
        });
    });

    // Open in new window
    openNewWindowBtn.addEventListener('click', () => {
        if (currentDashboard) {
            const url = `${window.location.origin}${window.location.pathname}#${currentDashboard.category}/${encodeURIComponent(currentDashboard.path)}`;
            window.open(url, '_blank');
        }
    });

    // Reload dashboard
    reloadDashboardBtn.addEventListener('click', () => {
        if (currentDashboard) {
            loadDashboard(currentDashboard);
        }
    });

    // Handle browser back/forward
    window.addEventListener('hashchange', () => {
        const hash = window.location.hash.slice(1);
        if (hash) {
            const [category, dashboardPath] = hash.split('/');
            loadDashboardFromHash(category, dashboardPath);
        } else {
            welcomeScreen.style.display = 'flex';
            dashboardContainer.style.display = 'none';
            document.querySelectorAll('.dashboard-item').forEach(item => {
                item.classList.remove('active');
            });
        }
    });
}

// Load samples when page loads
loadSamples();
