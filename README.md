# Mono2Micro

Tools to migrate a monolith to a microservices architecture, focusing on microservices identification, where the set of identified microservices minimizes the number of system transactions (microservices) associated with a business transaction, aiming to control introduction of relaxed consistency in the system.

Currently implemented for Spring-Boot monoliths that use FenixFramework and Spring Data ORMs.

### Pre-Requisites

- java 8+     (```java --version```)
- nodejs 10+  (```node --version```)
- npm 6+      (```npm --version```)
- python 3.5+   (```python --version```)
- (Non docker) run: ```pip install -r backend/src/main/resources/requirements.txt```
- Create the file ```specific.properties``` in backend/src/main/resources with the correct python command (example in file ```specific.properties.example```)

## Run Collectors

	cd collectors/
	see README.md for each collector

## Run Decomposition Generator

    docker-compose build
    docker-compose up

OR

    docker-compose build --no-cache
    docker-compose up --build

### Accessing

The frontend can be accessed in <http://localhost:3000> and the mongoDB contents in <http://localhost:8081>

## Experimentation Data
- The monolith codebases used by the spoon-callgraph and commit-collection Collectors are available [here](https://drive.google.com/drive/folders/1QiAPyM4ezhihoqJdSEJNmwfNDNzzsYv1?usp=share_link). 
- The monolith representations, Access and Repository Based, for some of the codebases are available [here](https://drive.google.com/drive/folders/1X1RHtWwLlJvc-i6q1fmXK_xrfD9OjuRb?usp=share_link).

## Publications Data
- [ECSA2019](https://doi.org/10.1007/978-3-030-29983-5_3): [Collector Tool](https://github.com/socialsoftware/mono2micro/tree/master/collectors/java-callgraph)
- [ICSA2020](https://doi.org/10.1109/ICSA47634.2020.00024): [Replication Package](https://github.com/socialsoftware/mono2micro/tree/master/data/icsa2020)
- [ECSA2020](https://doi.org/10.1007/978-3-030-58923-3_3): [Evaluation Data](https://github.com/socialsoftware/mono2micro/tree/master/data/ecsa2020/evaluation)

## Evaluation package using Spoon and Commits collectors

Looking for a reproducible evaluation package? It's available [here](https://github.com/socialsoftware/mono2micro/tree/master/data/commit).

## License
This project is licensed under the MIT License - see the [LICENSE](https://github.com/socialsoftware/mono2micro/blob/master/LICENSE) file for details.
