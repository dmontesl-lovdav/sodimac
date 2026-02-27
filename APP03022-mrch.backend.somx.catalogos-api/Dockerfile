FROM registrysecaas.azurecr.io/secaas/openjdk-jdk:v21.0.6

RUN addgroup -g 1000 java && adduser -u 1000 -S -G java -D java
LABEL maintainer="DevOps Merchandise <DevOpsMerchandising@falabella.onmicrosoft.com>"
WORKDIR /app

# Datadog APM
#install wget ONLY for downloading datadog apm agent
RUN apk --no-cache add wget
RUN wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer
#removing wget after downloading the agent
RUN apk del wget

USER java

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8


COPY --chown=java:java target/*jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Duser.timezone=America/Santiago", "-Duser.language=en", "-Duser.country=US", "-jar", "app.jar"]
