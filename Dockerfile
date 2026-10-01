# ---------- Stage 1: build del WAR con Maven + JDK 8 ----------
FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Stage 2: Tomcat 10.1 con JRE 17 ----------
FROM tomcat:10.1-jre17-temurin-jammy

# 1. Installa curl (serve per l'HEALTHCHECK) e pulisci la cache apt
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# 2. Crea l'utente non privilegiato tomcat
RUN groupadd -r tomcat && \
    useradd -r -g tomcat -d /usr/local/tomcat -s /sbin/nologin tomcat

# 3. Rimuovi le webapp di default
RUN rm -rf /usr/local/tomcat/webapps/*

# 4. Copia il WAR assegnando la proprietà all'utente tomcat
COPY --from=build --chown=tomcat:tomcat /app/target/ecommerce.war /usr/local/tomcat/webapps/ROOT.war

# 5. Assicura che le directory di Tomcat siano scrivibili dall'utente tomcat
RUN chown -R tomcat:tomcat /usr/local/tomcat

# 6. Esegui come utente non privilegiato
USER tomcat

# 7. Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD curl -fsS http://localhost:8080/ > /dev/null || exit 1

EXPOSE 8080

CMD ["catalina.sh", "run"]