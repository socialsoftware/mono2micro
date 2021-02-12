# The data replication package of the International Conference on Software Architecture (ICSA 2020)

## Repository code
- https://github.com/socialsoftware/mono2micro
- branch icsa2020
- `git clone https://github.com/socialsoftware/mono2micro.git`
- `git checkout icsa2020`

## Locate files in the project
- `data/icsa2020/collection/plugin-eclipse` - contains the collected data using Eclipse JDT static analysis for each of 
  the three systems. To obtain fresh data see [collectors/eclipse-plugin-callgraph/README.md](https://github.com/socialsoftware/mono2micro/blob/master/collectors/eclipse-plugin-callgraph/README.md) 
  on how to run the Eclipse JDT plugin data collector on each one of the monolith codebases.
- `data/icsa2020/expert` - contains expert cuts for two of the monoliths.
- `data/icsa2020/evaluation` - contains the results of a simulation of combinations of similarity measures and cluster cuts

## Replication process
Three ways to replicate the results in the paper:
- **Full Replication**: Do all steps from the static analysis of the monolith systems to the analysis of the combinations 
  of similarity measures and cuts. It allows to replicate the complete process.
- **Analysis Replication**: In the first step of **(A)** use the pre-generated files in `data/icsa2020/collection/plugin-eclipse` 
  to skip the static analysis and avoid installing and running Eclipse Plugin. It allows to replicate the complete analysis.
- **Evaluation Replication**: Use the pre-generated files in `data/icsa2020/evaluation` to skip the generation of dendrograms 
  and of all the combinations of similarity measures and cluster cuts. Do the first and last step of **(D)**. 
  It allows to replicate the evaluation in the paper, except Table II.

## Start the application
### Pre-Requisites
- Install Docker and Docker-Compose  
- python 3.5+   (```python --version```)
- Create the file ```specific.properties``` in `./backend/src/main/resources` with the line `python=python3`
- Create the directory ```codebases``` in `.` 
### Run
- `docker-compose build`
- `docker-compose up`
- Open `http://localhost:3001/` in a browser

## Analysis

### (A) Generate a dendrogram
- Under the `Codebases` tab, create a codebase using a pre-generated local file from `data/icsa2020/collection/plugin-eclipse`
  or the files generated using the Eclipse Plugin
- `Go to Codebase` -> `Go to Dendrogram`
- Fill the form and `Create Dendrogram` (in `Controller Profiles` select `Generic`)
- `Go to Dendrogram` 
  
### (B) Analyse a decomposition
- The dendrogram is generated (**do (A) steps**)
- Cut the dendrogram, giving the number of clusters using the `Cut Dendrogram` form. A decomposition is 
  generated and the metrics calculated
- `Go to Graph` to visualize and change the decomposition

### (C) Compare generated decomposition with expert decomposition
- Given a generated decomposition, **do (A) steps and the first and second steps of (B)**
- Import an expert decomposition using the `Create Expert Cut` form and a file from `data/icsa2020/expert`
- To compare with expert decomposition, under the `Microservice Analysis` tab, run an analysis, between the generated 
  cut and the expert one.

### (D) Analyse all the decompositions
- The codebase is created, **do first step of (A)**
- Under the `Analyser` tab: `Select Codebase`, `Controller Profiles` -> `Generic`, `Request Limit = 0`, `Submit`
- Wait until all decompositions are generated (it takes several minutes... observe the Spring-Boot log)
- Under the 'Analyser' tab, to observe the evaluation results, `Import Analyser Results` using the file generated that
  is in `./codebases/codebase/analyser/analyserResult.json`, or pre-generated file from
  `data/icsa2020/evaluation`

## Data in paper
- All figures and tables produced from data generated doing **(D)**
- The python scripts in `data/icsa2020/evaluation/tables-and-graphics` can be used to extract the data from the analyser
results json file (a concrete example is given for each case):
  - `python generateAnalyserCSV.py ../bwResults.json` extracts the data in bwResults.json to a csv format (to be used in 
    Tables III, IV and V)
  - `python generate-cohesion-and-coupling-relations.py ../ldodResults.json 5 coupling` extracts pairs complexity/coupling
  for all the decompositions of LdoD with 5 clusters (to be used in Figures 1 and 2)
  - `python generate-graph-complexities.py ../fenixResults.json 20` extracts the complexities for the decompositions of
  FenixEdu with 20 clusters (to be used in Figure 3)
  - `python generate-controller-complexities.py ../bwResults.json 0,0,0,100,10` extracts the complexities of controllers
  for Blended Workflow decomposition generated with similarities (0 access, 0 write, 0 read, 100 sequence) and 10 clusters (to 
  be used in Figure 4)
  - `python generate-graph-outliers.py ../ldodResults.json 5 7` extracts the 7 complexity outliers for LdoD 
  decompositions with 5 clusters (to be used in Table I)
  - `python generate-controller-outliers.py ../ldodResults.json 30,30,40,0,5 5` extracts the 5 controllers with higher 
  complexity for LdoD decomposition generated with similarities (30 access, 30 write, 40 read, 0 sequence) and 5 clusters
  (to be used in Table II)
- The folders below `data/icsa2020/evaluation/tables-and-graphics/` contain the data used in the paper, and in 
  [https://www.overleaf.com/read/cprvyddngwyf](https://www.overleaf.com/read/cprvyddngwyf) the latex file that generates
  the paper's figures using that data files
  

