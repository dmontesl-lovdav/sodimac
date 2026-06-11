# ⚠️ SOLO TESTING LOCAL — NO es el modelo de despliegue.
# El batch se despliega como jar/proceso (java -jar ... --spring.profiles.active=local).
# Esta imagen existe únicamente para PROBAR SAPITO en una PC donde Oracle corre en
# Docker: host->contenedor rompe el redirect dedicated-server de Oracle (NAT); metiendo
# el batch en la misma red Docker el redirect resuelve y se valida SAPITO.
# En Sodimac real Oracle es un servidor (ensenada:1541), sin NAT -> el jar local basta.
#
# Build:  mvn -DskipTests package && docker build -t invoice-status-sync .
# Run:    docker run --rm --network sodimac-net -p 8085:8085 \
#           invoice-status-sync --spring.profiles.active=docker
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY target/invoice-status-sync-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=docker"]
