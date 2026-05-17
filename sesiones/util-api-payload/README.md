# Utils API

API para gestionar utilidades del sistema incluyendo parametros, modulos, mensajes, procesos, tipos de item e items.

## Requisitos

- Node.js >= 18
- npm >= 9
- PostgreSQL >= 13

## Configuracion

```bash
cp .env.example .env
```

## Instalacion

```bash
npm install
```

## Ejecucion

### Desarrollo
```bash
npm run dev
```

### Produccion
```bash
npm run build
npm start
```

## Modulos

| Modulo | Ruta |
|--------|------|
| Parameters | /parameters |
| Modules | /modules |
| Messages | /messages |
| Application Messages | /application-messages |
| Processes | /processes |
| Item Types | /item-types |
| Items | /items |
