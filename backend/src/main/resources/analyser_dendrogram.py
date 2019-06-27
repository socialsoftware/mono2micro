import numpy as np
from scipy.cluster import hierarchy
from sklearn import metrics
import sys
import json

cutValue = float(sys.argv[1])

with open("temp_matrix.json") as f:
    dendrogramData = json.load(f)

entities = dendrogramData["entities"]
matrix = np.array(dendrogramData["matrix"])
hierarc = hierarchy.linkage(y=matrix, method="average")

cut = hierarchy.cut_tree(hierarc, n_clusters=cutValue)

clusters = {}
for i in range(len(cut)):
    if str(cut[i][0]) in clusters.keys():
        clusters[str(cut[i][0])] += [entities[i]]
    else:
        clusters[str(cut[i][0])] = [entities[i]]

with open("temp_clusters.txt", 'w') as outfile:  
    outfile.write(json.dumps(clusters))