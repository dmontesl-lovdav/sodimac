#!/bin/bash
# ==================================================
# CONFIGURACIÓN TOMCAT - AMBIENTE: DESARROLLO
# Copiar este archivo a: $CATALINA_HOME/bin/setenv.sh
# ==================================================

echo "=================================================="
echo "Configurando Tomcat para ambiente: DESARROLLO"
echo "=================================================="

# Perfil Spring Boot
export SPRING_PROFILES_ACTIVE=dev

# Memoria Java
export JAVA_OPTS="-Xms512m -Xmx1024m"

# Configuración Spring
export CATALINA_OPTS="$CATALINA_OPTS -Dspring.profiles.active=dev"

# Timezone
export CATALINA_OPTS="$CATALINA_OPTS -Duser.timezone=America/Mexico_City"

# Logs más verbosos para desarrollo
export CATALINA_OPTS="$CATALINA_OPTS -Dlogging.level.root=INFO"
export CATALINA_OPTS="$CATALINA_OPTS -Dlogging.level.com.sodimac.cfdi=DEBUG"

# Habilitar remote debugging (puerto 5005)
# export CATALINA_OPTS="$CATALINA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

echo "Perfil activo: $SPRING_PROFILES_ACTIVE"
echo "Memoria configurada: ${JAVA_OPTS}"
echo "=================================================="
