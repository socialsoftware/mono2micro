import numpy as np
from scipy.cluster import hierarchy
import json
import matplotlib
matplotlib.use('agg')
import matplotlib.pyplot as plt


def createDendrogramByEntitiesScipy(codebasesPath, codebaseName, dendrogramName):
    with open(codebasesPath + codebaseName + "/entities_embeddings.json") as f:
        entities_embeddings = json.load(f)

    names = []
    vectors = []
    for entity in entities_embeddings['entities']:
        names += [entity['name']]
        vectors += [entity['codeVector']]

    matrix = np.array(vectors)

    linkageType = entities_embeddings['linkageType']

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)

    fig = plt.figure(figsize=(25, 10))

    hierarchy.dendrogram(hierarc, labels=names, leaf_rotation=90.0, distance_sort='descending')
    plt.savefig(codebasesPath + codebaseName + "/" + dendrogramName + "/dendrogramImage.png", format="png",
                bbox_inches='tight')
