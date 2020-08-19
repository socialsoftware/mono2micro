import os
from os import walk
import pandas as pd
import csv
import shutil

# Create .CSV files from analyser results
# numberOfEntities_MaxComplexity_CodebaseName.csv

codebases = []
for (dirpath, dirnames, filenames) in walk("../codebases"):
    codebases.extend(dirnames)
    break


dataFolderPath = os.getcwd() + "/data/"
if not os.path.exists(dataFolderPath):
    os.mkdir(dataFolderPath)
else:
    shutil.rmtree(dataFolderPath)
    os.mkdir(dataFolderPath)

for folder in codebases:
    analyserPath = os.getcwd() + "/../codebases/" + folder + "/analyser/analyserResult.json"
    similarityMatrixPath = os.getcwd() + "/../codebases/" + folder + "/analyser/similarityMatrix.json"

    if os.path.exists(analyserPath):
        print(folder)

        dfSimilarity = pd.read_json(similarityMatrixPath)
        entitiesCount = len(dfSimilarity['entities'])

        dfAnalyser = pd.read_json(analyserPath)

        maxComplexity = 0
        for entry in dfAnalyser:
            n = dfAnalyser[entry]['numberClusters']
            if n == entitiesCount:
                maxComplexity = dfAnalyser[entry]['complexity']

        csvFilePath = os.getcwd() + "/data/" + str(entitiesCount) + "_" + str(maxComplexity) + "_" + folder + ".csv"
        analyser_data_file = open(csvFilePath, 'w')
        csv_writer = csv.writer(analyser_data_file)

        csv_writer.writerow(['n', 'A', 'W', 'R', 'S', 'cohesion', 'coupling', 'complexity', 'pComplexity'])
        for entry in dfAnalyser:
            row = dfAnalyser[entry]

            if row['numberClusters'] == entitiesCount:
                continue

            pComplexity = 0
            if maxComplexity != 0:
                pComplexity = row['complexity'] / maxComplexity

            csv_writer.writerow(
                [
                    row['numberClusters'],
                    row['accessWeight'],
                    row['writeWeight'],
                    row['readWeight'],
                    row['sequenceWeight'],
                    row['cohesion'],
                    row['coupling'],
                    row['complexity'],
                    pComplexity
                ]
            )

        analyser_data_file.close()
