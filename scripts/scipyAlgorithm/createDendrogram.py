import numpy as np
from scipy.cluster import hierarchy
import json
import matplotlib
matplotlib.use('agg')
import matplotlib.pyplot as plt


def createDendrogram(codebasesPath, codebaseName, dendrogramName):
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

    fig = plt.figure(figsize=(25, 10))

    hierarchy.dendrogram(hierarc, labels=entities, distance_sort='descending')
    plt.savefig(codebasesPath + codebaseName + "/" + dendrogramName + "/dendrogramImage.png", format="png",
                bbox_inches='tight')
