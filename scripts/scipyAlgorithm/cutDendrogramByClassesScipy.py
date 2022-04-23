import numpy as np
from scipy.cluster import hierarchy
from sklearn import metrics
import json


def cutDendrogramByClasses(codebasesPath, codebaseName, dendrogramName, graphName, cutType, cutValue):

    with open(codebasesPath + codebaseName + "/classes_embeddings.json") as f:
        classes_embeddings = json.load(f)

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
    linkageType = classes_embeddings['linkageType']

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)

    if cutType == "H":
        cut = hierarchy.cut_tree(hierarc, height=cutValue)
    elif cutType == "N":
        cut = hierarchy.cut_tree(hierarc, n_clusters=cutValue)

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

    nodes = hierarchy.fcluster(hierarc, len(clusters), criterion="maxclust")
    try:
        silhouetteScore = metrics.silhouette_score(matrix, nodes)
    except:
        silhouetteScore = 0

    clustersJSON = {"silhouetteScore": "{0:.2f}".format(silhouetteScore), "clusters": clusters}

    with open(codebasesPath + codebaseName + "/" + dendrogramName + "/" + graphName + "/clusters.json", 'w') as outfile:
        outfile.write(json.dumps(clustersJSON, indent=4))
