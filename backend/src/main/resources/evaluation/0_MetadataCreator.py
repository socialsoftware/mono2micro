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

    # if 'ldod' not in folder.lower() and "bw" not in folder:
    #     continue
    analyserFolderPath = os.getcwd() + "/../codebases/" + folder + "/analyser/"
    analyserResultFilePath = analyserFolderPath + "analyserResult.json"
    similarityMatrixFilePath = analyserFolderPath + "similarityMatrix.json"

    if os.path.exists(analyserResultFilePath):
        print(folder)

        dfAnalyser = pd.read_json(analyserResultFilePath)
        dfSimilarity = pd.read_json(similarityMatrixFilePath)

        entitiesCount = len(dfSimilarity['entities'])

        maxComplexity = 0
        maxPerformance = 0
        for entry in dfAnalyser:
            n = dfAnalyser[entry]['numberClusters']

            if n == entitiesCount:
                maxComplexity = dfAnalyser[entry]['complexity']

            if n != entitiesCount and dfAnalyser[entry]['performance'] > maxPerformance:
                maxPerformance = dfAnalyser[entry]['performance']

        csvFilePath = os.getcwd() + "/data/" + str(entitiesCount) + "_" + str(maxComplexity) + "_" + folder + ".csv"
        csv_file = open(csvFilePath, 'w')
        csv_writer = csv.writer(csv_file)

        csv_writer.writerow([
            'n', 'A', 'W', 'R', 'S', 'cohesion', 'coupling', 'complexity', 'pComplexity', 'performance', 'pPerformance'
        ])

        for entry in dfAnalyser:
            row = dfAnalyser[entry]

            if row['numberClusters'] == entitiesCount:
                continue

            pComplexity = 0
            if maxComplexity != 0:
                pComplexity = row['complexity'] / maxComplexity

            pPerformance = 0
            if maxPerformance != 0:
                pPerformance = row['performance'] / maxPerformance

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
                    pComplexity,
                    row['performance'],
                    pPerformance
                ]
            )

        csv_file.close()
