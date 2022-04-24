import numpy as np
from scipy.cluster import hierarchy
import json
from os import path

minClusters = 3
clusterStep = 1
maxClusters = -1

def analyser(codebasesPath, codebaseName):
    global maxClusters

    with open(codebasesPath + codebaseName + "/features_embeddings.json") as f:
        features_embeddings = json.load(f)

    totalNumberOfEntities = features_embeddings['numberOfEntities']

    if 3 < totalNumberOfEntities < 10:
        maxClusters = 3
    elif 10 <= totalNumberOfEntities < 20:
        maxClusters = 5
    elif 20 <= totalNumberOfEntities:
        maxClusters = 10
    else:
        raise Exception("Number of entities is too small (less than 4)")

    try:
        for clusterSize in range(minClusters, maxClusters + 1, clusterStep):
            createCut(codebasesPath, codebaseName, clusterSize)
            
    except Exception as e:
        print(e)


def createCut(codebasesPath, codebaseName, clusterSize):
    
    with open(codebasesPath + codebaseName + "/features_embeddings.json") as f:
        features_embeddings = json.load(f)

    with open(codebasesPath + codebaseName + "/datafile.json") as f:
        features_entities_accesses = json.load(f)

    maxDepth = features_embeddings['maxDepth']
    controllersWeight = features_embeddings['controllersWeight']
    servicesWeight = features_embeddings['servicesWeight']
    intermediateMethodsWeight = features_embeddings['intermediateMethodsWeight']
    entitiesWeight = features_embeddings['entitiesWeight']
    linkageType = features_embeddings['linkageType']

    name = ','.join(map(str, [maxDepth, controllersWeight, servicesWeight, intermediateMethodsWeight, entitiesWeight, clusterSize]))

    filePath = codebasesPath + codebaseName + "/analyser/features/methodCalls/cuts/" + name + ".json"

    if (path.exists(filePath)):
        return

    names = []
    classes = []
    vectors = []
    for feature in features_embeddings['features']:
        names += [feature['signature'].split("(")[0].split(".")[-1]]
        classes += [feature['class']]
        vectors += [feature['codeVector']]

    matrix = np.array(vectors)

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)
    cut = hierarchy.cut_tree(hierarc, n_clusters=clusterSize)

    clusters = {}
    for i in range(len(cut)):
        if str(cut[i][0]) in clusters.keys():
            clusters[str(cut[i][0])] += [i]
        else:
            clusters[str(cut[i][0])] = [i]

    entities_clusters_accesses = {}

    for cluster in clusters.keys():

        for idx in clusters[cluster]:
            feature = classes[idx] + "." + names[idx]

            if feature in features_entities_accesses.keys():
                accesses = features_entities_accesses[feature]['t'][0]['a']

                for access in accesses:
                    access_type = access[0]
                    entity = access[1]

                    if entity in entities_clusters_accesses.keys():

                        if cluster not in entities_clusters_accesses[entity].keys():
                            entities_clusters_accesses[entity][cluster] = { "R" : 0, "W" : 0}

                        entities_clusters_accesses[entity][cluster][access_type] += 1

                    else:
                        entities_clusters_accesses[entity] = {}
                        entities_clusters_accesses[entity][cluster] = { "R" : 0, "W" : 0}
                        entities_clusters_accesses[entity][cluster][access_type] = 1

    entities_cluster = {}
    for entity in entities_clusters_accesses.keys():
        max_nbr_accesses = 0
        attr_cluster = "0"

        for cluster in entities_clusters_accesses[entity].keys():
            nbr_accesses = entities_clusters_accesses[entity][cluster]["R"] + entities_clusters_accesses[entity][cluster]["W"]

            if nbr_accesses > max_nbr_accesses:
                max_nbr_accesses = nbr_accesses
                attr_cluster = cluster

        if attr_cluster in entities_cluster.keys():
            entities_cluster[attr_cluster] += [entity]
        else:
            entities_cluster[attr_cluster] = [entity]

    for cluster in clusters.keys():
        if cluster not in entities_cluster.keys():
            entities_cluster[cluster] = []

    clustersJSON = {"clusters": entities_cluster}

    with open(filePath, 'w') as outfile:
        outfile.write(json.dumps(clustersJSON, indent=4))

