FROM openjdk:27-ea-oracle

WORKDIR /app

COPY target/urlShortenerServer-0.0.1-SNAPSHOT.jar urlShortenerServer-0.0.1-SNAPSHOT.jar

#EXPOSE 8080

ENTRYPOINT ["java", "-jar", "urlShortenerServer-0.0.1-SNAPSHOT.jar"]

