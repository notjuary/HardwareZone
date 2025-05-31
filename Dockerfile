# Usa un'immagine Tomcat ufficiale
FROM tomcat:10.1-jdk17

# Rimuove le app di default (come ROOT, examples, ecc.)
RUN rm -rf /usr/local/tomcat/webapps/*

# Copia il tuo file .war nella cartella webapps e lo rinomina ROOT.war
COPY target/ecommerce.war /usr/local/tomcat/webapps/ROOT.war

# Espone la porta 8080
EXPOSE 8080

# Comando di default
CMD ["catalina.sh", "run"]
