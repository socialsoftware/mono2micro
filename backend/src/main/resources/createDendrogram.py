import numpy as np
from scipy.cluster import hierarchy
import sys
import json

import matplotlib
matplotlib.use('agg')
import matplotlib.pyplot as plt


codebasesPath = str(sys.argv[1])
codebaseName = str(sys.argv[2])
dendrogramName = str(sys.argv[3])

with open(codebasesPath + codebaseName + "/" + dendrogramName + "/similarityMatrix.json") as f:
    similarityMatrix = json.load(f)

entities = similarityMatrix["entities"]
matrix = np.array(similarityMatrix["matrix"])
linkageType = similarityMatrix["linkageType"]

hierarc = hierarchy.linkage(y=matrix, method=linkageType)

hierarchy.dendrogram(hierarc, labels=entities, distance_sort='descending')
plt.savefig(codebasesPath + codebaseName + "/" + dendrogramName + "/dendrogramImage.png", format="png", bbox_inches='tight')