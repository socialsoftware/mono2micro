import json
from os import walk
import numpy as np
import pandas as pd
import plotly.express as px
from py4j.java_gateway import JavaGateway

# Run MoJo Calculator Java code before running this script
# More info on ./mojoCalculator/ folder

DISTR_SRC_FILE_PATH = './mojoCalculator/src/main/resources/distrSrc.rsf'
DISTR_TARGET_FILE_PATH = './mojoCalculator/src/main/resources/distrTarget.rsf'

data_dict = {
    'transition': [],
    'mojoFM': [],
    'hoverText': [],
    'entityCount': [],
    # 'complexityDiff': []
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

    with open('/home/samuel/ProjetoTese/mono2micro/backend/src/main/resources/codebases/' +
              parsedFileName + '/analyser/cuts/' + cutName) as f:
        dataFile = json.load(f)
        return dataFile['clusters']


# each entry in the list 'clustersForN' corresponds to a list of clusters of a specific decomposition
# minComplexityClusters -> the decomposition with less complexity for each N
# 100A -> the decomposition with 100 weight on Access Metric for each
# calculates the MoJoFM result between a decomposition[n-1] and each possible decomposition with n-1 clusters
# given a decomposition with n clusters
def calculateTransitionMoJos(clustersForN):
    for i in range(1, len(clustersForN)):
        clustersN = clustersForN[i]
        clustersNLess1 = clustersForN[i - 1]
        numOfClustersN = len(clustersN)

        distrSrc = ""

        entityCount1 = 0
        for clusterKey in clustersNLess1.keys():
            for entity in clustersNLess1[clusterKey]:
                entityCount1 += 1
                distrSrc += "contain " + clusterKey + " " + entity + "\n"

        text_file = open(DISTR_SRC_FILE_PATH, "w+")
        text_file.write(distrSrc)
        text_file.close()

        distrTarget = ""
        for clusterKey in clustersN.keys():
            for entity in clustersN[clusterKey]:
                distrTarget += "contain " + clusterKey + " " + entity + "\n"

        text_file = open(DISTR_TARGET_FILE_PATH, "w+")
        text_file.write(distrTarget)
        text_file.close()

        # run Java to calculate MoJoFM
        gateway = JavaGateway()
        result = gateway.entry_point.runMoJo()

        transitionString = str(numOfClustersN - 1) + '->' + str(numOfClustersN)
        data_dict['mojoFM'].append(result)
        data_dict['entityCount'].append(entityCount1)
        data_dict['transition'].append(transitionString)
        data_dict['hoverText'].append(file)

        # diff = complexities[str(numOfClustersN)] - complexities[str(numOfClustersN-1)]
        # data_dict['complexityDiff'].append(diff)


files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break

for file in files:
    print(file)

    data = pd.read_csv("./data/" + file)

    minComplexityClusters = []

    # complexities = {}  # key = n + metricTag; value = complexity for that N

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
        # complexities[str(n)] = minComplexity

    if len(minComplexityClusters) <= 1:  # n=3 only
        continue

    calculateTransitionMoJos(minComplexityClusters)

# box plot style
boxFig = px.box(data_dict,
                x="transition",
                y="mojoFM",
                hover_name='hoverText',
                title='Transition From N to N+1',
                points='all',
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


# fig1 = px.scatter(
#     data_dict,
#     x='mojoFM',
#     y='complexityDiff',
#     hover_name='hoverText',
#     title="mojoFM vs complexityDiff",
#     labels={"x": "mojoFM", "y": "complexityDiff"},
#     range_x=[-1, 101],
#     range_y=[-0.1, 1.1],
#     trendline='ols',
# )
# fig1.show()
