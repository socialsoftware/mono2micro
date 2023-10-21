FROM maven:3-jdk-11

COPY backend/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY backend/src ./src
RUN mvn -Dmaven.test.skip=true package

CMD ["java", "-server", "-Djava.security.egd=file:/dev/./urandom","-jar", "./target/mono2micro-0.0.1-SNAPSHOT.jar"]
