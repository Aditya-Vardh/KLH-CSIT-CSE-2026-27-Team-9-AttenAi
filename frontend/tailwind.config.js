/** @type {import('tailwindcss').Config} */

// ─── Brand palette ─────────────────────────────────────────────────────────
// Exact values from the design spec. All tints are derived as CSS-in-JS
// color-mix equivalents via Tailwind opacity utilities — no ad-hoc hex codes
// anywhere in the component tree.
//
//   text       #fcf8e7   warm cream
//   background #1c1704   near-black warm brown
//   primary    #efd581   gold
//   secondary  #498c12   olive green
//   accent     #55e638   bright green  (use sparingly — success / live only)

export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  darkMode: 'class',          // dark class on <html> — we always add it
  theme: {
    // Replace the entire default scale for font sizes so only spec sizes exist
    fontSize: {
      '2xs':['0.625rem',  { lineHeight: '0.875rem' }],
      xs:   ['0.688rem',  { lineHeight: '1rem'     }],
      sm:   ['0.750rem',  { lineHeight: '1.1rem'   }],
      base: ['1rem',      { lineHeight: '1.5rem'   }],
      xl:   ['1.333rem',  { lineHeight: '1.8rem'   }],
      '2xl':['1.777rem',  { lineHeight: '2.2rem'   }],
      '3xl':['2.369rem',  { lineHeight: '2.8rem'   }],
      '4xl':['3.158rem',  { lineHeight: '3.6rem'   }],
      '5xl':['4.210rem',  { lineHeight: '4.8rem'   }],
    },
    fontWeight: {
      normal: '400',
      bold:   '700',
    },
    extend: {
      colors: {
        // ── Core tokens ──────────────────────────────────────────────────
        text:       '#fcf8e7',
        background: '#1c1704',
        primary:    '#efd581',
        secondary:  '#498c12',
        accent:     '#55e638',

        // ── Primary tints (gold) — used for hover/border/subtle fills ───
        // Naming: primary-[opacity%]  →  actual opacity applied via Tailwind
        // The actual hex-at-opacity values are generated here so Tailwind
        // can JIT them as bg-primary-10, text-primary-70, etc.
        'primary-10':  'rgb(239 213 129 / 0.10)',
        'primary-20':  'rgb(239 213 129 / 0.20)',
        'primary-30':  'rgb(239 213 129 / 0.30)',
        'primary-50':  'rgb(239 213 129 / 0.50)',
        'primary-70':  'rgb(239 213 129 / 0.70)',

        // ── Secondary tints (olive green) ───────────────────────────────
        'secondary-10': 'rgb(73 140 18 / 0.10)',
        'secondary-20': 'rgb(73 140 18 / 0.20)',
        'secondary-30': 'rgb(73 140 18 / 0.30)',
        'secondary-50': 'rgb(73 140 18 / 0.50)',

        // ── Accent tints (bright green) ─────────────────────────────────
        'accent-10': 'rgb(85 230 56 / 0.10)',
        'accent-20': 'rgb(85 230 56 / 0.20)',
        'accent-30': 'rgb(85 230 56 / 0.30)',

        // ── Surface layers (warm dark hierarchy) ────────────────────────
        // bg0 = page background = background token
        // bg1 = elevated card    ≈ bg + 4% white
        // bg2 = inset / input    ≈ bg + 2% white
        // bg3 = highlight row    ≈ bg + 8% white
        'bg0': '#1c1704',
        'bg1': '#251f07',
        'bg2': '#2e260a',
        'bg3': '#38300e',
        'bg4': '#453b12',

        // ── Border token ────────────────────────────────────────────────
        'border-subtle': 'rgb(239 213 129 / 0.12)',
        'border-default':'rgb(239 213 129 / 0.20)',
        'border-strong': 'rgb(239 213 129 / 0.35)',

        // ── Semantic — these map to the three brand colors ───────────────
        // success  → accent   (bright green)
        // warning  → primary  (gold)
        // error    → a warm red that doesn't fight the palette
        // info     → secondary (olive)
        'success': '#55e638',
        'warning': '#efd581',
        'error':   '#f87171',
        'info':    '#498c12',

        // ── Text hierarchy ───────────────────────────────────────────────
        'text-base':    '#fcf8e7',
        'text-muted':   'rgb(252 248 231 / 0.55)',
        'text-faint':   'rgb(252 248 231 / 0.35)',
        'text-inverse': '#1c1704',
      },

      fontFamily: {
        heading: ['"Sedan"', 'Georgia', 'serif'],
        body:    ['"Sedan"', 'Georgia', 'serif'],
        // keep mono for code / timestamps
        mono:    ['"JetBrains Mono"', '"Fira Code"', 'ui-monospace', 'monospace'],
      },

      boxShadow: {
        'xs':     '0 1px 2px 0 rgb(0 0 0 / 0.25)',
        'card':   '0 1px 3px 0 rgb(0 0 0 / 0.35), 0 4px 12px 0 rgb(0 0 0 / 0.25)',
        'card-md':'0 4px 16px 0 rgb(0 0 0 / 0.45)',
        'card-lg':'0 8px 30px 0 rgb(0 0 0 / 0.55)',
        // gold glow — used on primary CTAs and active nav
        'glow':   '0 0 20px rgb(239 213 129 / 0.30)',
        'glow-sm':'0 0 10px rgb(239 213 129 / 0.22)',
        'glow-lg':'0 0 40px rgb(239 213 129 / 0.40)',
        // accent glow — live indicators
        'glow-accent':'0 0 12px rgb(85 230 56 / 0.40)',
      },

      borderRadius: {
        '4xl': '2rem',
        '5xl': '2.5rem',
      },

      spacing: {
        '18': '4.5rem',
        '22': '5.5rem',
        '26': '6.5rem',
        '30': '7.5rem',
      },

      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        // hero gradient uses only the dark background palette
        'hero':   'linear-gradient(135deg, #1c1704 0%, #251f07 40%, #2e260a 100%)',
        // gold shimmer for skeleton
        'shimmer-dark': 'linear-gradient(90deg, #251f07 0%, #38300e 50%, #251f07 100%)',
      },

      animation: {
        'fade-up':    'fadeUp 0.22s ease-out both',
        'fade-in':    'fadeIn 0.18s ease-out both',
        'scale-in':   'scaleIn 0.15s ease-out both',
        'pulse-soft': 'pulseSoft 2.4s ease-in-out infinite',
        'shimmer':    'shimmer 1.6s linear infinite',
        'float':      'float 3s ease-in-out infinite',
        'glow-pulse': 'glowPulse 2s ease-in-out infinite',
      },
      keyframes: {
        fadeUp:   { from:{ opacity:'0', transform:'translateY(6px)' }, to:{ opacity:'1', transform:'translateY(0)' } },
        fadeIn:   { from:{ opacity:'0' }, to:{ opacity:'1' } },
        scaleIn:  { from:{ opacity:'0', transform:'scale(0.96)' }, to:{ opacity:'1', transform:'scale(1)' } },
        pulseSoft:{ '0%,100%':{ opacity:'1' }, '50%':{ opacity:'0.6' } },
        shimmer:  { '0%':{ backgroundPosition:'-200% 0' }, '100%':{ backgroundPosition:'200% 0' } },
        float:    { '0%,100%':{ transform:'translateY(0)' }, '50%':{ transform:'translateY(-8px)' } },
        glowPulse:{ '0%,100%':{ boxShadow:'0 0 8px rgb(239 213 129 / 0.20)' }, '50%':{ boxShadow:'0 0 20px rgb(239 213 129 / 0.45)' } },
      },

      transitionTimingFunction: {
        'snappy': 'cubic-bezier(0.2, 0, 0, 1)',
        'spring': 'cubic-bezier(0.175, 0.885, 0.32, 1.275)',
      },
    },
  },
  plugins: [],
}
