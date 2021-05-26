FROM maven:3-jdk-11 as maven

COPY backend/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY backend/src ./src
RUN mvn -Dmaven.test.skip=true package


FROM openjdk:slim as java-python
COPY --from=python:3.7.4 / /

RUN python -m pip install --upgrade pip
RUN pip install numpy==1.17.2 scipy==1.6.0 matplotlib==3.0.3 scikit-learn==0.24.0

COPY scripts/createDendrogram.py ./scripts/
COPY scripts/cutDendrogram.py ./scripts/
COPY scripts/analyser.py ./scripts/

COPY --from=maven target/mono2micro-0.0.1-SNAPSHOT.jar ./

CMD ["java","-Djava.security.egd=file:/dev/./urandom","-jar", "./mono2micro-0.0.1-SNAPSHOT.jar"]