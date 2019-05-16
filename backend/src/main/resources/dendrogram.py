import numpy as np
from scipy.cluster import hierarchy
import matplotlib.pylab as plab
import sys
import json
import time

start_time = time.time()

datafilePath = str(sys.argv[1])
dendrogramName = str(sys.argv[2])
linkageType = str(sys.argv[3])
accessMetricWeight = float(sys.argv[4])
readWriteMetricWeight = float(sys.argv[5])
sequenceMetricWeight = float(sys.argv[6])

data_dictionary = {}

def processDatafile(data_dictionary, datafile):
    for controller in datafile:
        for entityArray in datafile[controller]:
            entity = entityArray[0]
            mode = entityArray[1]
            if entity in data_dictionary:
                if controller in [c[0] for c in data_dictionary[entity]]:
                    for i in range(len(data_dictionary[entity])):
                        if data_dictionary[entity][i][0] == controller:
                            if mode not in data_dictionary[entity][i][1]:
                                data_dictionary[entity][i][1] += [mode]
                            break
                else:
                    data_dictionary[entity] += [[controller, [mode]]]
            else:
                data_dictionary[entity] = [[controller, [mode]]]
                 

def createSimilarityMatrix(data_dictionary, base_classes):
    similarity_matrix = np.zeros((len(data_dictionary.keys()), len(data_dictionary.keys())))

    for index, value in np.ndenumerate(similarity_matrix):
        # Initialize diagonal with ones
        if len(set(index)) == 1:
            similarity_matrix[index] = 1
        else:
            # index[0] represents class C1 and index[1] represents class C2
            in_common = 0
            in_common_writes = 0
            for c1 in data_dictionary[base_classes[index[0]]]:
                for c2 in data_dictionary[base_classes[index[1]]]:
                    if c1[0] == c2[0]:
                        in_common += 1
                    if c1[0] == c2[0] and 'W' in c1[1] and 'W' in c2[1]:
                        in_common_writes += 1

            entitiesSequenceCount = 0
            sequenceControllerCount = 0
            """for controller in datafile:
                hit = False
                for i in range(len(datafile[controller])-1):
                    if datafile[controller][i][0] == base_classes[index[0]] and datafile[controller][i+1][0] == base_classes[index[1]]:
                        entitiesSequenceCount += 1
                        hit = True
                if hit:
                    sequenceControllerCount += len(datafile[controller])-1"""

            accessMetric = in_common / len(data_dictionary[base_classes[index[0]]])
            readWriteMetric = in_common_writes / len(data_dictionary[base_classes[index[0]]])
            if sequenceControllerCount == 0:
                sequenceMetric = 0
            else:
                sequenceMetric = entitiesSequenceCount / sequenceControllerCount

            similarity_measure = accessMetric * accessMetricWeight + readWriteMetric * readWriteMetricWeight + sequenceMetric * sequenceMetricWeight

            similarity_matrix[index] = similarity_measure
    return similarity_matrix


with open(datafilePath + dendrogramName + ".txt") as f:
    datafile = json.load(f)

processDatafile(data_dictionary, datafile)

base_classes = sorted(list(data_dictionary.keys()))

similarityMatrix = createSimilarityMatrix(data_dictionary, base_classes)

np.save(datafilePath + dendrogramName + ".npy", similarityMatrix)

hierarc = hierarchy.linkage(y=similarityMatrix, method=linkageType)

dend = hierarchy.dendrogram(hierarc, labels=base_classes, distance_sort='descending')
plab.savefig(datafilePath + dendrogramName + ".png", format="png", bbox_inches='tight')

elapsed_time = time.time() - start_time
print(str(elapsed_time))