import numpy as np
from scipy.cluster import hierarchy
import matplotlib.pyplot as plt
import matplotlib.pylab as plab
import sys
import json

datafilePath = str(sys.argv[1])
dendrogramName = str(sys.argv[2])
linkageType = str(sys.argv[3])
cutValue = float(sys.argv[4])

with open(datafilePath + dendrogramName + ".txt") as f:
    datafile = json.load(f)

data_dictionary = {}

#luis
for controller in datafile:
    for entity in datafile[controller]:
        if entity in data_dictionary:
            data_dictionary[entity] += [controller]
        else:
            data_dictionary[entity] = [controller]

#eu
"""for controller in datafile:
    for entity in datafile[controller]:
        modes = datafile[controller][entity]
        if entity in data_dictionary:
            data_dictionary[entity] += [controller]
        else:
            data_dictionary[entity] = [controller]"""

base_classes = list(data_dictionary.keys())

similarity_matrix = np.zeros((len(data_dictionary.keys()), len(data_dictionary.keys())))

for index, value in np.ndenumerate(similarity_matrix):
    # Initialize diagonal with ones
    if len(set(index)) == 1:
        similarity_matrix[index] = 1
    else:
        # index[0] represents class C1 and index[1] represents class C2
        in_common = 0
        for c1 in data_dictionary[base_classes[index[0]]]:
            if c1 in data_dictionary[base_classes[index[1]]]:
                in_common += 1
        
        similarity_measure = in_common / len(data_dictionary[base_classes[index[0]]])

        similarity_matrix[index] = similarity_measure

hierarc = hierarchy.linkage(y=similarity_matrix, method=linkageType)

cut = hierarchy.cut_tree(hierarc, height=cutValue)

cluster_dict = {}
for i in range(len(cut)):
    if cut[i][0] in cluster_dict.keys():
        cluster_dict[cut[i][0]] += [base_classes[i]]
    else:
        cluster_dict[cut[i][0]] = [base_classes[i]]

for cluster in cluster_dict.keys():
    print(str(cluster) + ' ' + ','.join(cluster_dict[cluster]))
