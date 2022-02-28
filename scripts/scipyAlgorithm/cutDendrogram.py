import numpy as np
from scipy.cluster import hierarchy
from sklearn import metrics
import json


def cutDendrogram(codebasesPath, codebaseName, dendrogramName, graphName, cutType, cutValue):

    with open(codebasesPath + codebaseName + "/" + dendrogramName + "/similarityMatrix.json") as f:
        similarityMatrix = json.load(f)

    entities = similarityMatrix["entities"]
    linkageType = similarityMatrix["linkageType"]

    a = similarityMatrix["access"]
    w = similarityMatrix["write"]
    r = similarityMatrix["read"]
    s = similarityMatrix["sequence"]

    matrix = similarityMatrix["matrix"]
    for i in range(len(matrix)):
        for j in range(len(matrix)):
            matrix[i][j] = matrix[i][j][0] * a / 100 + \
                           matrix[i][j][1] * w / 100 + \
                           matrix[i][j][2] * r / 100 + \
                           matrix[i][j][3] * s / 100

    matrix = np.array(matrix)

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

    with open(codebasesPath + codebaseName + "/" + dendrogramName + "/" + graphName + "/clusters.json", 'w') as outfile:
        outfile.write(json.dumps(clustersJSON, indent=4))
