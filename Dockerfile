FROM kolov/java8

WORKDIR /app
COPY target/*-standalone.jar app.jar
COPY config/prod/wcig.edn .

EXPOSE 3000

ENTRYPOINT ["java", "-cp", "/app:/app/*", "wcig.run", ":prod", "3000", "mongo-w2f2", "27017"]