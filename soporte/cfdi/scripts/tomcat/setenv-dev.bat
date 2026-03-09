@echo off
REM ==================================================
REM CONFIGURACIÓN TOMCAT - AMBIENTE: DESARROLLO
REM Copiar este archivo a: %CATALINA_HOME%\bin\setenv.bat
REM ==================================================

echo ==================================================
echo Configurando Tomcat para ambiente: DESARROLLO
echo ==================================================

REM Perfil Spring Boot
set SPRING_PROFILES_ACTIVE=dev

REM Memoria Java
set "JAVA_OPTS=-Xms512m -Xmx1024m"

REM Configuración Spring
set "CATALINA_OPTS=%CATALINA_OPTS% -Dspring.profiles.active=dev"

REM Timezone
set "CATALINA_OPTS=%CATALINA_OPTS% -Duser.timezone=America/Mexico_City"

REM Logs más verbosos para desarrollo
set "CATALINA_OPTS=%CATALINA_OPTS% -Dlogging.level.root=INFO"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dlogging.level.com.sodimac.cfdi=DEBUG"

REM Habilitar remote debugging (puerto 5005)
REM set "CATALINA_OPTS=%CATALINA_OPTS% -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

echo Perfil activo: %SPRING_PROFILES_ACTIVE%
echo Memoria configurada: %JAVA_OPTS%
echo ==================================================
