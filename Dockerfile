FROM openjdk:17
ADD target/prime-java-gateway-0.0.1-SNAPSHOT.jar prime-java-gateway-0.0.1-SNAPSHOT.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "prime-java-gateway-0.0.1-SNAPSHOT.jar"]
