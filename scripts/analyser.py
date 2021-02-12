import numpy as np
from scipy.cluster import hierarchy
import sys
import json


codebasesPath = str(sys.argv[1])
codebaseName = str(sys.argv[2])

interval = 10
multiplier = 10
minClusters = 3
maxClusters = 21

with open(codebasesPath + codebaseName + "/analyser/similarityMatrix.json") as f:
    similarityMatrix = json.load(f)

entities = similarityMatrix["entities"]
linkageType = similarityMatrix["linkageType"]



def createCut(a,w,r,s,n):
    name = ','.join(map(str,[a,w,r,s,n]))

    with open(codebasesPath + codebaseName + "/analyser/similarityMatrix.json") as f:
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

    with open(codebasesPath + codebaseName + "/analyser/cuts/" + name + ".json", 'w') as outfile:
        outfile.write(json.dumps(clustersJSON, indent=4))


def sendRequest(a,w,r,s):
    a *= multiplier
    w *= multiplier
    r *= multiplier
    s *= multiplier

    for n in range(minClusters,maxClusters):
        createCut(a,w,r,s,n)

try:
    for a in range(interval,-1,-1):
        remainder = interval - a
        if remainder == 0:
            sendRequest(a, 0, 0, 0)
        else:
            for w in range(remainder,-1,-1): 
                remainder2 = remainder - w
                if remainder2 == 0:
                    sendRequest(a, w, 0, 0)
                else:
                    for r in range(remainder2,-1,-1):
                        remainder3 = remainder2 - r
                        if remainder3 == 0:
                            sendRequest(a, w, r, 0)
                        else:
                            sendRequest(a, w, r, remainder3)
except Exception as e:
    print(e)
