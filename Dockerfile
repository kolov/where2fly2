FROM java:8

WORKDIR /app
COPY target/*-standalone.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-cp", "/app:/app/*", "wcig.run", ":prod", "3000", "mongo", "27017"]