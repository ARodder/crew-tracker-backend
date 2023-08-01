FROM ubuntu:22.04
WORKDIR /app
COPY . .
RUN apt-get update && apt install -y openjdk-17-jdk && apt install -y openjdk-17-jre
RUN apt-get install -y maven
RUN mvn clean package -DskipTests

ARG JAR_FILE=/app/target/*.jar
RUN mv ${JAR_FILE} /app/crew-tracker.jar


EXPOSE 8080

CMD ["java", "-jar", "crew-tracker.jar"]


