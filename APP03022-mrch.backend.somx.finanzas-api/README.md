# Finanzas API (Express + TypeScript + TypeORM + OAS3)

API para gestionar **Accounts Payable** (órdenes de compra) con Express y TypeScript.  
Incluye documentación OpenAPI 3 generada desde código y UI con Swagger.

---

## 🚀 Requisitos

- **Node.js** ≥ 18  
- **npm** ≥ 9 (o pnpm/yarn si prefieres)  
- **PostgreSQL** ≥ 13  
- Un archivo de variables de entorno (`.env`)  

---

## 🔧 Configuración

1. Copia la plantilla de entorno y completa tus valores reales:

```bash
cp .env.example .env
```

Contenido esperado:

```dotenv
# App
PORT=8091
NODE_ENV=development
SECURITY_ENABLED=true

# Database Configuration (JDBC format compatible with Spring Boot)
DATASOURCE_URL=jdbc:postgresql://localhost:5432/finanzas?currentSchema=tenant_finance&useSSL=false
DATASOURCE_USERNAME=wwwfinanzas
DATASOURCE_PASSWORD=changeme

# JWT pública (Base64)
JWT_PUBLIC_KEY_B64=MIIBIjANBgkqh...

# Additional Configuration
DB_PURCHASE_SCHEMA=ebook_purchase
LOCAL_TEMPLATE_MAXIMUM_ROWS_TO_DELETE=20

# Paginación
PAGINATION_DEFAULT_SIZE=20
PAGINATION_MAX_SIZE=100

# Integración externa
FISCAL_API_URL=https://fiscal-api.tu-dominio
```

> Consejo: **.env.example** debe llevar placeholders (p. ej. `changeme`).  
> Coloca credenciales reales **solo** en `.env` (no lo subas al repo).

---

## 📦 Instalación

```bash
npm install
```

---

## ▶️ Ejecución

### Desarrollo (hot-reload)
```bash
npm run dev
```

### Compilar y ejecutar build
```bash
npm run build
npm start
```

### Linter
```bash
npm run lint
# o no-bloqueante (no falla el proceso)
npm run lint:nb
```

---

## 📚 Documentación (OpenAPI / Swagger)

- **Generar JSON de OpenAPI**:
  ```bash
  npm run openapi:gen
  ```
- Observa cambios en `src/docs` y regenera automáticamente:
  ```bash
  npm run openapi:watch
  ```

- **Al levantar la app** se monta la UI de Swagger en:  
  [http://localhost:8091/docs](http://localhost:8091/docs)

- Endpoints JSON (útiles para CI/CD):
  - `GET /docs/openapi.json` (OAS3)
  - `GET /docs/swagger.json` (Swagger 2.0)

> Nota: Para parámetros tipo UUID se usa `pattern` en lugar de `format: uuid` para evitar falsos negativos del validador de Swagger UI..

---

## 🧱 Estructura relevante

```
src/
  controllers/
    accountsPayable.controller.ts
  docs/
    components/
      accountsPayable.ts
    paths/
      accountsPayable.ts
    openapi.ts
    openapi.json      # generado
    swagger.json      # generado
  entities/
    AccountsPayable.entities.ts
  repositories/
    accountsPayable.repo.ts
  services/
    accountsPayable.service.ts
  routes/
    accountsPayable.routes.ts
    index.ts
  middlewares/
    validate.ts
  server.ts
```

---

## 🆕 Cómo crear un nuevo servicio

1. **Crear la entidad** en `src/entities`  
   ```ts
   @Entity({ name: 'purchase_order' })
   export class AccountsPayable { ... }
   ```

2. **Crear el repositorio** en `src/repositories`  
   ```ts
   export const repo = () => datasource.getRepository(AccountsPayable);
   ```

3. **Crear el servicio** en `src/services`  
   - Métodos: `list`, `get`, `create`, `update`, `remove`

4. **Crear el controlador** en `src/controllers`  
   - Funciones Express: `list`, `getById`, `create`, `update`, `remove`

5. **Crear las rutas** en `src/routes`  
   ```ts
   r.get("/", controller.list);
   r.get("/:id", controller.getById);
   r.post("/", controller.create);
   r.patch("/:id", controller.update);
   r.delete("/:id", controller.remove);
   ```

6. **Montar la ruta en `index.ts`**  
   ```ts
   router.use("/accounts", accountsPayableRouter);
   ```

7. **Actualizar OpenAPI** en `src/docs/components` y `src/docs/paths`  
   - Definir schemas DTO y endpoints.

8. **Regenerar OpenAPI**  
   ```bash
   npm run openapi:gen
   ```

Con esto, el nuevo servicio quedará disponible y documentado.

---


---

## 📋 Checklist de creación de un servicio

- **Configuración del DataSource (TypeORM)** → define la conexión y entidades base en `src/config/typeorm-datasource.ts`
- **Entity** → defines la tabla en TypeORM.  
- **Repository** → centralizas queries (`find`, `save`, `update`, `delete`).  
- **Service** → encapsula la lógica de negocio usando el repo.  
- **Controller** → maneja requests/responses Express.  
- **Routes** → monta controladores con middlewares de validación.  
- **Index de rutas** → agrupa todas las rutas de la API.  
- **Docs (OpenAPI)** → añades schemas y paths para documentar y validar.  
- **Regenerar spec** → `npm run openapi:gen` para refrescar la doc.  

## 📜 Licencia

MIT © Finanzas API
