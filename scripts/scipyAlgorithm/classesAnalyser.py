import numpy as np
from scipy.cluster import hierarchy
import json
from os import path

minClusters = 3
clusterStep = 1
maxClusters = -1

def analyser(codebasesPath, codebaseName):
    global maxClusters

    with open(codebasesPath + codebaseName + "/classes_embeddings.json") as f:
        classes_embeddings = json.load(f)

    totalNumberOfEntities = classes_embeddings['numberOfEntities']

    if 3 < totalNumberOfEntities < 10:
        maxClusters = 3
    elif 10 <= totalNumberOfEntities < 20:
        maxClusters = 5
    elif 20 <= totalNumberOfEntities:
        maxClusters = 10
    else:
        raise Exception("Number of entities is too small (less than 4)")

    try:
        for clusterSize in range(minClusters, maxClusters + 1, clusterStep):
            createCut(codebasesPath, codebaseName, clusterSize)
            
    except Exception as e:
        print(e)


def createCut(codebasesPath, codebaseName, clusterSize):

    with open(codebasesPath + codebaseName + "/classes_embeddings.json") as f:
        classes_embeddings = json.load(f)

    linkageType = classes_embeddings['linkageType']
    name = ','.join(map(str, [linkageType, clusterSize]))

    filePath = codebasesPath + codebaseName + "/analyser/classes/cuts/" + name + ".json"

    if (path.exists(filePath)):
        return

    names = []
    ids = []
    vectors = []
    for cls in classes_embeddings['classes']:
        names += [cls['name']]
        vectors += [cls['codeVector']]
        if 'translationID' in cls.keys():
            ids += [cls['translationID']]
        else:
            ids += [-1]

    matrix = np.array(vectors)

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)
    cut = hierarchy.cut_tree(hierarc, n_clusters=clusterSize)

    clusters = {}
    for i in range(len(cut)):
        if str(cut[i][0]) in clusters.keys():
            if ids[i] != -1:
                clusters[str(cut[i][0])] += [ids[i]]
        else:
            if ids[i] != -1:
                clusters[str(cut[i][0])] = [ids[i]]
            else:
                clusters[str(cut[i][0])] = []

    numberOfEntitiesClusters = 0
    for k in clusters.keys():
        if len(clusters[k]) != 0:
            numberOfEntitiesClusters += 1

    clustersJSON = {"clusters": clusters, "numberOfEntitiesClusters": numberOfEntitiesClusters}

    with open(filePath, 'w') as outfile:
        outfile.write(json.dumps(clustersJSON, indent=4))

