# Scripts de Configuración para Tomcat

## 📋 Descripción

Scripts listos para configurar el perfil de Spring Boot en Tomcat para despliegues WAR.

---

## 📁 Archivos Incluidos

```
scripts/tomcat/
├── setenv-dev.sh       # Linux/Unix - Desarrollo
├── setenv-prod.sh      # Linux/Unix - Producción
├── setenv-dev.bat      # Windows - Desarrollo
└── setenv-prod.bat     # Windows - Producción
```

---

## 🚀 Cómo Usar

### **Opción 1: Copiar directamente**

#### Linux/Unix - Desarrollo:
```bash
cp scripts/tomcat/setenv-dev.sh /opt/tomcat/bin/setenv.sh
chmod +x /opt/tomcat/bin/setenv.sh
sudo systemctl restart tomcat
```

#### Linux/Unix - Producción:
```bash
cp scripts/tomcat/setenv-prod.sh /opt/tomcat/bin/setenv.sh
chmod +x /opt/tomcat/bin/setenv.sh
sudo systemctl restart tomcat
```

#### Windows - Desarrollo:
```cmd
copy scripts\tomcat\setenv-dev.bat C:\apache-tomcat\bin\setenv.bat
net stop Tomcat9
net start Tomcat9
```

#### Windows - Producción:
```cmd
copy scripts\tomcat\setenv-prod.bat C:\apache-tomcat\bin\setenv.bat
net stop Tomcat9
net start Tomcat9
```

---

### **Opción 2: Crear enlace simbólico (Linux)**

Esto facilita cambiar entre ambientes sin copiar archivos.

#### Configuración inicial:
```bash
# Copiar scripts a un directorio común
sudo mkdir -p /etc/cfdi/scripts
sudo cp scripts/tomcat/setenv-*.sh /etc/cfdi/scripts/
sudo chmod +x /etc/cfdi/scripts/setenv-*.sh
```

#### Para usar Desarrollo:
```bash
sudo ln -sf /etc/cfdi/scripts/setenv-dev.sh /opt/tomcat/bin/setenv.sh
sudo systemctl restart tomcat
```

#### Para usar Producción:
```bash
sudo ln -sf /etc/cfdi/scripts/setenv-prod.sh /opt/tomcat/bin/setenv.sh
sudo systemctl restart tomcat
```

---

## ⚙️ Configuración Incluida en cada Script

### **Desarrollo (dev)**

| Configuración | Valor |
|--------------|-------|
| Perfil Spring | `dev` |
| Memoria inicial | 512 MB |
| Memoria máxima | 1 GB |
| Logging level | DEBUG |
| Remote debugging | Deshabilitado (comentado) |

### **Producción (prod)**

| Configuración | Valor |
|--------------|-------|
| Perfil Spring | `prod` |
| Memoria inicial | 2 GB |
| Memoria máxima | 4 GB |
| Logging level | WARN/INFO |
| Garbage Collector | G1GC optimizado |
| Pool de conexiones BD | 20 |

---

## ✅ Verificar Configuración

Después de copiar el script y reiniciar Tomcat:

### Linux:
```bash
tail -f /opt/tomcat/logs/catalina.out | grep "profiles are active"
```

### Windows:
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

---

## 🔧 Personalización

Si necesitas ajustar la configuración, edita los scripts antes de copiarlos:

**Ejemplo - Aumentar memoria en desarrollo:**
```bash
# En setenv-dev.sh, línea 13:
export JAVA_OPTS="-Xms1024m -Xmx2048m"
```

**Ejemplo - Cambiar nivel de logs:**
```bash
# En setenv-prod.sh, líneas 26-27:
export CATALINA_OPTS="$CATALINA_OPTS -Dlogging.level.root=INFO"
export CATALINA_OPTS="$CATALINA_OPTS -Dlogging.level.com.sodimac.cfdi=DEBUG"
```

---

## 📞 Contacto

Para dudas sobre estos scripts, contacta al equipo DevOps.
