import numpy as np
from scipy.cluster import hierarchy
from sklearn import metrics
import sys
import json


codebasesPath = str(sys.argv[1])
codebaseName = str(sys.argv[2])
dendrogramName = str(sys.argv[3])
graphName = str(sys.argv[4])
cutType = str(sys.argv[5])
cutValue = float(sys.argv[6])

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

nodes = hierarchy.fcluster(hierarc, len(clusters), criterion="maxclust")
try:
    silhouetteScore = metrics.silhouette_score(matrix, nodes)
except:
    silhouetteScore = 0

clustersJSON = {}
clustersJSON["silhouetteScore"] = "{0:.2f}".format(silhouetteScore)
clustersJSON["clusters"] = clusters

with open(codebasesPath + codebaseName + "/" + dendrogramName + "/" + graphName + "/clusters.json", 'w') as outfile:  
    outfile.write(json.dumps(clustersJSON, indent=4))