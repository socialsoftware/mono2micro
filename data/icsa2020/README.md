# The data replication package of the International Conference on Software Architecture (ICSA 2020)

## Repository code
- tag icsa2020 - https://github.com/socialsoftware/mono2micro/releases/tag/icsa2020

## Locate files in the project
- data/icsa2020/collection/plugin-eclipse - contains the collected data using Eclipse JDT static analysys for each of the systems
- data/icsa2020/expert - contains expert cuts for each of the systems
- data/icsa2020/evaluation - contains the results of a simulation of combinations of similarity measures and cluster cuts

## Run the application
- Start backend and frontend
- Open http://localhost:3000/
- Under the 'Codebases' tab, create a codebase using a local file from data/icsa2020/collection/plugin-eclipse
- Proceed to create a dendrogram
- Cut the dendrogram, using number of clusters = 5.
- Import an expert decomposition using a local file from data/icsa2020/expert
- To compare with expert cut, under the 'Microservice Analysis' tab, run an analysis, between the generated cut and the expert one.
- To observe the evaluation results, under the 'Analyser' tab, import an analyser result using a local file from data/icsa2020/evaluation.
