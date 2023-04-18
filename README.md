# Mono2Micro

Tools to migrate a monolith to a microservices architecture, focusing on microservices identification, where the set of identified microservices minimizes the number of system transactions (microservices) associated with a business transaction, aiming to control introduction of relaxed consistency in the system.

Currently implemented for Spring-Boot monoliths that use FenixFramework and Spring Data ORMs.

# Architecture

## 1. Collectors

 - The collectors are responsible for collecting all the necessary data from a given codebase, either with static or dynamic analysis.

## 2. Spring Boot Server (Backend)

 - The Spring Boot Server is responsible for managing all codebase information, evaluating decompositions, and recommend decompositions by generating all possible combinations to find the best parameters for each decomposition strategy.

## 3. Fast API Server (Scripts)

 - The Fast API Server is responsible for the clustering operations, and also to expose the Code2Vec model.

## 4. User Inteface (Frontend)

 - The User Interface allows the user to create codebases by submitting the collectors data to the Spring Boot Server, and create and evaluate several decompositions.

## 5. Evaluation Playground

 - The Evaluation Playground is used to evaluate the strategies results data in order to generate graphics and statistic reports.

# Setup

### Pre-Requisites

- java 8+     (```java --version```)
- nodejs 10+  (```node --version```)
- npm 6+      (```npm --version```)
- python 3.5+   (```python --version```)
- pipenv 2022.6.7 (```pipenv --version```)

### Spring Boot Server Setup
- Create the file ```specific.properties``` in backend/src/main/resources with the correct python command (example in file ```specific.properties.example```)
- Due to a limitation in SpringBoot documented in https://github.com/spring-projects/spring-boot/issues/2895, .jar files cannot be generated with more than 655535 total files inside. Due to this, make sure the folder ```/codebases``` is empty before building the project.

### Fast API Server Setup

	cd scripts/
	mkdir models
	cd models
	wget https://code2vec.s3.amazonaws.com/model/java-large-released-model.tar.gz
	tar -xf java-large-released-model.tar.gz

## Run manually

### 1. To run the Collectors:

	cd collectors/
	see README.md for each collector

### 2. To run the Spring Boot Server:
	
	cd backend/
	mvn clean install -DskipTests
	java -Djava.security.egd=file:/dev/./urandom -jar ./target/mono2micro-0.0.1-SNAPSHOT.jar

    mvn -Dmaven.test.skip=true package # If there are ContextManager issues when refreshing the context
    java -jar ./target/mono2micro-0.0.1-SNAPSHOT.jar

### 3. To run the Fast API Server:
	
	cd scripts/
	pip install -r requirements.txt
    python main.py

### 4. To run the Frontend:

	cd frontend/
	npm install --legacy-peer-deps
	npm start

### 5. To run the Evaluation Playground:

	cd evaluation-playground/
	pipenv shell
	pipenv install # First time only
	python <script-file-name>.py

## Run using Docker

    docker-compose build
    docker-compose up

## Run using Docker, clean install

    docker-compose build --no-cache
    docker-compose up --build

## Accessing Web Service

The web service can be accessed in <http://localhost:3000> and the mongoDB contents in <http://localhost:8081>

## Experimentation Data
- The monolith codebases used by the spoon-callgraph, commit-collection and code2vec Collectors are available [here](https://drive.google.com/drive/folders/1QiAPyM4ezhihoqJdSEJNmwfNDNzzsYv1?usp=share_link).
- The monolith representations, Access and Repository Based, for some codebases are available [here](https://drive.google.com/drive/folders/1X1RHtWwLlJvc-i6q1fmXK_xrfD9OjuRb?usp=share_link).
- The monolith representations, Code Embeddings generated using code2vec, for some codebased are available [here](https://drive.google.com/drive/folders/1R4NU3QcTboAPuvMqp-_AMRmfftAmGgXt?usp=share_link).

## Evaluation package
Looking for a reproducible evaluation package? It's available [here](https://github.com/socialsoftware/mono2micro/tree/master/data/commit).

## Publications Data
- [ECSA2019](https://doi.org/10.1007/978-3-030-29983-5_3): [Collector Tool](https://github.com/socialsoftware/mono2micro/tree/master/collectors/java-callgraph)
- [ICSA2020](https://doi.org/10.1109/ICSA47634.2020.00024): [Replication Package](https://github.com/socialsoftware/mono2micro/tree/master/data/icsa2020)
- [ECSA2020](https://doi.org/10.1007/978-3-030-58923-3_3): [Evaluation Data](https://github.com/socialsoftware/mono2micro/tree/master/data/ecsa2020/evaluation)

## License
This project is licensed under the MIT License - see the [LICENSE](https://github.com/socialsoftware/mono2micro/blob/master/LICENSE) file for details.
