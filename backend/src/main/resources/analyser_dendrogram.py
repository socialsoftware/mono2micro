import numpy as np
from scipy.cluster import hierarchy
import sys
import json

filename = str(sys.argv[1])
cutValue = float(sys.argv[2])

with open(filename) as f:
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

with open("cluster" + filename[6:-5] + ".txt", 'w') as outfile:  
    outfile.write(json.dumps(clusters))