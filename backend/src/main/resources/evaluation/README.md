To run the analysis scripts

1. Load the codebases through the Mono2Micro frontend. They will be stored in `backend/src/main/resources/codebases
2. For each codebase, generate decompositions using all combinations of similarity measures and number of clusters using 
   the analyser feature in the Mono2Micro frontend. It creates a directory analyser with the produced information below `backend/src/main/resources/codebases/CODEBASE_ONE/`
3. Execute `python 0_MetadataCreator.py` to process all the generated decompositions. It generates a csv file for each codebase below `backend/src/main/resources/evaluation/data`
4. Execute the python scripts for each one of the analysis
5. For the python scripts that use the MojoFM algorithm, (`7_dendogram_evolution_analysis_MoJoFM_BeforeIncrementingN.py`,
`7_dendogram_evolution_analysis_MoJoFM_TransformNintoNPlus1.py` and `9_static_vs_dynamic_bestDecompositions.py`) it is 
   necessary that it the script in `backend/src/main/java/pt/ist/socialsoftware/mono2micro/utils/mojoCalculator/` is running,
   therefore, execute the commmnd `mvn exec:java`