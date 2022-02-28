import numpy as np
from scipy.cluster import hierarchy
import json
from os import path

interval = 10
multiplier = 10
minClusters = 3
clusterStep = 1
maxClusters = -1


def analyser(codebasesPath, codebaseName, totalNumberOfEntities):
    global maxClusters

    if 3 < totalNumberOfEntities < 10:
        maxClusters = 3
    elif 10 <= totalNumberOfEntities < 20:
        maxClusters = 5
    elif 20 <= totalNumberOfEntities:
        maxClusters = 10
    else:
        raise Exception("Number of entities is too small (less than 4)")

    codebase = codebasesPath + codebaseName

    with open(codebase + "/analyser/similarityMatrix.json") as f:
        similarityMatrix = json.load(f)

    entities = similarityMatrix["entities"]
    linkageType = similarityMatrix["linkageType"]

    try:
        for a in range(interval, -1, -1):
            remainder = interval - a
            if remainder == 0:
                sendRequest(a, 0, 0, 0, False, totalNumberOfEntities, codebase, entities, linkageType)
            else:
                for w in range(remainder, -1, -1):
                    remainder2 = remainder - w
                    if remainder2 == 0:
                        sendRequest(a, w, 0, 0, False, totalNumberOfEntities, codebase, entities, linkageType)
                    else:
                        for r in range(remainder2, -1, -1):
                            remainder3 = remainder2 - r
                            if remainder3 == 0:
                                sendRequest(a, w, r, 0, False, totalNumberOfEntities, codebase, entities, linkageType)
                            else:
                                sendRequest(a, w, r, remainder3, False, totalNumberOfEntities, codebase, entities, linkageType)

        # last request to discover max Complexity possible (each cluster is singleton)
        sendRequest(10, 0, 0, 0, True, totalNumberOfEntities, codebase, entities, linkageType)
    except Exception as e:
        print(e)


def sendRequest(a, w, r, s, maxClusterCut, totalNumberOfEntities, codebase, entities, linkageType):
    a *= multiplier
    w *= multiplier
    r *= multiplier
    s *= multiplier

    if maxClusterCut:
        createCut(a, w, r, s, totalNumberOfEntities, codebase, entities, linkageType)
    else:
        for n in range(minClusters, maxClusters + 1, clusterStep):
            createCut(a, w, r, s, n, codebase, entities, linkageType)


def createCut(a, w, r, s, n, codebase, entities, linkageType):
    name = ','.join(map(str, [a, w, r, s, n]))

    filePath = codebase + "/analyser/cuts/" + name + ".json"

    if (path.exists(filePath)):
        return

    with open(codebase + "/analyser/similarityMatrix.json") as f:
        similarityMatrix = json.load(f)

    matrix = similarityMatrix["matrix"]
    for i in range(len(matrix)):
        for j in range(len(matrix)):
            matrix[i][j] = matrix[i][j][0] * a / 100 + \
                           matrix[i][j][1] * w / 100 + \
                           matrix[i][j][2] * r / 100 + \
                           matrix[i][j][3] * s / 100

    matrix = np.array(matrix)

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)

    cut = hierarchy.cut_tree(hierarc, n_clusters=n)

    clusters = {}
    for i in range(len(cut)):
        if str(cut[i][0]) in clusters.keys():
            clusters[str(cut[i][0])] += [entities[i]]
        else:
            clusters[str(cut[i][0])] = [entities[i]]

    clustersJSON = {}
    clustersJSON["clusters"] = clusters

    with open(filePath, 'w') as outfile:
        outfile.write(json.dumps(clustersJSON, indent=4))
