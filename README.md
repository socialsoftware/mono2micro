# Mono2Micro

Tools to migrate a monolith to a microservices architecture, focusing on microservices identification, where the set of identified microservices minimizes the number of system transactions (microservices) associated with a business transaction, aiming to control introduction of relaxed consistency in the system.

Currently implemented for systems that use the FénixFramework ORM and Spring-Boot, but can be generalized for any monolith technology.

### Pre-Requisites

- java 8+     (java --version)
- nodejs 10+  (node --version)
- npm 6+      (npm --version)
- python 3.5+   (python --version)
- Run: pip install -r backend/src/main/resources/requirements.txt

### Run

To run the backend:
	
	cd backend/
	mvn clean install -DskipTests
	mvn spring-boot:run

To run the frontend:
	
	cd frontend/
	npm install
	npm start

### Demo Instructions - ICSA2020

- Start backend and frontend
- Open http://localhost:3000/
- Under the 'Codebases' tab, create a codebase using a local file from data/icsa2020/collection/plugin-eclipse
- Proceed to create a dendrogram
- Cut the dendrogram, using number of clusters = 5.
- Import an expert decomposition using a local file from data/icsa2020/expert
- Under the 'Microservice Analysis' tab, run an analysis, between the generated cut and the expert one.
- Under the 'Analyser' tab, import an analyser result using a local file from data/icsa2020/evaluation. Observe the evaluation results.
