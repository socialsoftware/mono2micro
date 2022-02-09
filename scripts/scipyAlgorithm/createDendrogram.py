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
    matrix = np.array(similarityMatrix["matrix"])
    linkageType = similarityMatrix["linkageType"]

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)

    fig = plt.figure(figsize=(25, 10))

    hierarchy.dendrogram(hierarc, labels=entities, distance_sort='descending')
    plt.savefig(codebasesPath + codebaseName + "/" + dendrogramName + "/dendrogramImage.png", format="png",
                bbox_inches='tight')
