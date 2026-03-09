#!/bin/bash
# ==================================================
# CONFIGURACIÓN TOMCAT - AMBIENTE: PRODUCCIÓN
# Copiar este archivo a: $CATALINA_HOME/bin/setenv.sh
# ==================================================

echo "=================================================="
echo "Configurando Tomcat para ambiente: PRODUCCIÓN"
echo "=================================================="

# Perfil Spring Boot
export SPRING_PROFILES_ACTIVE=prod

# Memoria Java (aumentada para producción)
export JAVA_OPTS="-Xms2048m -Xmx4096m"

# Garbage Collector optimizado para producción
export JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"
export JAVA_OPTS="$JAVA_OPTS -XX:MaxGCPauseMillis=200"
export JAVA_OPTS="$JAVA_OPTS -XX:InitiatingHeapOccupancyPercent=70"

# Configuración Spring
export CATALINA_OPTS="$CATALINA_OPTS -Dspring.profiles.active=prod"

# Timezone
export CATALINA_OPTS="$CATALINA_OPTS -Duser.timezone=America/Mexico_City"

# Logs optimizados para producción
export CATALINA_OPTS="$CATALINA_OPTS -Dlogging.level.root=WARN"
export CATALINA_OPTS="$CATALINA_OPTS -Dlogging.level.com.sodimac.cfdi=INFO"

# Configuración de conexiones de BD
export CATALINA_OPTS="$CATALINA_OPTS -Dhikari.maximum-pool-size=20"

# Configuración de seguridad
export JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"

echo "Perfil activo: $SPRING_PROFILES_ACTIVE"
echo "Memoria configurada: ${JAVA_OPTS}"
echo "=================================================="
