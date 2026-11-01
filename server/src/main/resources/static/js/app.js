/**
 * EasyNutri — Main Web Client Application Script
 */

(function () {
  'use strict';

  // 1. Theme Management (Light / Dark)
  const currentTheme = localStorage.getItem('easynutri_theme') || 'light';
  document.documentElement.setAttribute('data-theme', currentTheme);

  window.toggleTheme = function () {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    const nextTheme = isDark ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', nextTheme);
    localStorage.setItem('easynutri_theme', nextTheme);
  };

  // 2. Sensitive-User Mode (Feature 33)
  const sensitiveModeActive = localStorage.getItem('easynutri_sensitive_mode') === 'true';
  if (sensitiveModeActive) {
    document.body.classList.add('sensitive-mode');
  }

  window.toggleSensitiveMode = function () {
    const active = document.body.classList.toggle('sensitive-mode');
    localStorage.setItem('easynutri_sensitive_mode', active);
    const btn = document.getElementById('sensitiveToggleBtn');
    if (btn) {
      btn.classList.toggle('active', active);
    }
    showToast(
      active ? 'Sensitive-user mode enabled: calorie metrics masked.' : 'Sensitive-user mode disabled.',
      'info'
    );
  };

  // 3. Mobile Sidebar Toggle
  window.toggleSidebar = function () {
    const sidebar = document.querySelector('.app-sidebar');
    if (sidebar) {
      sidebar.classList.toggle('open');
    }
  };

  // 4. Toast Notification Engine
  window.showToast = function (message, type = 'info') {
    let container = document.getElementById('toastContainer');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toastContainer';
      container.style.cssText =
        'position: fixed; top: 20px; right: 20px; z-index: 9999; display: flex; flex-direction: column; gap: 10px; max-width: 360px; width: 100%;';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast-item toast-${type}`;
    toast.style.cssText =
      'background: var(--color-surface, #fff); color: var(--color-text-main, #1c2922); border-left: 4px solid var(--color-primary, #237a57); box-shadow: 0 4px 12px rgba(0,0,0,0.12); padding: 12px 16px; border-radius: 8px; font-size: 0.875rem; font-weight: 500; display: flex; align-items: center; justify-content: space-between; transition: all 250ms ease;';

    if (type === 'danger') {
      toast.style.borderLeftColor = 'var(--color-danger, #d9483b)';
    } else if (type === 'warning') {
      toast.style.borderLeftColor = 'var(--color-accent, #ffad73)';
    }

    toast.innerHTML = `<span>${message}</span><button style="background:none;border:none;cursor:pointer;color:var(--color-text-muted);font-size:16px;margin-left:12px;" onclick="this.parentElement.remove()">&times;</button>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(-10px)';
      setTimeout(() => toast.remove(), 250);
    }, 4000);
  };

  // Initialize UI on DOM load
  document.addEventListener('DOMContentLoaded', () => {
    const sensitiveBtn = document.getElementById('sensitiveToggleBtn');
    if (sensitiveBtn && sensitiveModeActive) {
      sensitiveBtn.classList.add('active');
    }
  });
})();

