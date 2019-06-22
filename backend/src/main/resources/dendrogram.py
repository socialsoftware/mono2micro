import numpy as np
from scipy.cluster import hierarchy
import matplotlib.pylab as plab
import sys
import json
import time

start_time = time.time()

codebaseFolder = str(sys.argv[1])
codebaseName = str(sys.argv[2])
dendrogramName = str(sys.argv[3])
linkageType = str(sys.argv[4])

with open(codebaseFolder + codebaseName + "/" + dendrogramName + ".txt") as f:
    dendrogramData = json.load(f)

entities = dendrogramData["entities"]
matrix = np.array(dendrogramData["matrix"])
hierarc = hierarchy.linkage(y=matrix, method=linkageType)

dend = hierarchy.dendrogram(hierarc, labels=entities, distance_sort='descending')
topLevel = dend["dcoord"][-1][1]
middleCut = round(topLevel / 2, 1)
plab.savefig(codebaseFolder + codebaseName + "/" + dendrogramName + ".png", format="png", bbox_inches='tight')

elapsed_time = time.time() - start_time
print(str(elapsed_time))