# Mono2Micro

Tools to migrate a monolith to a microservices architecture, focusing on microservices identification, where the set of identified microservices minimizes the number of system transactions (microservices) associated with a business transaction, aiming to control introduction of relaxed consistency in the system.

Currently implemented for systems that use the FénixFramework ORM and Spring-Boot, but can be generalized for any monolith technology.

### Pre-Requisites

- java 8+     (java --version)
- nodejs 10+  (node --version)
- npm 6+      (npm --version)
- python 3.5+   (python --version)
- Run: pip install -r backend/src/main/resources/requirements.txt
- Create the file specific.properties in backend/src/main/resources with the correct python command (example in specific.properties.example)

### Run

To run the backend:
	
	cd backend/
	mvn clean install -DskipTests
	mvn spring-boot:run

To run the frontend:
	
	cd frontend/
	npm install
	npm start
