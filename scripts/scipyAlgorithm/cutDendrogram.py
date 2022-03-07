import numpy as np
from scipy.cluster import hierarchy
from sklearn import metrics
import json
import os


def cutDendrogram(codebasesPath, codebaseName, dendrogramName, graphName, cutType, cutValue, commitBased):

    with open(codebasesPath + codebaseName + "/" + dendrogramName + "/similarityMatrix.json") as f:
        similarityMatrix = json.load(f)

    entities = similarityMatrix["entities"]
    matrix = np.array(similarityMatrix["matrix"])
    linkageType = similarityMatrix["linkageType"]

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

    if commitBased == 'true':
        # When the base is "commits", we want to remove from the clusters non-domain entities

        with open(codebasesPath + codebaseName + "/IDToEntity.json") as f:
            entity_to_id = json.load(f)

        for cluster_id, cluster_entities in clusters.items():
            reduced_cluster_entities = []
            for entity in cluster_entities:
                entity_name = os.path.basename(entity).replace(".java", "")
                if entity_name in list(entity_to_id.keys()):
                    reduced_cluster_entities.append(entity_to_id[entity_name])

            clusters[cluster_id] = reduced_cluster_entities

    nodes = hierarchy.fcluster(hierarc, len(clusters), criterion="maxclust")
    try:
        silhouetteScore = metrics.silhouette_score(matrix, nodes)
    except:
        silhouetteScore = 0

    clustersJSON = {"silhouetteScore": "{0:.2f}".format(silhouetteScore), "clusters": clusters}

    with open(codebasesPath + codebaseName + "/" + dendrogramName + "/" + graphName + "/clusters.json", 'w') as outfile:
        outfile.write(json.dumps(clustersJSON, indent=4))
