/* =====================================================
   Finance Pro — Main JavaScript
   Clock, greeting, sidebar, counter animations, toasts
   ===================================================== */

// ── Real-time clock ───────────────────────────────────────────────────────────
function updateClock() {
    const now = new Date();
    const h = String(now.getHours()).padStart(2, '0');
    const m = String(now.getMinutes()).padStart(2, '0');
    const s = String(now.getSeconds()).padStart(2, '0');
    const el = document.getElementById('liveClock');
    if (el) el.textContent = `${h}:${m}:${s}`;
}

setInterval(updateClock, 1000);
updateClock();

// ── Greeting based on time of day ─────────────────────────────────────────────
function updateGreeting() {
    const el = document.getElementById('greetingText');
    if (!el) return;
    const h = new Date().getHours();
    let greeting;
    if (h < 12)      greeting = 'Good morning!';
    else if (h < 17) greeting = 'Good afternoon!';
    else if (h < 21) greeting = 'Good evening!';
    else             greeting = 'Good night!';
    el.textContent = greeting;
}

updateGreeting();

// ── Sidebar toggle (mobile) ───────────────────────────────────────────────────
function openSidebar() {
    document.getElementById('sidebar')?.classList.add('open');
    document.getElementById('overlay')?.classList.add('open');
    document.body.style.overflow = 'hidden';
}

function closeSidebar() {
    document.getElementById('sidebar')?.classList.remove('open');
    document.getElementById('overlay')?.classList.remove('open');
    document.body.style.overflow = '';
}

// Close sidebar on escape key
document.addEventListener('keydown', e => {
    if (e.key === 'Escape') closeSidebar();
});

// ── Animated number counter ───────────────────────────────────────────────────
/**
 * Animates a number from 0 to target over ~1.2s using easeOutExpo.
 * @param {HTMLElement} el - element to update
 * @param {number} target  - final numeric value
 */
function animateCounter(el, target) {
    if (!el || isNaN(target)) return;
    const duration = 1200;
    const start    = performance.now();
    const isNeg    = target < 0;
    const abs      = Math.abs(target);

    function easeOutExpo(t) {
        return t === 1 ? 1 : 1 - Math.pow(2, -10 * t);
    }

    function step(now) {
        const elapsed  = now - start;
        const progress = Math.min(elapsed / duration, 1);
        const eased    = easeOutExpo(progress);
        const current  = abs * eased;

        el.textContent = (isNeg ? '-' : '') +
            current.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

        if (progress < 1) requestAnimationFrame(step);
    }

    requestAnimationFrame(step);
}

// Run counters on page load
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.counter').forEach(el => {
        const raw = parseFloat(el.dataset.target ?? '0');
        animateCounter(el, raw);
    });
});

// ── Toast notifications ───────────────────────────────────────────────────────
/**
 * Show a lightweight toast.
 * @param {string} message
 * @param {'success'|'error'|'info'} type
 */
function showToast(message, type = 'success') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const iconMap = {
        success: 'bi-check-circle-fill',
        error:   'bi-exclamation-circle-fill',
        info:    'bi-info-circle-fill',
    };

    const colorMap = {
        success: '#4ade80',
        error:   '#f87171',
        info:    '#60a5fa',
    };

    const toast = document.createElement('div');
    toast.className = 'fp-toast';
    toast.innerHTML = `
        <i class="bi ${iconMap[type] ?? iconMap.info}" style="color:${colorMap[type] ?? colorMap.info};font-size:1rem;"></i>
        <span>${message}</span>
    `;

    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// ── Auto-dismiss Bootstrap alerts ────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.alert').forEach(alert => {
        setTimeout(() => {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            bsAlert?.close();
        }, 4000);
    });
});

// ── Dark mode (basic CSS class toggle, persisted in localStorage) ────────────
function applyDarkMode(enabled) {
    document.documentElement.classList.toggle('dark-mode', enabled);
}

const darkPref = localStorage.getItem('fp_dark_mode');
if (darkPref === 'true') applyDarkMode(true);

const darkToggle = document.getElementById('darkModeSwitch');
if (darkToggle) {
    darkToggle.addEventListener('change', () => {
        const enabled = darkToggle.checked;
        applyDarkMode(enabled);
        localStorage.setItem('fp_dark_mode', enabled);
    });
}

// ── Loading state on form submit ──────────────────────────────────────────────
document.querySelectorAll('form').forEach(form => {
    form.addEventListener('submit', () => {
        const btn = form.querySelector('button[type="submit"]');
        if (btn) {
            btn.disabled = true;
            const original = btn.innerHTML;
            btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status"></span>Saving...`;
            // Re-enable after 5s as safety fallback
            setTimeout(() => {
                btn.disabled = false;
                btn.innerHTML = original;
            }, 5000);
        }
    });
});

// ── Table search (client-side, for static pages) ─────────────────────────────
const tableSearch = document.getElementById('tableSearch');
if (tableSearch) {
    tableSearch.addEventListener('input', function () {
        const q = this.value.toLowerCase();
        document.querySelectorAll('#expenseTable tbody tr').forEach(row => {
            row.style.display = row.textContent.toLowerCase().includes(q) ? '' : 'none';
        });
    });
}

// ── Budget health bar animation (fired after short delay for visual effect) ───
window.addEventListener('load', () => {
    const bar = document.getElementById('budgetBar');
    if (bar) {
        const target = parseFloat(bar.dataset.target ?? '0');
        setTimeout(() => {
            bar.style.width = Math.min(target, 100) + '%';
        }, 400);
    }
});

// ── Chart defaults (global) ───────────────────────────────────────────────────
if (typeof Chart !== 'undefined') {
    Chart.defaults.font.family = "'Inter', sans-serif";
    Chart.defaults.font.size   = 12;
    Chart.defaults.color       = '#64748b';
    Chart.defaults.plugins.tooltip.backgroundColor = '#1e293b';
    Chart.defaults.plugins.tooltip.titleFont       = { weight: '700' };
    Chart.defaults.plugins.tooltip.bodyFont        = { weight: '500' };
    Chart.defaults.plugins.tooltip.padding         = 10;
    Chart.defaults.plugins.tooltip.cornerRadius    = 8;
}
