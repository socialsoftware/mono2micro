import numpy as np
from scipy.cluster import hierarchy
from sklearn import metrics
import json


def cutDendrogramByFeaturesEntities(codebasesPath, codebaseName, dendrogramName, graphName, cutType, cutValue):
    with open(codebasesPath + codebaseName + "/entities_embeddings.json") as f:
        entities_embeddings = json.load(f)

    names = []
    ids = []
    vectors = []
    for cls in entities_embeddings['entities']:
        if 'translationID' in cls.keys():
            names += [cls['name']]
            ids += [cls['translationID']]
            vectors += [cls['codeVector']]

    matrix = np.array(vectors)
    linkageType = entities_embeddings['linkageType']

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)

    if cutType == "H":
        cut = hierarchy.cut_tree(hierarc, height=cutValue)
    elif cutType == "N":
        cut = hierarchy.cut_tree(hierarc, n_clusters=cutValue)

    clusters = {}
    for i in range(len(cut)):
        if str(cut[i][0]) in clusters.keys():
            clusters[str(cut[i][0])] += [ids[i]]
        else:
            clusters[str(cut[i][0])] = [ids[i]]

    nodes = hierarchy.fcluster(hierarc, len(clusters), criterion="maxclust")
    try:
        silhouetteScore = metrics.silhouette_score(matrix, nodes)
    except:
        silhouetteScore = 0

    clustersJSON = {"silhouetteScore": "{0:.2f}".format(silhouetteScore), "clusters": clusters}

    with open(codebasesPath + codebaseName + "/" + dendrogramName + "/" + graphName + "/clusters.json", 'w') as outfile:
        outfile.write(json.dumps(clustersJSON, indent=4))