# Conexion a Base de Datos

## Datos de Conexion

| Parametro | Valor |
|-----------|-------|
| **Host** | `localhost` |
| **Puerto** | `5434` |
| **Base de datos** | `b2b_portal` |
| **Usuario** | `wwwb2bportal` |
| **Password** | `b8@qU0YM1HU>` |
| **Driver** | PostgreSQL |

## Esquemas Disponibles

La base de datos `b2b_portal` contiene multiples esquemas. La conexion es la misma, solo se cambia el esquema segun el modulo:

| Esquema | Modulo | Proyecto |
|---------|--------|----------|
| `tenant_fiscal` | Facturacion y Pagos | `fiscal-api` |
| `tenant_finance` | Financiero | - |
| `core_audit` | Auditoria | `auditoria-api` |
| `core_security` | Seguridad | - |
| `core_utils` | Utilerias | - |
| `shared_catalogs` | Catalogos compartidos | `catalogos-api` |
| `shared_communication` | Comunicaciones | - |
| `public` | Esquema por defecto PostgreSQL | - |

## Conexion desde DBeaver

1. Nueva conexion > PostgreSQL
2. Configurar host, puerto, base de datos y usuario
3. En la pestana **PostgreSQL**: marcar "Show all databases"
4. Para cambiar esquema: click derecho en la conexion > Set Default Schema

## Conexion desde linea de comandos (jshell)

Si no tienes `psql` instalado, puedes usar `jshell` con el driver JDBC que ya esta en el repositorio Maven local:

```bash
jshell --class-path "%USERPROFILE%\.m2\repository\org\postgresql\postgresql\42.7.7\postgresql-42.7.7.jar"
```

```java
import java.sql.*;
var conn = DriverManager.getConnection(
    "jdbc:postgresql://localhost:5434/b2b_portal?currentSchema=tenant_fiscal",
    "wwwb2bportal", "b8@qU0YM1HU>"
);
var rs = conn.createStatement().executeQuery("SELECT current_schema()");
rs.next();
System.out.println("Schema: " + rs.getString(1));
```

Para cambiar de esquema, modificar `currentSchema` en la URL JDBC:
```
jdbc:postgresql://localhost:5434/b2b_portal?currentSchema=core_audit
jdbc:postgresql://localhost:5434/b2b_portal?currentSchema=tenant_finance
```

## Conexion desde Spring Boot

Cada proyecto define su esquema en `application-dev.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5434/b2b_portal?currentSchema=tenant_fiscal
spring.datasource.username=wwwb2bportal
spring.datasource.password=${DATASOURCE_PASSWORD:...}
spring.jpa.properties.hibernate.default_schema=tenant_fiscal
```

## Notas

- En ambientes de desarrollo (DEV/UAT), las credenciales se inyectan como variables de entorno en Kubernetes.
