FROM maven:3.9.4-eclipse-temurin-20 as BUILD

COPY . /usr/src/app
RUN mvn --batch-mode -f /usr/src/app/pom.xml clean package

FROM azul/zulu-openjdk:20-latest
ENV PORT 8080
EXPOSE 8080
COPY --from=BUILD /usr/src/app/target /opt/target
RUN rm -f /etc/localtime && ln -sv /usr/share/zoneinfo/Europe/Oslo /etc/localtime && echo "Europe/Oslo" > /etc/timezone
WORKDIR /opt/target

CMD ["/bin/bash", "-c", "find -type f -name '*-SNAPSHOT.jar' | xargs java -jar"]
