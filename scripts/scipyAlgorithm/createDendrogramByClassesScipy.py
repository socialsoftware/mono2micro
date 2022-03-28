import numpy as np
from scipy.cluster import hierarchy
import json
import matplotlib
matplotlib.use('agg')
import matplotlib.pyplot as plt


def createDendrogramByClassesScipy(codebasesPath, codebaseName, dendrogramName):
    with open(codebasesPath + codebaseName + "/classes_embeddings.json") as f:
        classes_embeddings = json.load(f)

    names = []
    vectors = []
    for cls in classes_embeddings['classes']:
        names += [cls['name']]
        vectors += [cls['codeVector']]

    matrix = np.array(vectors)

    linkageType = classes_embeddings['linkageType']

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)

    fig = plt.figure(figsize=(25, 10))

    hierarchy.dendrogram(hierarc, labels=names, leaf_rotation=90.0, distance_sort='descending')
    plt.savefig(codebasesPath + codebaseName + "/" + dendrogramName + "/dendrogramImage.png", format="png",
                bbox_inches='tight')
