# Configuración de Perfiles para Despliegue WAR

## Problema
Cuando se genera un **WAR** para desplegar en Tomcat u otro servidor de aplicaciones, se necesita una forma de cambiar el perfil activo **sin modificar el WAR**.

---


## Soluciones para Distinguir entre PROD y DEV

### **Opción 1: Variable de Entorno del Sistema (Recomendada)**

Esta es la opción más limpia y profesional para entornos empresariales.

#### **En Linux (Producción):**

**1. Edita el archivo de entorno de Tomcat:**
```bash
sudo nano /opt/tomcat/bin/setenv.sh
```

**2. Agrega esta línea:**
```bash
export SPRING_PROFILES_ACTIVE=prod
```

**3. Guarda y reinicia Tomcat:**
```bash
sudo systemctl restart tomcat
```

#### **En Windows (Desarrollo):**

**Opción A - Variables de entorno del sistema:**

1. Panel de Control → Sistema → Configuración avanzada del sistema
2. Variables de entorno
3. Agregar nueva variable del sistema:
   - Nombre: `SPRING_PROFILES_ACTIVE`
   - Valor: `dev`
4. Reiniciar Tomcat

**Opción B - Script setenv.bat:**

Crea/edita `C:\apache-tomcat\bin\setenv.bat`:
```batch
set SPRING_PROFILES_ACTIVE=dev
```

---

### **Opción 2: CATALINA_OPTS en Tomcat**

Configura directamente en las opciones de inicio de Tomcat.

#### **Linux - /opt/tomcat/bin/setenv.sh:**
```bash
#!/bin/bash
export CATALINA_OPTS="$CATALINA_OPTS -Dspring.profiles.active=prod"
```

#### **Windows - C:\apache-tomcat\bin\setenv.bat:**
```batch
set "CATALINA_OPTS=%CATALINA_OPTS% -Dspring.profiles.active=dev"
```

#### **Windows Service:**

Si Tomcat está como servicio Windows:

1. Abre `tomcat9w.exe` (en el directorio bin de Tomcat)
2. Pestaña "Java"
3. En "Java Options" agrega:
```
-Dspring.profiles.active=dev
```

---

### **Opción 3: Archivo context.xml por Aplicación**

Cada servidor tiene su propio `context.xml`.

#### **Ubicación del archivo:**

**Para Tomcat:**
```
$CATALINA_HOME/conf/Catalina/localhost/cfdi.xml
```

**Contenido (Desarrollo):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <Environment name="spring.profiles.active"
                 value="dev"
                 type="java.lang.String"
                 override="false"/>
</Context>
```

**Contenido (Producción):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <Environment name="spring.profiles.active"
                 value="prod"
                 type="java.lang.String"
                 override="false"/>
</Context>
```

---

### **Opción 4: Archivo de Configuración Externo (Más Segura)**

Coloca un archivo de configuración **fuera del WAR** con las credenciales reales.

#### **Paso 1: Crea un directorio de configuración**

**Linux:**
```bash
sudo mkdir -p /etc/cfdi/config
sudo chmod 700 /etc/cfdi/config  # Solo root puede acceder
```

**Windows:**
```cmd
mkdir C:\config\cfdi
```

#### **Paso 2: Crea archivos de configuración por ambiente**

**Desarrollo - /etc/cfdi/config/databaseFiscal-dev.properties:**
```properties
# jdbc.X - DESARROLLO
jdbc.fiscal.driverClassName=com.mysql.cj.jdbc.Driver
jdbc.fiscal.url=jdbc:mysql://[SERVIDOR_DEV]:3306/sodimacfiscal?...
jdbc.fiscal.user=[USUARIO_DEV]
jdbc.fiscal.pass=[PASSWORD_DEV]
```

**Producción - /etc/cfdi/config/databaseFiscal-prod.properties:**
```properties
# jdbc.X - PRODUCCIÓN
jdbc.fiscal.driverClassName=com.mysql.cj.jdbc.Driver
jdbc.fiscal.url=jdbc:mysql://[SERVIDOR_PROD]:3306/sodimacfiscal?...
jdbc.fiscal.user=[USUARIO_PROD]
jdbc.fiscal.pass=[PASSWORD_PROD]
```

#### **Paso 3: Configura Tomcat para usar archivos externos**

**setenv.sh (Linux):**
```bash
export SPRING_PROFILES_ACTIVE=prod
export CATALINA_OPTS="$CATALINA_OPTS -Dspring.config.additional-location=file:/etc/cfdi/config/"
```

---

## 📊 Comparativa de Opciones

| Opción | Facilidad | Seguridad | Recomendada para | Reinicio Tomcat |
|--------|-----------|-----------|------------------|-----------------|
| Variable de entorno | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Producción | Sí |
| CATALINA_OPTS | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Producción/Dev | Sí |
| context.xml | ⭐⭐⭐ | ⭐⭐⭐ | Desarrollo | No* |
| Config externo | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Empresas grandes | Sí |

---

## Verificar Perfil Activo

### **Opción 1: Revisar logs de Tomcat**

**Linux:**
```bash
tail -f /opt/tomcat/logs/catalina.out | grep "profiles are active"
```

**Windows:**
```cmd
type C:\apache-tomcat\logs\catalina.out | findstr "profiles are active"
```

Deberías ver:
```
The following profiles are active: dev
```
o
```
The following profiles are active: prod
```

### **Opción 2: Agregar endpoint de verificación**

Crea un controlador simple:

```java
@RestController
public class ProfileController {

    @Autowired
    private Environment env;

    @GetMapping("/api/perfil-activo")
    public String getActiveProfile() {
        return "Perfil activo: " + Arrays.toString(env.getActiveProfiles());
    }
}
```

**Accede desde el navegador:**
```
http://localhost:8080/cfdi/api/perfil-activo
```

---

**Última actualización:** 2025-10-31
