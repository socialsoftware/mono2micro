import numpy as np
from scipy.cluster import hierarchy
import sys
np.set_printoptions(threshold=sys.maxsize)
import matplotlib.pyplot as plt
from collections import defaultdict


def cut(codebases_path, codebase_name, commit_metric_value, authors_metric_value, n_clusters, similarityMatrixData):
    print(f"Performing cut with {n_clusters} clusters")
    entities = similarityMatrixData["entities"]
    linkageType = similarityMatrixData["linkageType"]
    matrix = similarityMatrixData["matrix"]
    for i in range(len(matrix)):
        for j in range(len(matrix)):
            matrix[i][j] = matrix[i][j][0] * int(commit_metric_value) / 100 + \
                           matrix[i][j][1] * int(authors_metric_value) / 100

    matrix = np.array(matrix)
    # print(matrix)
    hierarc = hierarchy.linkage(y=matrix, method=linkageType)

    cut_result = hierarchy.cut_tree(hierarc, n_clusters=n_clusters)
    fcluster_clusters = hierarchy.fcluster(hierarc, n_clusters, criterion='maxclust')
    # print(entities)
    clusters = {}
    for i in range(len(fcluster_clusters)):
        if str(fcluster_clusters[i]) in clusters.keys():
            clusters[str(fcluster_clusters[i])] += [entities[i]]
        else:
            clusters[str(fcluster_clusters[i])] = [entities[i]]

    # print(fcluster_clusters)
    # print(cut_result)
    # clusters = {}
    # print(cut_result)
    # for i in range(len(cut_result)):
    #     if str(cut_result[i][0]) in clusters.keys():
    #         clusters[str(cut_result[i][0])] += [entities[i]]
    #     else:
    #         clusters[str(cut_result[i][0])] = [entities[i]]

    # print(f"Cut {commit_metric_value},{authors_metric_value},{n_clusters} done.")
    # print(clusters)
    return clusters