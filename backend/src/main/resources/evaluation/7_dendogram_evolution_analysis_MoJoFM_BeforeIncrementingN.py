import json
from os import walk
import numpy as np
import pandas as pd
import plotly.express as px
from py4j.java_gateway import JavaGateway
import itertools
DISTR_SRC_FILE_PATH = './mojoCalculator/src/main/resources/distrSrc.rsf'
DISTR_TARGET_FILE_PATH = './mojoCalculator/src/main/resources/distrTarget.rsf'

# Warning:
# Run MoJo Calculator Java code before running this script
# More info on ./mojoCalculator/ folder

# Calculates how many changes have to be made in the best decomposition
# of N = n, so that the best decomposition of N = n + 1 can be obtained
# incrementally from the first.

data_dict = {
    'transition': [],
    'mojoFM': [],
    'hoverText': [],
    'entityCount': []
}


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

    with open('../codebases/' +
              parsedFileName + '/analyser/cuts/' + cutName) as f:
        dataFile = json.load(f)
        return dataFile['clusters']


# each entry in the list 'clustersForN' corresponds to a list of clusters of a specific decomposition
# calculates the MoJoFM result between a decomposition[n-1] and each possible decomposition
# with n-1 clusters given a decomposition with n clusters, i.e., each parent decomposition of a
# decomposition with n clusters so that this one can be obtained incrementally from the parent
def calculateTransitionMoJos(clustersForN):
    for i in range(1, len(clustersForN)):
        clustersN = clustersForN[i]
        clustersNLess1 = clustersForN[i - 1]
        clustersNLess1FromN_Comb = []  # matrix [combinationN][ClustersList]
        numOfClustersN = len(clustersN)
        lst1 = list(range(numOfClustersN))
        # possible "parent decompositions" of a a system with n clusters
        combinationsList = list(itertools.combinations(lst1, 2))

        for comb in combinationsList:
            possibleCombinations = []
            clusterMerge1Index = comb[0]
            clusterMerge2Index = comb[1]
            remainingIndexes = list(range(numOfClustersN))
            remainingIndexes.remove(clusterMerge1Index)
            remainingIndexes.remove(clusterMerge2Index)
            possibleCombinations.append(clustersN[str(clusterMerge1Index)] + clustersN[str(clusterMerge2Index)])

            for j in remainingIndexes:
                possibleCombinations.append(clustersN[str(j)])

            clustersNLess1FromN_Comb.append(possibleCombinations)

        distrSrc = ""

        entityCount1 = 0
        for clusterKey in clustersNLess1.keys():
            for entity in clustersNLess1[clusterKey]:
                entityCount1 += 1
                distrSrc += "contain " + clusterKey + " " + entity + "\n"

        text_file = open(DISTR_SRC_FILE_PATH, "w+")
        text_file.write(distrSrc)
        text_file.close()

        maxResult = 0  # MojoResult = 100, both decompositions are equals

        # clusters aggregate = one possible set of clusters of the previous n given the actual set of clusters
        for clustersAggregate in clustersNLess1FromN_Comb:
            distrTarget = ""
            for j in range(0, len(clustersAggregate)):
                clusterI = clustersAggregate[j]
                for entity in clusterI:
                    distrTarget += "contain " + str(j) + " " + entity + "\n"

            # possible decomposition obtained
            # Calculate de MojoFM between src and target
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

            if result > maxResult:
                maxResult = result

                if result == 100:
                    break

        transitionString = str(numOfClustersN - 1) + '->' + str(numOfClustersN)
        data_dict['mojoFM'].append(maxResult)
        data_dict['entityCount'].append(entityCount1)
        data_dict['transition'].append(transitionString)
        data_dict['hoverText'].append(file)


files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break

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

        minComplexityClusters.append(getClusters(minComplexityWeights))

    if len(minComplexityClusters) <= 1:  # n=3 only
        continue

    calculateTransitionMoJos(minComplexityClusters)

data_dict = pd.DataFrame(data_dict)

# box plot style
boxFig = px.box(
    data_dict,
    x="transition",
    y="mojoFM",
    hover_name='hoverText',
    title='Transition From N to N of the next N+1 decomposition',
    points='all',
    # range_y=[0, 100]
)
boxFig.update_traces(marker=dict(size=2))
boxFig.show()

entityCountGE80 = []
entityCountLT80 = []

for mojo, entityCount in zip(data_dict['mojoFM'], data_dict['entityCount']):
    if mojo >= 80:
        entityCountGE80.append(entityCount)
    else:
        entityCountLT80.append(entityCount)

print('mean GE 80: ' + str(np.mean(entityCountGE80)))
print('std GE 80: ' + str(np.std(entityCountGE80)))
print('mean LT 80: ' + str(np.mean(entityCountLT80)))
print('std LT 80: ' + str(np.std(entityCountLT80)))

print()
print("100 > mojoFM >= 90:")
print(data_dict[(data_dict['mojoFM'] >= 90)].count() / data_dict[:].count())
print()
print("90 > mojoFM >= 80:")
print(data_dict[(data_dict['mojoFM'] < 90) & (data_dict['mojoFM'] >= 80)].count() / data_dict[:].count())
print()
print("80 > mojoFM:")
print(data_dict[(data_dict['mojoFM'] < 80)].count() / data_dict[:].count())
