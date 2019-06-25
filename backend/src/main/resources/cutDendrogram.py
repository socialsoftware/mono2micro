import numpy as np
from scipy.cluster import hierarchy
from sklearn import metrics
import sys
import json

codebaseFolder = str(sys.argv[1])
codebaseName = str(sys.argv[2])
dendrogramName = str(sys.argv[3])
linkageType = str(sys.argv[4])
cutValue = float(sys.argv[5])
cutType = str(sys.argv[6])

with open(codebaseFolder + codebaseName + "/" + dendrogramName + ".txt") as f:
    dendrogramData = json.load(f)

entities = dendrogramData["entities"]
matrix = np.array(dendrogramData["matrix"])
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
print(silhouetteScore)

with open("temp_clusters.txt", 'w') as outfile:  
    outfile.write(json.dumps(clusters))