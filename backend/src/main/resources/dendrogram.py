import numpy as np
from scipy.cluster import hierarchy
import matplotlib.pyplot as plt
import matplotlib.pylab as plab
import sys
import json

datafilePath = str(sys.argv[1])

#with open(datafilePath + "datafile.txt") as f:
with open(datafilePath + "datafile.txt") as f:
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

#print(similarity_matrix)

# Get clustering linkage type
linkage_type = 'average'

hierarc = hierarchy.linkage(y=similarity_matrix, method=linkage_type)

dend = hierarchy.dendrogram(hierarc, labels=base_classes, distance_sort='descending')
plab.savefig(datafilePath + "dend.png", format="png", bbox_inches='tight')
#plt.show()