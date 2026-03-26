#!/bin/bash
# Configuración de JVM para wsmdlwticket - Producción (Tomcat 88)

# Memoria
export JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx2048m"
export JAVA_OPTS="$JAVA_OPTS -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m"

# Configuración general
export JAVA_OPTS="$JAVA_OPTS -Djava.awt.headless=true"
export JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"
export JAVA_OPTS="$JAVA_OPTS -Duser.timezone=America/Mexico_City"
export JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"

# Perfil de Spring Boot - PRODUCCION
export JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=prod"

# Garbage Collector
export JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"
export JAVA_OPTS="$JAVA_OPTS -XX:MaxGCPauseMillis=200"

# Logs de GC (Java 11)
#export JAVA_OPTS="$JAVA_OPTS -Xlog:gc*:file=$CATALINA_BASE/logs/gc.log:time,uptime:filecount=5,filesize=10M"
export JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
export JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDateStamps"
export JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCTimeStamps"
export JAVA_OPTS="$JAVA_OPTS -Xloggc:$CATALINA_BASE/logs/gc.log"
export JAVA_OPTS="$JAVA_OPTS -XX:+UseGCLogFileRotation"
export JAVA_OPTS="$JAVA_OPTS -XX:NumberOfGCLogFiles=5"
export JAVA_OPTS="$JAVA_OPTS -XX:GCLogFileSize=10M"

echo "========================================"
echo "Tomcat 88 JVM Configuration - wsmdlwticket"
echo "Profile: PROD"
echo "========================================"
echo "JAVA_OPTS: $JAVA_OPTS"
echo "========================================"
