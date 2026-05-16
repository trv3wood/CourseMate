---
name: CourseMate Academic System
colors:
  surface: '#f7f9fc'
  surface-dim: '#d8dadd'
  surface-bright: '#f7f9fc'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4f7'
  surface-container: '#eceef1'
  surface-container-high: '#e6e8eb'
  surface-container-highest: '#e0e3e6'
  on-surface: '#191c1e'
  on-surface-variant: '#414751'
  inverse-surface: '#2d3133'
  inverse-on-surface: '#eff1f4'
  outline: '#717783'
  outline-variant: '#c1c7d3'
  surface-tint: '#0060ac'
  primary: '#005da7'
  on-primary: '#ffffff'
  primary-container: '#2976c7'
  on-primary-container: '#fdfcff'
  inverse-primary: '#a4c9ff'
  secondary: '#4858ab'
  on-secondary: '#ffffff'
  secondary-container: '#96a5ff'
  on-secondary-container: '#27378a'
  tertiary: '#006860'
  on-tertiary: '#ffffff'
  tertiary-container: '#00837a'
  on-tertiary-container: '#f3fffc'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d4e3ff'
  primary-fixed-dim: '#a4c9ff'
  on-primary-fixed: '#001c39'
  on-primary-fixed-variant: '#004883'
  secondary-fixed: '#dee0ff'
  secondary-fixed-dim: '#bac3ff'
  on-secondary-fixed: '#00105b'
  on-secondary-fixed-variant: '#2f3f92'
  tertiary-fixed: '#8ef4e9'
  tertiary-fixed-dim: '#71d7cd'
  on-tertiary-fixed: '#00201d'
  on-tertiary-fixed-variant: '#00504a'
  background: '#f7f9fc'
  on-background: '#191c1e'
  surface-variant: '#e0e3e6'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '500'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.1px
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  margin-mobile: 16px
  gutter-mobile: 12px
  container-padding: 16px
---

## Brand & Style

The design system is rooted in the principles of **Material Design 3 (MD3)**, tailored specifically for university environments. The brand personality is scholarly, organized, and reliable, yet avoids the coldness of traditional institutional software by incorporating soft, approachable tones and organic motion.

The target audience consists of university students and educators who require a high-cognition environment that minimizes distraction while fostering community. The UI evokes a sense of **calm productivity and academic clarity**. By utilizing a "Modern Corporate" aesthetic with minimalist tendencies, the design system prioritizes content density without sacrificing white space, ensuring that course materials and community discussions remain the focal point.

## Colors

The color strategy employs a **Tonal Palette** approach consistent with MD3. 

- **Primary (Soft Blue):** Used for key action states, active navigation indicators, and brand-critical components.
- **Secondary (Indigo):** Reserved for subtle accents, secondary buttons, and categorization of course types.
- **Tertiary (Teal):** Used for "Success" states and positive reinforcement within the learning flow.
- **Surface/Neutral:** A cool-tinted grey-blue (#F5F7FA) is used for background layers to reduce eye strain during long reading sessions, while pure white (#FFFFFF) is reserved for high-priority card surfaces.
- **Error (Coral):** Specifically designated for homework deadlines, overdue tasks, and urgent system alerts to ensure high visibility without the aggression of a standard crimson red.

## Typography

The typography system utilizes **Inter** for its exceptional legibility in both English and numerical data, providing a clean pairing with Simplified Chinese system fonts. 

For Chinese text, line heights are increased by approximately 10-15% compared to standard Latin settings to accommodate the complexity of the characters. Headlines use a tighter letter-spacing to appear more authoritative, while body text maintains a generous line-height (1.5x) to ensure that academic posts and course descriptions remain readable over long periods.

## Layout & Spacing

This design system adheres to an **8dp Square Grid** for all structural elements and a **4dp baseline grid** for typography alignment. 

- **Mobile Layout:** A 4-column fluid grid with 16dp outer margins and 12dp gutters.
- **Alignment:** Components should be pinned to the grid to maintain a professional, "academic" structure. 
- **Vertical Rhythm:** Spacing between sections (e.g., between a Course Card and a Deadline List) should consistently use 24dp (lg) to provide clear visual separation and breathing room.

## Elevation & Depth

In alignment with Material Design 3, depth is primarily communicated through **Tonal Elevation** rather than heavy drop shadows. 

- **Level 0 (Background):** Pure White or Surface tint.
- **Level 1 (Cards):** Subtle tonal overlay of the primary color (approx 5% opacity) over the surface.
- **Level 2 (Active States/Pop-ups):** A very soft, diffused shadow (8px blur, 4% opacity) to suggest interactivity.
- **Navigation:** The Bottom Navigation and Top App Bar use a consistent "Surface Container" color to feel physically separate from the scrolling content area.

## Shapes

The shape language is characterized by **large, friendly corner radii** that soften the professional layout. 

- **Small Components:** Buttons and Input fields use an 8dp (0.5rem) radius.
- **Medium Components:** Course Cards and Modals use a 16dp (1rem) radius.
- **Large Components:** Bottom Sheets and featured header containers use a 24dp (1.5rem) radius on top corners.
- **FAB:** The Floating Action Button follows the MD3 "squircle" or rounded-square evolution, rather than a perfect circle, to maintain a modern technical feel.

## Components

### Buttons & Interaction
- **Primary FAB:** Located in the bottom right, using the Primary Soft Blue. Used for "New Post" or "Join Course."
- **Buttons:** Filled buttons for primary actions; Outlined buttons for secondary course options. Corner radius: 8dp.

### Navigation
- **Bottom Navigation:** Features 4-5 destinations (Home, Courses, Deadlines, Profile). Icons use the "filled" state when active, accompanied by a subtle tonal pill background.
- **Top App Bar:** Centers the "CourseMate" title or current course name. Uses a transparent background that transitions to a solid Surface color upon scroll.

### Data Display
- **Cards:** Used for course listings. Must include a header area for the Course Code (e.g., CS101) and a content area for the latest announcement. Padding: 16dp.
- **Chips:** Used for course tags (e.g., "Required," "Elective," "Group Project"). High roundedness (pill-shaped).

### Input & Feedback
- **TextFields:** Outlined style with Primary color accents on focus. Labels should always be visible (Floating Label style).
- **Deadlines:** Displayed as a vertical list with a Coral accent bar on the left edge of the card to indicate urgency.