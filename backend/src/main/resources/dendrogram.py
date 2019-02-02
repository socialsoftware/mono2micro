import numpy as np
from scipy.cluster import hierarchy
import matplotlib.pyplot as plt
import matplotlib.pylab as plab
import sys

datafilePath = str(sys.argv[1])

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

tree = hierarchy.to_tree(hierarc)

leaves = hierarchy.leaves_list(hierarc)

result = []
not_processed = [tree]

while not_processed != []:
    processing = not_processed[0]
    del(not_processed[0])

    line = ""

    if processing.id in leaves:
        line += base_classes[processing.id] + ":"
        line += "null:null"
        line += ":" + str(processing.dist * -1)
    else:
        line += str(processing.id) + ":"
        if processing.left.id in leaves:
            line += base_classes[processing.left.id] + ":"
        else:
            line += str(processing.left.id) + ":"
        if processing.right.id in leaves:
            line += base_classes[processing.right.id]
        else:
            line += str(processing.right.id)
        line += ":" + str(processing.dist * -1)
        not_processed += [processing.left, processing.right]

    result += [line]

for l in result:
    print(l)

dend = hierarchy.dendrogram(hierarc, labels=base_classes, distance_sort='descending')
plab.savefig(datafilePath + "dend.png", format="png", bbox_inches='tight')
#plt.show()