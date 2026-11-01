# EasyNutri — Design System Specification

## 1. Color Palette

EasyNutri uses a calming, wellness-focused natural palette with high-contrast text and accessible accents.

### Light Theme (Default)
| Token Name | Color Hex | Resource Name | Usage |
|---|---|---|---|
| Primary | `#237A57` | `@color/easynutri_primary` | Action buttons, top app bar, selected icons, primary brand elements |
| Primary Dark | `#1A5C41` | `@color/easynutri_primary_dark` | Status bar, pressed states |
| Secondary | `#E8F5EC` | `@color/easynutri_secondary` | Card accents, chips, pill highlights, badge backgrounds |
| Secondary Variant | `#D0EBD8` | `@color/easynutri_secondary_variant` | Progress track backgrounds, subtle borders |
| Background | `#F8FAF7` | `@color/easynutri_background` | Window and screen canvas |
| Surface | `#FFFFFF` | `@color/easynutri_surface` | Card surfaces, bottom sheets, navigation bar |
| Text Primary | `#1C2922` | `@color/easynutri_text_primary` | Headings, titles, high-emphasis body text |
| Text Secondary | `#52665B` | `@color/easynutri_text_secondary` | Subtitles, helper text, timestamps, captions |
| Accent | `#FFAD73` | `@color/easynutri_accent` | Highlights, warnings, callouts, energy badges |
| Banner Sample | `#FFF3E0` | `@color/easynutri_sample_banner_bg` | Demonstration data banner background |

### Dark Theme
| Token Name | Color Hex | Resource Name | Usage |
|---|---|---|---|
| Primary | `#4CAF82` | `@color/easynutri_primary` | Adjusted for dark mode contrast (WCAG AAA) |
| Secondary | `#2D4537` | `@color/easynutri_secondary` | Dark surface accent |
| Background | `#121815` | `@color/easynutri_background` | Dark screen canvas |
| Surface | `#1C2420` | `@color/easynutri_surface` | Dark card surface |
| Text Primary | `#E8F0EB` | `@color/easynutri_text_primary` | Light high-emphasis text |
| Text Secondary | `#A0B0A7` | `@color/easynutri_text_secondary` | Muted subtitle text |
| Accent | `#FFB987` | `@color/easynutri_accent` | High-visibility dark mode accent |

---

## 2. Spacing and Metrics

All spacing adheres to an 8dp grid (with 4dp sub-grid units).

| Token | Dimension | Resource Name | Usage |
|---|---|---|---|
| Touch Target | 48dp | `@dimen/min_touch_target` | Minimum interactive target for buttons and icons |
| Spacing XS | 4dp | `@dimen/spacing_xs` | Micro spacing between text and icons |
| Spacing S | 8dp | `@dimen/spacing_s` | Spacing inside compact lists, chips |
| Spacing M | 16dp | `@dimen/spacing_m` | Default container and screen edge margin |
| Spacing L | 24dp | `@dimen/spacing_l` | Section separation |
| Spacing XL | 32dp | `@dimen/spacing_xl` | Hero element spacing |
| Card Radius | 16dp | `@dimen/card_corner_radius` | Rounded corner radius for content cards |
| Button Radius | 12dp | `@dimen/button_corner_radius` | Rounded corner radius for Material buttons |
| Card Elevation | 2dp | `@dimen/card_elevation` | Subtle resting elevation |

---

## 3. Typography

EasyNutri leverages standard Material 3 typography with high-legibility weights:
- **Headline Medium:** Bold 24sp for section headers.
- **Title Medium:** Semi-bold 16sp for card headers and food item names.
- **Body Large / Medium:** 16sp / 14sp for descriptive text and instructions.
- **Label Medium:** 12sp for badges, calories, macros, and nutrient values.

---

## 4. Reusable Styles & Components

- `Widget.EasyNutri.Button.Primary`: Filled Material button with `#237A57` background and 12dp rounded corners.
- `Widget.EasyNutri.Card`: Surface card with 16dp corner radius, 2dp elevation, and subtle border.
- `Widget.EasyNutri.Banner.SampleData`: Warm amber background pill clearly labelling demonstrative mock data.

---

## 5. Accessibility & Multilingual Support

- **Touch Targets:** All clickable navigation items, buttons, and list items enforce `>= 48dp` touch targets.
- **Color Contrast:** All text tokens exceed WCAG 2.1 AA minimum contrast (4.5:1 for body text, 3:1 for large text).
- **Localization:** All UI strings are extracted to `strings.xml`. Full Hindi translations provided in `values-hi/strings.xml`.
- **Sensitive-User Policy:** Pluggable `DisplayPolicy` architecture enables hiding or disguising calorie numbers, weight metrics, and triggering language for eating disorder sensitivity.

