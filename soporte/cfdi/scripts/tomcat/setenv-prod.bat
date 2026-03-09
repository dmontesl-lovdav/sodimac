@echo off
REM ==================================================
REM CONFIGURACIÓN TOMCAT - AMBIENTE: PRODUCCIÓN
REM Copiar este archivo a: %CATALINA_HOME%\bin\setenv.bat
REM ==================================================

echo ==================================================
echo Configurando Tomcat para ambiente: PRODUCCIÓN
echo ==================================================

REM Perfil Spring Boot
set SPRING_PROFILES_ACTIVE=prod

REM Memoria Java (aumentada para producción)
set "JAVA_OPTS=-Xms2048m -Xmx4096m"

REM Garbage Collector optimizado para producción
set "JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC"
set "JAVA_OPTS=%JAVA_OPTS% -XX:MaxGCPauseMillis=200"
set "JAVA_OPTS=%JAVA_OPTS% -XX:InitiatingHeapOccupancyPercent=70"

REM Configuración Spring
set "CATALINA_OPTS=%CATALINA_OPTS% -Dspring.profiles.active=prod"

REM Timezone
set "CATALINA_OPTS=%CATALINA_OPTS% -Duser.timezone=America/Mexico_City"

REM Logs optimizados para producción
set "CATALINA_OPTS=%CATALINA_OPTS% -Dlogging.level.root=WARN"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dlogging.level.com.sodimac.cfdi=INFO"

REM Configuración de conexiones de BD
set "CATALINA_OPTS=%CATALINA_OPTS% -Dhikari.maximum-pool-size=20"

REM Configuración de seguridad
set "JAVA_OPTS=%JAVA_OPTS% -Djava.security.egd=file:/dev/./urandom"

echo Perfil activo: %SPRING_PROFILES_ACTIVE%
echo Memoria configurada: %JAVA_OPTS%
echo ==================================================
