/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        brand: {
          50: "#eef2ff",
          100: "#e0e7ff",
          200: "#c7d2fe",
          300: "#a5b4fc",
          400: "#818cf8",
          500: "#6366f1",
          600: "#4f46e5",
          700: "#4338ca",
          800: "#3730a3",
          900: "#312e81",
        },
        surface: {
          0: "#0a0b0f",
          50: "#0f1117",
          100: "#151822",
          200: "#1c2029",
          300: "#252a36",
          400: "#323847",
          500: "#454c5e",
        },
        ink: {
          50: "#f8fafc",
          100: "#e8eaee",
          200: "#c9ccd6",
          300: "#9ba0af",
          400: "#767c8e",
          500: "#5c6273",
          600: "#454a58",
        },
      },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "sans-serif"],
        display: ["Lexend", "Inter", "ui-sans-serif", "sans-serif"],
      },
      boxShadow: {
        soft: "0 1px 2px 0 rgb(0 0 0 / 0.2)",
        card: "0 4px 16px -4px rgb(0 0 0 / 0.4), 0 2px 6px -2px rgb(0 0 0 / 0.3)",
        lifted: "0 12px 32px -8px rgb(0 0 0 / 0.5), 0 4px 12px -4px rgb(0 0 0 / 0.35)",
        glow: "0 0 0 1px rgb(99 102 241 / 0.15), 0 4px 20px -4px rgb(99 102 241 / 0.25)",
      },
      backgroundImage: {
        "grid-pattern":
          "linear-gradient(to right, rgb(255 255 255 / 0.05) 1px, transparent 1px), linear-gradient(to bottom, rgb(255 255 255 / 0.05) 1px, transparent 1px)",
        "glow-radial":
          "radial-gradient(ellipse 60% 50% at 50% 0%, rgb(99 102 241 / 0.15), transparent 100%)",
      },
      keyframes: {
        aurora1: {
          "0%, 100%": { transform: "translate(0%, 0%) scale(1)" },
          "33%": { transform: "translate(15%, -10%) scale(1.15)" },
          "66%": { transform: "translate(-10%, 10%) scale(0.9)" },
        },
        aurora2: {
          "0%, 100%": { transform: "translate(0%, 0%) scale(1)" },
          "33%": { transform: "translate(-15%, 15%) scale(0.9)" },
          "66%": { transform: "translate(10%, -15%) scale(1.1)" },
        },
        aurora3: {
          "0%, 100%": { transform: "translate(0%, 0%) scale(1)" },
          "50%": { transform: "translate(8%, 12%) scale(1.2)" },
        },
        "pulse-slow": {
          "0%, 100%": { opacity: "0.4" },
          "50%": { opacity: "0.7" },
        },
      },
      animation: {
        aurora1: "aurora1 18s ease-in-out infinite",
        aurora2: "aurora2 22s ease-in-out infinite",
        aurora3: "aurora3 14s ease-in-out infinite",
        "pulse-slow": "pulse-slow 6s ease-in-out infinite",
      },
    },
  },
  plugins: [],
}
