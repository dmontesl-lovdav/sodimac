@echo off
REM Script para instalar la libreria cifrado en el repositorio Maven local
REM Ejecutar desde la carpeta lib/ o ajustar la ruta del archivo JAR

echo Instalando cifrado-0.0.1-SNAPSHOT.jar en el repositorio Maven local...

mvn install:install-file -Dfile="%~dp0cifrado-0.0.1-SNAPSHOT.jar" -DgroupId=com.sodimac.lib -DartifactId=cifrado -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Instalacion completada exitosamente.
) else (
    echo.
    echo Error durante la instalacion. Verifique que Maven este instalado y en el PATH.
)

pause
