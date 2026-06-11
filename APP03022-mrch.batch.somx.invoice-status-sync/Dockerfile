# invoice-status-sync — imagen para correr el batch dentro de la red Docker
# (misma red que sodimac-oracle / sodimac-mssql) y evitar el NAT host->contenedor.
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
