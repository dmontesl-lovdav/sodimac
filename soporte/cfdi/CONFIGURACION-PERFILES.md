# Configuración de Perfiles - Aplicación CFDI

## 📋 Descripción

Este proyecto está configurado con **múltiples perfiles** de Spring Boot para facilitar el despliegue en diferentes ambientes (Desarrollo, Producción, etc.) cambiando únicamente las credenciales de base de datos.

---

## 🎯 Perfiles Disponibles

| Perfil | Descripción | Base de Datos Fiscal | Base de Datos Factura |
|--------|-------------|----------------------|----------------------|
| **prod** | Producción | [Consultar con DevOps] | [Consultar con DevOps] |
| **dev** | Desarrollo | [Consultar con DevOps] | [Consultar con DevOps] |

---

## 📁 Archivos de Configuración

### Estructura de Archivos

```
src/main/resources/
├── application.properties                    # Configuración principal
├── databaseFiscal.properties                # Config base BD Fiscal
├── databaseFiscal-dev.properties            # Config BD Fiscal - Desarrollo
├── databaseFiscal-prod.properties           # Config BD Fiscal - Producción
├── databaseFactura.properties               # Config base BD Factura
├── databaseFactura-dev.properties           # Config BD Factura - Desarrollo
└── databaseFactura-prod.properties          # Config BD Factura - Producción
```

### Jerarquía de Carga

Spring Boot carga los archivos en este orden:
1. `databaseFiscal.properties` (base)
2. `databaseFiscal-{perfil}.properties` (sobreescribe la base)

---

## 🔧 Cómo Activar un Perfil

### Opción 1: application.properties (Recomendado)

Edita el archivo `application.properties`:

```properties
# Cambiar este valor según el ambiente
spring.profiles.active=prod   # Para producción
# spring.profiles.active=dev  # Para desarrollo
```

### Opción 2: Variable de Entorno

Configura la variable de entorno antes de iniciar la aplicación:

**Windows:**
```cmd
set SPRING_PROFILES_ACTIVE=dev
java -jar cfdi-0.0.1-SNAPSHOT.jar
```

**Linux/Mac:**
```bash
export SPRING_PROFILES_ACTIVE=dev
java -jar cfdi-0.0.1-SNAPSHOT.jar
```

### Opción 3: Argumento de Línea de Comandos

Al ejecutar la aplicación:

```bash
java -jar cfdi-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Opción 4: Maven/Spring Boot

Al ejecutar con Maven:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Opción 5: IDE (Eclipse/IntelliJ)

**Eclipse:**
1. Run → Run Configurations
2. Selecciona tu aplicación Spring Boot
3. Pestaña "Arguments"
4. En "Program arguments" agrega: `--spring.profiles.active=dev`

**IntelliJ IDEA:**
1. Run → Edit Configurations
2. Selecciona tu aplicación
3. En "Active profiles" escribe: `dev`

---

## 🗄️ Configuración de Base de Datos por Perfil

### Perfil: PRODUCCIÓN (prod)

**Base de Datos Fiscal:**
- Servidor: `[SERVIDOR_PROD]:[PUERTO]`
- Base de Datos: `sodimacfiscal`
- Usuario: `[USUARIO_PROD]`
- Contraseña: `[PASSWORD_PROD]`

**Base de Datos Factura:**
- Servidor: `[SERVIDOR_PROD]:[PUERTO]`
- Base de Datos: `facturacion`
- Usuario: `[USUARIO_PROD]`
- Contraseña: `[PASSWORD_PROD]`

**Consulta con el equipo de DevOps o BD para obtener las credenciales reales.**

### Perfil: DESARROLLO (dev)

**Base de Datos Fiscal:**
- Servidor: `[SERVIDOR_DEV]:[PUERTO]`
- Base de Datos: `sodimacfiscal`
- Usuario: `[USUARIO_DEV]`
- Contraseña: `[PASSWORD_DEV]`

**Base de Datos Factura:**
- Servidor: `[SERVIDOR_DEV]:[PUERTO]`
- Base de Datos: `facturacion`
- Usuario: `[USUARIO_DEV]`
- Contraseña: `[PASSWORD_DEV]`

**Consulta con el equipo de DevOps o BD para obtener las credenciales reales.**

---

## Verificar Perfil Activo

### Al Iniciar la Aplicación

Revisa los logs al iniciar, verás algo como:

```
The following profiles are active: dev
```

O para producción:

```
The following profiles are active: prod
```

### Agregar Log en el Código (Opcional)

Puedes agregar esto en cualquier @Configuration:

```java
@Autowired
private Environment env;

@PostConstruct
public void init() {
    logger.info("===== PERFIL ACTIVO: {} =====",
        Arrays.toString(env.getActiveProfiles()));
}
```

---

## Agregar un Nuevo Perfil (Ej: QA)

### Paso 1: Crear archivos de configuración

```bash
src/main/resources/databaseFiscal-qa.properties
src/main/resources/databaseFactura-qa.properties
```

### Paso 2: Configurar credenciales

Copia el contenido de `-dev.properties` o `-prod.properties` y ajusta las credenciales.

### Paso 3: Activar el perfil

```properties
spring.profiles.active=qa
```

---

## Notas Importantes

1. **Por defecto** la aplicación usa el perfil `prod` (producción)

2. **Los archivos base** (`databaseFiscal.properties` y `databaseFactura.properties`) sirven como fallback si alguna propiedad no está en el perfil específico

3. **Las credenciales** en los archivos específicos de perfil (`-dev`, `-prod`) **sobrescriben** las del archivo base

---

**Última actualización:** 2025-10-31
