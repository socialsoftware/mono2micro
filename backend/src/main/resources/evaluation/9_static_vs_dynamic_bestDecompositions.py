import json

import pandas as pd
from py4j.java_gateway import JavaGateway

DISTR_SRC_FILE_PATH = '../../java/pt/ist/socialsoftware/mono2micro/utils/mojoCalculator/src/main/resources/distrSrc.rsf'
DISTR_TARGET_FILE_PATH = '../../java/pt/ist/socialsoftware/mono2micro/utils/mojoCalculator/src/main/resources' \
                         '/distrTarget.rsf'


def calculateMoJoBetweenBestOfStaticAndBestOfDynamic():
    bestDecompositionsClustersForNFiles = []  # each position is a different file
    for key in bestDecompositionsOfFile.keys():
        bestDecompositionsClustersForNFiles.append(bestDecompositionsOfFile[key])

    dynamicEntities = []
    staticEntities = []
    firstFileDecompositions = bestDecompositionsClustersForNFiles[0]
    bestDecompositionForN = firstFileDecompositions[0]
    for clusterKey in bestDecompositionForN.keys():
        for entity in bestDecompositionForN[clusterKey]:
            dynamicEntities.append(entity)

    secondFileDecompositions = bestDecompositionsClustersForNFiles[1]
    bestDecompositionForN = secondFileDecompositions[0]
    for clusterKey in bestDecompositionForN.keys():
        for entity in bestDecompositionForN[clusterKey]:
            staticEntities.append(entity)

    for n in range(0, len(bestDecompositionsClustersForNFiles[0])):
        distrSrc = ""
        firstFileDecompositions = bestDecompositionsClustersForNFiles[0]
        bestDecompositionForN = firstFileDecompositions[n]
        for clusterKey in bestDecompositionForN.keys():
            for entity in bestDecompositionForN[clusterKey]:
                distrSrc += "contain " + clusterKey + " " + entity + "\n"

        text_file = open(DISTR_SRC_FILE_PATH, "w+")
        text_file.write(distrSrc)
        text_file.close()

        distrTarget = ""  # static
        secondFileDecompositions = bestDecompositionsClustersForNFiles[1]
        bestDecompositionForN = secondFileDecompositions[n]
        for clusterKey in bestDecompositionForN.keys():
            for entity in bestDecompositionForN[clusterKey]:
                distrTarget += "contain " + clusterKey + " " + entity + "\n"

        text_file = open(DISTR_TARGET_FILE_PATH, "w+")
        text_file.write(distrTarget)
        text_file.close()

        # run Java to calculate MoJoFM
        try:
            gateway = JavaGateway()
            result = gateway.entry_point.runMoJo()
        except Exception:
            print("Warning: Entry point for the MoJoFM calculator not running")
            raise SystemExit

        print("N = " + str(n + 3))
        print("MoJo = " + str(result))


def getClusters(complexityWeights):
    cutName = ",".join(
        [
            str(int(complexityWeights[0])),
            str(int(complexityWeights[1])),
            str(int(complexityWeights[2])),
            str(int(complexityWeights[3])),
            str(int(n))
        ]
    ) + ".json"

    with open('/home/samuel/ProjetoTese/mono2micro/backend/src/main/resources/codebases/' +
              parsedFileName + '/analyser/cuts/' + cutName) as f:
        dataFile = json.load(f)
        return dataFile['clusters']


# the two csv files two compare
files = [
    '58_3195.8_LdoDTest-2ndIteration.csv',
    '69_99631.32_ldod-maven.csv'
]
bestDecompositionsOfFile = {}

for file in files:
    print(file)

    data = pd.read_csv("./data/" + file)

    minComplexityClusters = []

    for n in range(3, 11):
        minComplexity = float("inf")
        minComplexityWeights = []  # a, w, r, s
        for entry in data.values:
            if entry[0] != n:
                continue

            if entry[8] < minComplexity:
                minComplexity = entry[8]
                minComplexityWeights = [entry[1], entry[2], entry[3], entry[4]]

        if minComplexity == float("inf"):  # no entries for this N
            continue

        parsedFileName = "_".join(file.split("_")[2:])
        parsedFileName = parsedFileName[0:len(parsedFileName) - 4]

        print('N = ' + str(n) + " " + str(minComplexityWeights))
        minComplexityClusters.append(getClusters(minComplexityWeights))

    bestDecompositionsOfFile[file] = minComplexityClusters

calculateMoJoBetweenBestOfStaticAndBestOfDynamic()