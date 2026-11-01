# EasyNutri — Web Design System Specification

## 1. Visual Identity & Brand Direction

EasyNutri is styled as a premium commercial health and nutrition SaaS product:
- **Tone:** Premium, Minimal, Modern, Clean, Friendly, Health-Focused, Professional.
- **Form:** Soft rounded cards (16px), subtle ambient elevation shadows, clean typography, accessible color contrast.

---

## 2. Color Palette & CSS Custom Properties

All styling uses standard CSS variables defined in `:root` and overridden in `[data-theme="dark"]`.

| Token | Light Hex | Dark Hex | Usage |
|---|---|---|---|
| `--color-primary` | `#237A57` | `#38A174` | Action buttons, brand icons, active navigation items |
| `--color-primary-dark` | `#19583E` | `#277955` | Button hover states, high-emphasis text accents |
| `--color-mint` | `#E8F5EC` | `#1B3125` | Active nav backgrounds, badges, soft highlights |
| `--color-background` | `#F8FAF7` | `#101613` | Application background canvas |
| `--color-surface` | `#FFFFFF` | `#18221D` | Cards, sidebars, top headers, modal surfaces |
| `--color-text-main` | `#1C2922` | `#EBF3EE` | Primary headers, body text, data values |
| `--color-text-muted` | `#78847D` | `#95A59D` | Subtitles, labels, secondary information |
| `--color-accent` | `#FFAD73` | `#FFA263` | Callout highlights, energy badges, hydration bars |
| `--color-border` | `#E5ECE7` | `#293830` | Card borders, dividers, table lines |

---

## 3. Responsive Layout System

### Desktop (Viewport > 1024px)
- **Fixed Sidebar:** 260px width with 13 distinct navigation items grouped logically.
- **Sticky Header:** 70px height containing search, network sync status, sensitive mode toggle, theme switcher, and user profile.
- **Main Content:** Max-width 1400px centered canvas with responsive multi-column CSS grids (`.grid-cols-4`, `.grid-cols-3`, `.dashboard-split`).

### Tablet (768px - 1024px)
- Grid columns auto-adjust from 4 columns to 2 columns.
- Split dashboards stack gracefully into a single vertical stream.

### Mobile (Viewport < 768px)
- **Collapsible Drawer:** Left sidebar folds off-screen (`transform: translateX(-100%)`) with smooth slide-in transition triggered by header burger button.
- **Bottom Navigation Bar:** Fixed 64px bottom bar providing 5 thumb-friendly touch targets (`Home`, `Recipes`, `Diary`, `Pantry`, `Assistant`).
- **Touch Targets:** Minimum 44x44px for all interactive buttons and inputs.
- **Zero Horizontal Scrolling:** Content adapts to 100% viewport width without overflowing.

---

## 4. UI Components

### Metric Cards (`.metric-card`)
- Clean surface container with uppercase label, large 30px bold metric value, and colored progress bar.

### Sensitive-User Mode Masking (Feature 33)
- When enabled via `#sensitiveToggleBtn`, the body receives `.sensitive-mode`.
- Numerical fields (`.sensitive-metric`) receive `filter: blur(8px)`.
- Qualitative wellness indicators (`.sensitive-fallback`) appear in place ("Optimal Balance", "Nutrient Dense", "High Protein").

### Toast Notification System
- Non-blocking notifications sliding from top-right corner (`window.showToast(message, type)`).
- Supports `info`, `warning`, and `danger` styling with automatic 4-second dismissal.

### Progressive Web App (PWA) Offline Indicator
- Dynamic badge in header (`#networkSyncBadge`):
  - **Online:** Green dot with "Online" text.
  - **Offline:** Amber dot with "Offline Mode" text.
