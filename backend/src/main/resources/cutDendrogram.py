import numpy as np
from scipy.cluster import hierarchy
from sklearn import metrics
import matplotlib.pyplot as plt
import matplotlib.pylab as plab
import sys
import json

def calculateSilhouetteScore(base_classes, distances, cluster_dict):
    distancesFile = open("distances.txt","w+")
    ss = []
    for cluster in cluster_dict.keys():
        distancesFile.write('CLUSTER' + str(cluster) + '\n')
        cluster_ss = []
        for o1 in cluster_dict[cluster]:
            distancesFile.write('    ' + o1 + '\n')
            intra_distance = 0
            for d in distances:
                if d[0] == o1 and d[1] in cluster_dict[cluster]:
                    intra_distance += d[2]
            if len(cluster_dict[cluster])-1 > 0:
                intra_distance = intra_distance / (len(cluster_dict[cluster])-1)
            distancesFile.write('    intra ' + str(intra_distance) + '\n')
            extra_distances = []
            for cluster2 in cluster_dict.keys():
                extra_distance = 0
                if cluster2 != cluster:
                    for d in distances:
                        if d[0] == o1 and d[1] in cluster_dict[cluster2]:
                            extra_distance += d[2]
                    if len(cluster_dict[cluster2]) > 0:
                        extra_distance = extra_distance / (len(cluster_dict[cluster2]))
                    extra_distances += [extra_distance]
            
            extra_distance = min(extra_distances)
            distancesFile.write('    extra ' + str(extra_distance) + '\n')
            o1_ss = 0
            if max(intra_distance, extra_distance) != 0:
                o1_ss = (extra_distance - intra_distance) / max(intra_distance, extra_distance) 
            distancesFile.write('    ss ' + str(o1_ss) + '\n')
            cluster_ss += [o1_ss]
        
        if len(cluster_ss) > 0:
            cluster_ss = sum(cluster_ss) / len(cluster_ss)
        else:
            cluster_ss = 0
        distancesFile.write('  cluster ' + str(cluster_ss) + '\n')
        ss += [cluster_ss]

    if len(ss) > 0:
        ss = sum(ss) / len(ss)
    else:
        ss = 0
    distancesFile.write('ss ' + str(ss) + '\n')
    distancesFile.close()


datafilePath = str(sys.argv[1])
dendrogramName = str(sys.argv[2])
linkageType = str(sys.argv[3])
cutValue = float(sys.argv[4])

with open(datafilePath + dendrogramName + ".txt") as f:
    datafile = json.load(f)

data_dictionary = {}

for controller in datafile:
    for entity in datafile[controller]:
        if isinstance(datafile[controller], dict):
            modes = datafile[controller][entity] 
        if entity in data_dictionary:
            data_dictionary[entity] += [controller]
        else:
            data_dictionary[entity] = [controller]

base_classes = list(data_dictionary.keys())

similarity_matrix = np.zeros((len(data_dictionary.keys()), len(data_dictionary.keys())))

distances = []

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
        
        distances += [[base_classes[index[0]],base_classes[index[1]],similarity_measure]]

hierarc = hierarchy.linkage(y=similarity_matrix, method=linkageType)

cut = hierarchy.cut_tree(hierarc, height=cutValue)

cluster_dict = {}
for i in range(len(cut)):
    if cut[i][0] in cluster_dict.keys():
        cluster_dict[cut[i][0]] += [base_classes[i]]
    else:
        cluster_dict[cut[i][0]] = [base_classes[i]]

#calculateSilhouetteScore(base_classes, distances, cluster_dict)
nodes = hierarchy.fcluster(hierarc, len(cluster_dict), criterion="maxclust")
silhouetteScore = metrics.silhouette_score(similarity_matrix, nodes)
print(silhouetteScore)

for cluster in cluster_dict.keys():
    print(str(cluster) + ' ' + ','.join(cluster_dict[cluster]))
