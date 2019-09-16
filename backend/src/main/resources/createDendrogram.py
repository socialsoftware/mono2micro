import numpy as np
from scipy.cluster import hierarchy
import sys
import json

import matplotlib
matplotlib.use('agg')
import matplotlib.pyplot as plt


codebaseFolder = str(sys.argv[1])
codebaseName = str(sys.argv[2])
dendrogramName = str(sys.argv[3])
linkageType = str(sys.argv[4])

with open(codebaseFolder + codebaseName + "/" + dendrogramName + ".txt") as f:
    dendrogramData = json.load(f)

entities = dendrogramData["entities"]
matrix = np.array(dendrogramData["matrix"])
hierarc = hierarchy.linkage(y=matrix, method=linkageType)

hierarchy.dendrogram(hierarc, labels=entities, distance_sort='descending')
plt.savefig(codebaseFolder + codebaseName + "/" + dendrogramName + ".png", format="png", bbox_inches='tight')