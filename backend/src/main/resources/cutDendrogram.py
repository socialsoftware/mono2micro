import numpy as np
from scipy.cluster import hierarchy
from sklearn import metrics
import sys
import json

datafilePath = str(sys.argv[1])
dendrogramName = str(sys.argv[2])
linkageType = str(sys.argv[3])
cutValue = float(sys.argv[4])

with open(datafilePath + dendrogramName + ".txt") as f:
    dendrogramData = json.load(f)

entities = dendrogramData["entities"]
matrix = np.array(dendrogramData["matrix"])
hierarc = hierarchy.linkage(y=matrix, method=linkageType)

cut = hierarchy.cut_tree(hierarc, height=cutValue)

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
print(silhouetteScore)

with open("temp_clusters.txt", 'w') as outfile:  
    outfile.write(json.dumps(clusters))