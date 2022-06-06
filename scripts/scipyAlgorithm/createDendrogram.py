import numpy as np
from scipy.cluster import hierarchy
import json
import matplotlib
matplotlib.use('agg')
import matplotlib.pyplot as plt


def createDendrogram(codebasesPath, codebaseName, strategyName):
    with open(codebasesPath + codebaseName + "/strategies/" + strategyName + "/similarityMatrices/similarityMatrix.json") as f:
        similarityMatrix = json.load(f)

    entities = similarityMatrix["entities"]
    linkageType = similarityMatrix["linkageType"]
    matrix = np.array(similarityMatrix["matrix"])

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)

    fig = plt.figure(figsize=(25, 10))

    hierarchy.dendrogram(hierarc, labels=entities, distance_sort='descending')
    plt.savefig(codebasesPath + codebaseName + "/strategies/" + strategyName + "/dendrogramImage.png", format="png",
                bbox_inches='tight')

    with open(codebasesPath + codebaseName + "/strategies/" + strategyName + "/copheneticDistances.json",'w') as copheneticDistanceFile:
        copheneticDistanceFile.write(json.dumps(hierarchy.cophenet(hierarc).tolist(), indent=4))
