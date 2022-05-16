import numpy as np
from scipy.cluster import hierarchy
from sklearn import metrics
import json
import os


def cutDendrogram(codebasesPath, codebaseName, dendrogramName, graphName, cutType, cutValue, commitBased):

    with open(codebasesPath + codebaseName + "/" + dendrogramName + "/similarityMatrix.json") as f:
        similarityMatrix = json.load(f)

    entities = similarityMatrix["entities"]
    print(entities)
    matrix = np.array(similarityMatrix["matrix"])
    linkageType = similarityMatrix["linkageType"]

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)

    if cutType == "H":
        cut = hierarchy.cut_tree(hierarc, height=cutValue)
    elif cutType == "N":
        cut = hierarchy.cut_tree(hierarc, n_clusters=cutValue)

    all_files_clusters = {}
    for i in range(len(cut)):
        if str(cut[i][0]) in all_files_clusters.keys():
            all_files_clusters[str(cut[i][0])] += [entities[i]]
        else:
            all_files_clusters[str(cut[i][0])] = [entities[i]]

    with open(codebasesPath + codebaseName + "/IDToEntity.json") as f:
        id_to_entity = json.load(f)
        entity_to_id = {}
        for _id, entity in id_to_entity.items():
            entity_to_id[entity] = _id

    all_nodes = hierarchy.fcluster(hierarc, len(all_files_clusters), criterion="maxclust")
    try:
        allSilhouetteScore = metrics.silhouette_score(matrix, all_nodes)
    except:
        allSilhouetteScore = 0

    allFilesClustersJSON = {"silhouetteScore": "{0:.2f}".format(allSilhouetteScore), "clusters": all_files_clusters}
    print(all_files_clusters)
    if commitBased == 'true':
        with open(codebasesPath + codebaseName + "/" + dendrogramName + "/" + graphName + "/clusters.json", 'w') as outfile:
            outfile.write(json.dumps(allFilesClustersJSON, indent=4))

        # We want to slightly modify the commit clusters info, so that the frontend has an easier
        # time displaying the data
        modified_all_files_clusters = {}
        for cluster, files in all_files_clusters.items():
            print(f"Looking at cluster {cluster}")
            other_files = []
            entity_files = []
            for file in files:
                filename = os.path.basename(file).replace(".java", "")
                if filename in list(entity_to_id.keys()):
                    entity_files.append(file)
                else:
                    other_files.append(file)
            modified_all_files_clusters[cluster] = {"entityFiles": entity_files, "otherFiles": other_files}
        allFilesClustersJSON = {"silhouetteScore": "{0:.2f}".format(allSilhouetteScore), "clusters": modified_all_files_clusters}

        with open(codebasesPath + codebaseName + "/" + dendrogramName + "/" + graphName + "/commit-clusters.json", 'w') as outfile:
            outfile.write(json.dumps(allFilesClustersJSON, indent=4))
    else:
        with open(codebasesPath + codebaseName + "/" + dendrogramName + "/" + graphName + "/clusters.json", 'w') as outfile:
            outfile.write(json.dumps(allFilesClustersJSON, indent=4))
        
