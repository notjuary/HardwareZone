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
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/ecommerce.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]