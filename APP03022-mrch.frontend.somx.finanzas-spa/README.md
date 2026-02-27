# Mrch.Frontend.Somx.Finanzas SPA

This repository contains the **Single Page Application (SPA) frontend for the Finance module** of the Sodimac Portal.
It is built with **React, TypeScript, Webpack, and TailwindCSS**, and is designed to provide a modern, maintainable, and scalable user experience for finance-related workflows.

---

## 🚀 Getting Started

### Prerequisites
- Node.js >= 20.x
- npm >= 9.x

### Installation
Clone the repository and install dependencies:

```bash
git clone https://gitlab.falabella.tech/rtl/merchandise-ti-corp/sourcing/portalprovsomx/sodimac/frontend/mrch.frontend.somx.finanzas-spa.git
cd mrch.frontend.somx.finanzas-spa
npm install  
```

### Development
Run the local development server with hot reload:

```bash
npm run dev
```

The app will be available at http://localhost:3702.

### Build
Generate a production-ready build in the `dist/` folder:

```bash
npm run build
```

---

## 📂 Project Structure

```
src/
 ├── assets/          # Static assets (images, icons, etc.)
 ├── configuration/   # Environment and API configuration
 ├── domain/          # Domain models & services
 ├── features/        # Feature modules (Finance screens, forms, etc.)
 │   └── maintainers/ # Maintainers views (if any)
 ├── shared/          # Shared components & UI
 │   └── components/ui/navigation/Breadcrumb.tsx
 ├── store/           # State management
 ├── types/           # Global TypeScript types
 ├── App.tsx          # Root component
 ├── index.tsx        # Application entry point
 └── styles.css       # Tailwind entry
```

---

## 🧩 Features

- Finance-specific workflows:
  - Invoice and purchase order tracking
  - Payment requests and validations
  - Finance dashboards and reports
- Modern UI built with TailwindCSS
- SPA navigation with React Router
- Modular architecture with clear domain separation

---

## 🛠️ Available Scripts

- `npm run dev` → Start development server  
- `npm run build` → Build production bundle  
- `npm run lint` → Run ESLint on source code  
- `npm run test` → Run Jest test suite  

---

## ⚙️ Webpack Notes

- Dev server is for **local development only** (`webpack-dev-server`).
- Production assets are generated under `dist/` and should be served by a real web server (Nginx/Apache/Express).

Optional performance tweaks (add to `webpack.config.js` if needed):

```js
optimization: {
  splitChunks: { chunks: 'all' },
  runtimeChunk: 'single',
},
performance: {
  hints: 'warning',
  maxAssetSize: 400000,
  maxEntrypointSize: 400000,
}
```

---

## 🤝 Contributing

1. Create a feature branch from `main`.
2. Submit a merge request with a clear description.
3. Ensure linting and tests pass before review.

---

## 📄 License

This project is proprietary and internal to Sodimac.  
Unauthorized copying or distribution is not permitted.
