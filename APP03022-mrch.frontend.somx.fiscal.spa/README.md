# Portal Fiscal - Proveedores

Sistema web para la gestión de complementos de pago fiscales para proveedores.

## 🚀 Características

- **Publicación de Complementos**: Carga y validación de complementos de pago (XML/PDF)
- **Consulta de Historial**: Visualización de complementos publicados
- **Validaciones Automáticas**: Verificación de facturas y notas de crédito relacionadas
- **Gestión Documental**: Almacenamiento y consulta de documentos fiscales

## 📋 Requisitos

- Node.js >= 20.18.1
- npm o yarn

## 🛠️ Instalación

```bash
npm install
```

## 🏃 Ejecución

### Desarrollo
```bash
npm start
# o
npm run dev
```

La aplicación estará disponible en http://localhost:3703

### Producción
```bash
npm run build
```

## 📁 Estructura del Proyecto

```
src/
├── features/
│   ├── home/              # Menú principal
│   └── complement/        # Módulo de complementos
│       ├── api/          # Servicios API
│       ├── components/   # Componentes React
│       └── interfaces.ts # TypeScript interfaces
├── shared/
│   └── components/       # Componentes reutilizables
├── configuration/        # Configuración global
└── types/               # Definiciones de tipos
```

## 🎨 Tecnologías

- React 18.3
- TypeScript 4.7
- React Router 6
- Tailwind CSS 4
- Webpack 5
