# Mono2Micro

Tools to migrate a monolith to a microservices architecture, focusing on microservices identification, where the set of identified microservices minimizes the number of system transactions (microservices) associated with a business transaction, aiming to control introduction of relaxed consistency in the system.

Currently implemented for systems that use the FénixFramework ORM and Spring-Boot, but can be generalized for any monolith technology.

### Pre-Requisites

- java 8+
- nodejs 10+
- npm 6+
- python 3+
- Install the following python libraries: numpy, scipy, matplotlib, sklearn

### Run

To run the backend:
	
	cd backend/
	Create the file specific.properties in src/main/resources with the correct python command (example in specific.properties.example).
	mvn spring-boot:run

To run the frontend:
	
	cd frontend/
	npm install
	npm start
