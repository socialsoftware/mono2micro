import numpy as np
from scipy.cluster import hierarchy
import matplotlib.pyplot as plt
import matplotlib.pylab as plab
import sys

datafilePath = str(sys.argv[1])
cutValue = float(sys.argv[2])

fp = open(datafilePath + "datafile.txt")
datafile = fp.read()

data_dictionary = {}

for line in datafile.strip().split("\n"):
    line_split = line.split(":")
    controller = line_split[0]
    base_class = line_split[1]
    mode = line_split[2].rstrip()
    if base_class in data_dictionary:
        data_dictionary[base_class] += [controller]
    else:
        data_dictionary[base_class] = [controller]

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


# Get clustering linkage type
linkage_type = 'single'

hierarc = hierarchy.linkage(y=similarity_matrix, method=linkage_type)

cut = hierarchy.cut_tree(hierarc, height=cutValue)

cluster_dict = {}
for i in range(len(cut)):
    if cut[i][0] in cluster_dict.keys():
        cluster_dict[cut[i][0]] += [base_classes[i]]
    else:
        cluster_dict[cut[i][0]] = [base_classes[i]]

for cluster in cluster_dict.keys():
    print(str(cluster) + ' ' + ','.join(cluster_dict[cluster]))
