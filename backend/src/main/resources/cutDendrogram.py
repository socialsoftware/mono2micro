import numpy as np
from scipy.cluster import hierarchy
from sklearn import metrics
import matplotlib.pylab as plab
import sys
import json


datafilePath = str(sys.argv[1])
dendrogramName = str(sys.argv[2])
linkageType = str(sys.argv[3])
cutValue = float(sys.argv[4])


def getEntities(datafile):
    entities = set()
    for controller in datafile:
        for entityArray in datafile[controller]:
            entities.add(entityArray[0])
    return sorted(list(entities))    

with open(datafilePath + dendrogramName + ".txt") as f:
    datafile = json.load(f)

entities = getEntities(datafile)

similarityMatrix = np.load(datafilePath + dendrogramName + ".npy")

hierarc = hierarchy.linkage(y=similarityMatrix, method=linkageType)

cut = hierarchy.cut_tree(hierarc, height=cutValue)

clusters = {}
for i in range(len(cut)):
    clusterName = "Cluster" + str(cut[i][0])
    if clusterName in clusters.keys():
        clusters[clusterName] += [entities[i]]
    else:
        clusters[clusterName] = [entities[i]]

nodes = hierarchy.fcluster(hierarc, len(clusters), criterion="maxclust")
silhouetteScore = metrics.silhouette_score(similarityMatrix, nodes)
print(silhouetteScore)

with open("temp_clusters.txt", 'w') as outfile:  
    outfile.write(json.dumps(clusters))