# ---------- Stage 1: build del WAR con Maven + JDK 8 ----------
FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app

# Copia il pom e scarica le dipendenze (layer cachato)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia i sorgenti e compila il WAR
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Stage 2: Tomcat 10.1 con il WAR ----------
FROM tomcat:10.1-jdk17

# --- INIZIO MODIFICHE PER LA SICUREZZA ---

# 1. Aggiorna tutti i pacchetti di sistema per applicare le patch di sicurezza.
#    Questo risolve la maggior parte delle CVE "fixabili" (es. expat, util-linux).
#    --no-install-recommends evita di installare pacchetti non necessari.
RUN apt-get update && \
    apt-get upgrade -y --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

# 2. Crea un utente non privilegiato per eseguire Tomcat.
#    Questo mitiga il rischio delle CVE "non fixabili" (es. pcre2, zlib).
#    Anche se un attaccante sfruttasse una CVE dell'OS, non avrebbe privilegi di root.
RUN groupadd -r tomcat && useradd -r -g tomcat -d /usr/local/tomcat -s /sbin/nologin tomcat

# 3. Rimuovi le webapp di default (riduce la superficie d'attacco)
RUN rm -rf /usr/local/tomcat/webapps/*

# 4. Copia il WAR assegnando la proprietà all'utente non privilegiato
COPY --from=build --chown=tomcat:tomcat /app/target/ecommerce.war /usr/local/tomcat/webapps/ROOT.war

# 5. Assicura che le directory di Tomcat siano accessibili all'utente non privilegiato
RUN chown -R tomcat:tomcat /usr/local/tomcat

# 6. Esegui il container come utente non privilegiato
USER tomcat

# 7. Aggiungi un HEALTHCHECK per verificare che l'applicazione risponda
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD curl -fsS http://localhost:8080/ > /dev/null || exit 1

# --- FINE MODIFICHE PER LA SICUREZZA ---

EXPOSE 8080

CMD ["catalina.sh", "run"]