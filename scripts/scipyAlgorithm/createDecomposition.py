import numpy as np
from scipy.cluster import hierarchy
from sklearn import metrics
import json


def createDecomposition(codebasesPath, codebaseName, strategyFolder, strategyName, graphName, matrixFile, cutType, cutValue):

    with open(codebasesPath + codebaseName + "/" + strategyFolder + "/" + strategyName + "/similarityMatrices/" + matrixFile + ".json") as f:
        similarityMatrix = json.load(f)

    entities = similarityMatrix["entities"]
    linkageType = similarityMatrix["linkageType"]
    matrix = np.array(similarityMatrix["matrix"])

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)

    if cutType == "H":
        cut = hierarchy.cut_tree(hierarc, height=cutValue)
    elif cutType == "N":
        cut = hierarchy.cut_tree(hierarc, n_clusters=cutValue)

    clusters = {}
    for i in range(len(cut)):
        if str(cut[i][0]) in clusters.keys():
            clusters[str(cut[i][0])] += [entities[i]]
        else:
            clusters[str(cut[i][0])] = [entities[i]]

    nodes = hierarchy.fcluster(hierarc, len(clusters), criterion="maxclust")
    try:
        silhouetteScore = metrics.silhouette_score(matrix, nodes)
    except:
        silhouetteScore = 0

    clustersJSON = {"silhouetteScore": "{0:.2f}".format(silhouetteScore), "clusters": clusters}

    with open(codebasesPath + codebaseName + "/" + strategyFolder + "/" + strategyName + "/decompositions/" + graphName + "/clusters.json", 'w') as outfile:
        outfile.write(json.dumps(clustersJSON, indent=4))
