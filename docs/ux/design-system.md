# Genetic Planner — Design System

**Version:** 1.1  
**Status:** Approved design baseline  
**Project:** Genetic Planner (PFG)  
**Scope:** Frontend — React, TypeScript, Material UI

---

## 1. Purpose

This document defines the visual language, UI conventions, accessibility requirements, and responsive design principles of Genetic Planner.

Its purpose is to ensure a consistent, accessible, and maintainable user experience across the application.

The design system applies to:

- Dashboard and planning management.
- Planning creation and configuration wizard.
- Schedule generation and results visualization.
- Shared components, navigation, forms, feedback, and application states.

The design system complements the existing frontend architecture, UX strategy, user flows, and wireframes.

It does not define business rules, genetic algorithm behavior, or backend contracts.

## 2. Design principles

### 2.1. Clarity first

Genetic Planner manages potentially complex scheduling problems. The interface must communicate information clearly, prioritize relevant actions, and avoid unnecessary visual complexity.

### 2.2. Progressive disclosure

Users should encounter configuration options progressively, particularly throughout the planning wizard.

Advanced information should not compete with the primary task.

### 2.3. Consistency

Similar elements must share visual appearance, interaction patterns, spacing, and feedback behavior.

### 2.4. Accessible by default

Accessibility is considered during component design rather than as a final adjustment.

WCAG 2.2 Level AA is the target for the frontend.

### 2.5. Responsive and adaptable

The application follows a desktop-first approach while supporting tablet and mobile layouts.

Information density and navigation adapt to the available screen space.

### 2.6. Functional visual hierarchy

Color, typography, spacing, and elevation must help users understand structure, priorities, and system states.

Decoration must not interfere with usability.

---

## 3. Color system

### 3.1. Brand and interactive color separation

Genetic Planner distinguishes brand colors from interactive colors.

The original brand orange remains the primary visual identity, while a darker orange is used for interactive elements to ensure sufficient contrast on light backgrounds.

| Token | Hex | Purpose |
|---|---|---|
| Brand orange | `#FE5000` | Logo, identity, decorative accents |
| Peach | `#FFB07C` | Supporting accents, gradients, decorative hover effects |
| Interactive primary | `#B83B00` | Primary buttons, links, selected controls |
| Interactive hover | `#A63500` | Hover state for primary buttons |
| Teal | `#147D7E` | Complementary accent, secondary actions, informational states |

**Rules:**

- Brand orange must not automatically be used as an interactive foreground color.
- Primary contained buttons use the darker interactive orange with white text.
- Primary links on light backgrounds use the interactive orange.
- Peach is reserved for decorative effects and supporting accents.
- Peach must not replace a darker interactive hover color when doing so would reduce contrast.
- Teal can represent secondary emphasis and informational states.
- Interactive states must remain distinguishable without relying exclusively on color.

### 3.2. Dashboard hero gradient

The Dashboard hero uses the approved orange-to-peach gradient.

```css
background: linear-gradient(
  110deg,
  #FE5000 0%,
  #FFB07C 100%
);
```

The gradient is primarily decorative.

Text and interactive controls placed over it must maintain sufficient contrast across the entire gradient surface.

Prefer a contrasting solid surface or an independently validated foreground treatment when the gradient does not provide sufficient contrast.

### 3.3. Neutral palette

The interface uses warm neutral backgrounds and white surfaces.

| Token | Hex | Usage |
|---|---|---|
| Background default | `#FAF8F5` | Main application background |
| Background paper | `#FFFFFF` | Cards, dialogs, panels |
| Background subtle | `#F5F1EC` | Secondary sections |
| Text primary | `#292524` | Main text |
| Text secondary | `#57534E` | Descriptions and secondary information |
| Divider | `#E7E1DA` | Borders and separators |

These values are the initial implementation palette. They should be validated in the actual interface, including contrast between adjacent surfaces and non-text controls.

### 3.4. Semantic colors

Semantic colors communicate application states independently of brand identity.

| State | Hex | Purpose |
|---|---|---|
| Success | `#2E7D5B` | Successful operations, valid results |
| Warning | `#875900` | Warnings and situations requiring attention |
| Error | `#D32F2F` | Failed operations and invalid inputs |
| Info | `#147D7E` | Informational messages and neutral guidance |

The warning color is intentionally darker to provide sufficient contrast for interactive uses.

Semantic colors must not be communicated through color alone.

Use appropriate labels, icons, or explanatory text.

### 3.5. Contrast requirements

The design system targets WCAG 2.2 AA.

Minimum contrast requirements include:

- Normal text: 4.5:1.
- Large text: 3:1.
- Relevant non-text UI indicators: 3:1 against adjacent colors, where applicable.

The interactive palette is designed to support white text on solid semantic backgrounds.

Approximate contrast ratios against white:

| Color | Contrast ratio |
|---|---|
| Brand orange `#FE5000` | 3.30:1 |
| Interactive primary `#B83B00` | 5.73:1 |
| Success `#2E7D5B` | 5.00:1 |
| Warning `#875900` | 6.07:1 |
| Error `#D32F2F` | 4.98:1 |
| Info `#147D7E` | 4.92:1 |

Brand orange does not meet the 4.5:1 minimum for normal white text.

Therefore, it should not be used as the background for small white text without an alternative accessible treatment.

Contrast must be checked for all actual foreground/background combinations, including hover, selected, disabled, and focus states where applicable.

---

## 4. Typography

Genetic Planner uses two complementary typefaces.

### 4.1. Space Grotesk

**Purpose:** Brand identity, visual hierarchy, page headings, and highlighted metrics.

| Weight | Usage |
|---|---|
| 700 | Brand, main headings, page titles, key metrics |
| 600 | Section headings, visual subtitles, card headings |

Applied to Material UI typography variants:

- `h1`
- `h2`
- `h3`
- `h4`

Highlighted numerical metrics may also use Space Grotesk.

### 4.2. Inter

**Purpose:** Functional UI, reading, navigation, and data presentation.

| Weight | Usage |
|---|---|
| 400 | Body text, descriptions, table content |
| 500 | Labels, form fields, secondary data |
| 600 | Buttons, tabs, chips, navigation, functional subtitles |
| 700 | Table headers and high-emphasis labels |

Inter is the default application font.

### 4.3. Typographic hierarchy

| Element | Desktop size | Weight | Font |
|---|---|---|---|
| h1 | 40 px | 700 | Space Grotesk |
| h2 | 32 px | 700 | Space Grotesk |
| h3 | 28 px | 700 | Space Grotesk |
| h4 | 24 px | 700 | Space Grotesk |
| h5 | 20 px | 600 | Inter |
| h6 | 18 px | 600 | Inter |
| Body 1 | 16 px | 400 | Inter |
| Body 2 | 14 px | 400 | Inter |
| Button | 14 px | 600 | Inter |
| Caption | 12 px | 400 | Inter |

Heading sizes should adapt on smaller screens.

Body text should use comfortable line heights, typically around 1.5.

### 4.4. Typography rules

- Maintain consistent heading hierarchy.
- Avoid using font size alone to communicate meaning.
- Do not use Space Grotesk for long-form reading content.
- Use tabular numeric alignment when comparing numerical data.
- Avoid unnecessary uppercase text.
- Support browser text resizing and zoom.

---

## 5. Spacing and density

### 5.1. Density

**Approved density: Balanced.**

The interface must provide enough spacing for comfortable interaction without sacrificing information visibility.

### 5.2. Spacing scale

Use the Material UI spacing system based on 8 px.

| Spacing unit | Value |
|---|---|
| 0.5 | 4 px |
| 1 | 8 px |
| 1.5 | 12 px |
| 2 | 16 px |
| 3 | 24 px |
| 4 | 32 px |
| 5 | 40 px |
| 6 | 48 px |

Use spacing tokens consistently instead of introducing arbitrary margins and padding.

### 5.3. Recommended layout spacing

- Main content padding: approximately 24–32 px on desktop.
- Section spacing: approximately 24–32 px.
- Card internal padding: approximately 16–24 px.
- Form field spacing: approximately 16–24 px.
- Mobile content padding: approximately 16 px.

These are starting values and may be adjusted for individual screen requirements.

---

## 6. Shapes, borders, and elevation

### 6.1. Border radius

**Approved style: Soft rounding, approximately 10–12 px.**

| Component | Radius |
|---|---|
| Cards and panels | 12 px |
| Buttons and inputs | 10 px |
| Dialogs | 12 px |
| Chips | Component-specific, potentially pill-shaped |

Exceptions should be intentional and documented.

### 6.2. Cards

**Approved style: Thin border with a very subtle shadow.**

Cards should use:

- White background.
- Subtle warm-neutral border.
- Soft corner radius.
- Minimal shadow.
- Balanced internal spacing.

Card elevation must not create excessive visual separation between sections.

### 6.3. Elevation

Prefer a limited elevation scale:

- Level 0: Flat surfaces.
- Level 1: Cards with subtle elevation.
- Level 2: Menus and floating elements.
- Level 3: Dialogs and overlays.

Elevation values will be centralized in the theme.

---

## 7. Application layout and navigation

### 7.1. Shared application shell

The application uses a shared header and a main content area.

The global layout must not impose a permanent sidebar on every page.

### 7.2. Dashboard

The Dashboard uses:

- Shared application header.
- Primary New Planning action.
- Welcome or introductory hero.
- Planning management content.
- Summary cards and relevant status information.

The hero uses the orange-to-peach gradient.

Statistics must be derived from real application data rather than static placeholders in production.

### 7.3. Planning Wizard

The Wizard uses:

- Contextual header.
- Navigation back to the Dashboard.
- Step-based navigation.
- Main configuration panel.
- Clear progress and validation feedback.

The stepper belongs to the Wizard, not to the global application shell.

### 7.4. Results

The Results screen uses:

- Contextual header.
- Summary of generated scheduling results.
- Quality and feasibility indicators.
- Calendar and alternative result views, where implemented.

Results require a wide content area suitable for schedule visualization.

### 7.5. Unknown routes

Unknown routes display a dedicated Not Found page with a clear action to return to the Dashboard.

### 7.6. Navigation principles

- Use React Router for application navigation.
- Preserve meaningful URLs.
- Highlight the current navigation context where applicable.
- Use links for navigation and buttons for actions.
- Avoid presenting actions that are not implemented as if they were functional.

---

## 8. Responsive design

**Approved strategy: Desktop-first, responsive for tablet and mobile.**

### 8.1. Breakpoints

Use Material UI default breakpoints unless a specific screen requires otherwise.

| Breakpoint | Minimum width |
|---|---|
| xs | 0 px |
| sm | 600 px |
| md | 900 px |
| lg | 1200 px |
| xl | 1536 px |

### 8.2. Desktop

- Comfortable content width.
- Multiple columns when appropriate.
- Full navigation labels.
- Wide calendar and data views.

### 8.3. Tablet

- Reduce columns and horizontal spacing.
- Adapt navigation to available width.
- Preserve readable forms and controls.

### 8.4. Mobile

- Prefer single-column layouts.
- Stack cards and form sections.
- Simplify navigation where necessary.
- Avoid horizontal overflow of the overall page.
- Allow complex tables and calendars to scroll within their own containers when appropriate.

The mobile experience must remain functional even when dense scheduling views require specialized interaction patterns.

---

## 9. Shared component conventions

### 9.1. Buttons

Use Material UI Button as the base component.

- Contained: Primary actions.
- Outlined: Secondary actions.
- Text: Low-emphasis actions.

Primary contained buttons use the interactive primary color `#B83B00` with white text.

Primary button hover uses `#A63500`.

The brand orange and peach colors must not replace interactive colors where contrast would be compromised.

Destructive actions require appropriate warning or confirmation behavior.

### 9.2. Cards

Use Material UI Card or Paper, with shared theme configuration for borders, radius, and elevation.

Avoid repeated component-specific styling.

### 9.3. Forms

Use:

- React Hook Form for form state.
- Zod for schema validation.
- Material UI for controls and feedback.

Requirements:

- Visible labels.
- Clear required-field indicators.
- Accessible error messages.
- Consistent validation behavior.
- Preservation of user input when moving between Wizard steps, where supported.

### 9.4. Chips and status indicators

Chips may represent planning status, constraints, or result quality.

Their meaning must remain understandable without relying only on color.

### 9.5. Tables

Tables should prioritize readability and efficient comparison.

Use:

- Clear column headings.
- Consistent alignment.
- Appropriate numerical formatting.
- Accessible sorting and pagination when implemented.
- Responsive overflow strategies.

### 9.6. Calendars

FullCalendar is the primary calendar visualization library.

Calendar event colors must have consistent meanings.

Where color represents a resource, activity, or status, provide an additional way to identify it.

---

## 10. Interaction and feedback states

All interactive components should define appropriate states:

| State | Expected behavior |
|---|---|
| Default | Normal appearance |
| Hover | Visible interactive feedback |
| Focus | Clear keyboard focus indicator |
| Active | Feedback during interaction |
| Selected | Persistent indication of selection |
| Disabled | Clearly unavailable |
| Loading | Progress feedback |
| Error | Explanation and recovery guidance |
| Success | Confirmation of completed action |

### 10.1. Loading

Use loading indicators or skeletons when waiting for server data.

Avoid replacing existing content unnecessarily during background refreshes.

### 10.2. Empty states

Explain why content is empty and provide a useful next action where possible.

Example: A Dashboard without planning problems should encourage creation of the first planning.

### 10.3. Error states

Error messages should:

- Explain the problem in understandable terms.
- Avoid exposing internal implementation details.
- Offer recovery or retry actions where appropriate.

### 10.4. Generation feedback

Schedule generation must communicate its state without implying completion before the backend confirms it.

Results should distinguish feasibility from optimization quality.

---

## 11. Accessibility

WCAG 2.2 AA is the accessibility target.

### 11.1. Contrast

- Normal text: minimum 4.5:1.
- Large text: minimum 3:1.
- Relevant non-text UI indicators: minimum 3:1 against adjacent colors, where applicable.

Brand colors may require darker foregrounds, alternative shades, or different usage depending on the background.

### 11.2. Keyboard navigation

- All interactive elements must be keyboard accessible.
- Focus order must be logical.
- Focus indicators must remain visible.
- Keyboard users must be able to navigate the Wizard and results interfaces.

### 11.3. Forms

- Associate labels with controls.
- Connect error messages to their fields.
- Do not communicate validation errors only through color.
- Provide clear instructions for complex inputs.

### 11.4. Motion and interaction

- Respect reduced-motion preferences.
- Avoid unnecessary animation.
- Provide sufficiently large interactive targets and appropriate spacing.

### 11.5. Semantic structure

Use semantic headings, navigation landmarks, main content regions, and accessible component labels.

---

## 12. Styling architecture

The styling strategy is:

**Material UI Theme + CSS Modules**

### 12.1. Material UI Theme

The global theme is responsible for:

- Color palette.
- Typography.
- Spacing.
- Border radius.
- Shadows.
- Shared component variants and overrides.
- CSS design tokens.

Main implementation file:

`frontend/src/theme/theme.ts`

### 12.2. CSS Modules

CSS Modules are responsible for:

- Component-specific layout.
- Grid and flex arrangements.
- Responsive adaptations.
- Structural styles that are not already provided by shared Material UI variants.

Example:

`AppHeader.module.css`

### 12.3. Inline styles

The Material UI `sx` property is allowed for limited, localized, or dynamic styling.

Avoid extensive static `sx` definitions when a CSS Module or theme-level component override would provide a more maintainable solution.

### 12.4. Design token reuse

Shared colors, fonts, radius values, and elevation rules must not be duplicated as arbitrary values across components.

Prefer theme tokens and generated CSS variables.

When CSS variables are used, ensure they are supported by the configured Material UI theme and provide appropriate fallbacks where necessary.

### 12.5. Brand tokens

Brand colors must remain distinct from Material UI interactive palette tokens.

The implementation should expose reusable brand tokens for:

- Brand orange: `#FE5000`.
- Peach: `#FFB07C`.
- Dashboard gradient.
- Decorative brand accents.

The primary Material UI palette represents interactive orange rather than brand orange.

---

## 13. Design references

The approved UI references include:

- Dashboard design.
- Planning Wizard design.
- Results design.
- Existing project wireframes and user-flow documentation.

These references guide layout and visual hierarchy but do not override implemented backend capabilities or approved MVP scope.

Visual examples containing planning statistics, optimization scores, or schedule entries are illustrative unless connected to real application data.

---

## 14. Implementation and validation

Design decisions should be introduced incrementally.

Recommended order:

1. Configure global theme tokens and typography.
2. Update shared application shell components.
3. Implement Dashboard components.
4. Implement Wizard-specific UI components.
5. Implement Results components.
6. Verify responsive behavior and accessibility across the application.

Before considering a UI task complete:

- TypeScript compilation must pass.
- Production build must pass.
- Lint must pass.
- Relevant navigation and interactions must work.
- The screen must be checked against its design reference.
- Applicable accessibility and responsive behavior must be verified.

---

## 15. Maintenance

This document is the source of truth for the frontend visual design baseline.

New shared visual patterns should be added here when they become reusable conventions.

Changes to approved colors, typography roles, density, card styling, corner radius, or responsive strategy should be documented explicitly.

Screen-specific implementation details should remain within their corresponding features unless they establish a reusable design pattern.

---

## 16. Change history

| Version | Description |
|---|---|
| 1.0 | Initial visual design baseline: brand palette, typography, density, spacing, cards, responsive behavior |
| 1.1 | Separated brand and interactive colors; revised primary, hover and semantic colors; strengthened accessibility and contrast rules |