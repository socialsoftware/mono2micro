import numpy as np
from scipy.cluster import hierarchy
import json
from os import path
import multiprocessing
from copy import deepcopy
import sys

interval = 10
multiplier = 10
minClusters = 3
clusterStep = 1
maxClusters = -1
current_request = 0

class Task:

    def __init__(self, arguments, max_clusters):
        self.arguments = arguments
        self.max_clusters = max_clusters

    def __call__(self):
        return self.sendRequest(*self.arguments)

    def sendRequest(self, a, w, r, s, c, author, maxClusterCut, totalNumberOfEntities, codebase, entities, linkageType,
                    similarityMatrix):
        a *= multiplier
        w *= multiplier
        r *= multiplier
        s *= multiplier
        c *= multiplier
        author *= multiplier

        def createCut(a, w, r, s, c, author, n, codebase, entities, linkageType, similarityMatrix):
            name = ','.join(map(str, [a, w, r, s, c, author, n]))
            print(f"Creating cut: {name}")
            sys.stdout.flush()

            filePath = codebase + "/analyser/cuts/" + name + ".json"

            if (path.exists(filePath)):
                return

            matrix = deepcopy(similarityMatrix["matrix"])
            for i in range(len(matrix)):
                for j in range(len(matrix)):
                    # if i == 0 and j == 0:
                    #     print(f"Looking at matrix[{i}][{j}]")
                    #     print(matrix[i][j])
                    matrix[i][j] = matrix[i][j][0] * a / 100 + \
                                   matrix[i][j][1] * w / 100 + \
                                   matrix[i][j][2] * r / 100 + \
                                   matrix[i][j][3] * s / 100 + \
                                   matrix[i][j][4] * c / 100 + \
                                   matrix[i][j][5] * author / 100

            matrix = np.array(matrix)

            hierarc = hierarchy.linkage(y=matrix, method=linkageType)

            cut = hierarchy.cut_tree(hierarc, n_clusters=n)

            clusters = {}
            for i in range(len(cut)):
                if str(cut[i][0]) in clusters.keys():
                    clusters[str(cut[i][0])] += [entities[i]]
                else:
                    clusters[str(cut[i][0])] = [entities[i]]

            clustersJSON = {}
            clustersJSON["clusters"] = clusters

            with open(filePath, 'w') as outfile:
                outfile.write(json.dumps(clustersJSON, indent=4))

        if maxClusterCut:
            createCut(a, w, r, s, c, author, totalNumberOfEntities, codebase, entities, linkageType, similarityMatrix)
        else:
            for n in range(minClusters, self.max_clusters + 1, clusterStep):
                createCut(a, w, r, s, c, author, n, codebase, entities, linkageType, similarityMatrix)

        return True



class Consumer(multiprocessing.Process):

    def __init__(self, task_queue, similarityMatrix):
        print("Initializing consumer")
        multiprocessing.Process.__init__(self)
        self.task_queue = task_queue
        self.similarityMatrix = similarityMatrix

    def run(self):
        proc_name = self.name
        while True:
            next_task = self.task_queue.get()
            if next_task is None:
                # Poison pill means shutdown
                print('{}: Exiting'.format(proc_name))
                self.task_queue.task_done()
                break
            next_task()
            self.task_queue.task_done()


def analyser(codebasesPath, codebaseName, totalNumberOfEntities):
    global maxClusters
    print("Creating all cuts for " + codebaseName)
    if 3 < totalNumberOfEntities < 10:
        maxClusters = 3
    elif 10 <= totalNumberOfEntities < 20:
        maxClusters = 5
    elif 20 <= totalNumberOfEntities:
        maxClusters = 10
    else:
        raise Exception("Number of entities is too small (less than 4)")

    codebase = codebasesPath + codebaseName

    with open(codebase + "/analyser/similarityMatrix.json") as f:
        similarityMatrix = json.load(f)


    entities = similarityMatrix["entities"]
    linkageType = similarityMatrix["linkageType"]

    operations = multiprocessing.JoinableQueue()
    num_consumers = multiprocessing.cpu_count()
    print('Creating {} consumers'.format(num_consumers))
    consumers = [
        Consumer(operations, similarityMatrix)
        for _ in range(num_consumers)
    ]
    for w in consumers:
        print(f"Starting {w}")
        w.start()

    try:
        for a in range(interval, -1, -1):
            remainder = interval - a
            if remainder == 0:
                operations.put(Task((a, 0, 0, 0, 0, 0, False, totalNumberOfEntities, codebase, entities, linkageType, similarityMatrix), maxClusters))
            else:
                for w in range(remainder, -1, -1):
                    remainder2 = remainder - w
                    if remainder2 == 0:
                        operations.put(Task((a, w, 0, 0, 0, 0, False, totalNumberOfEntities, codebase, entities, linkageType, similarityMatrix), maxClusters))
                    else:
                        for r in range(remainder2, -1, -1):
                            remainder3 = remainder2 - r
                            if remainder3 == 0:
                                operations.put(Task((a, w, r, 0, 0, 0, False, totalNumberOfEntities, codebase, entities,
                                            linkageType, similarityMatrix), maxClusters))
                            else:
                                for s in range(remainder3, -1, -1):
                                    remainder4 = remainder3 - s
                                    if remainder4 == 0:
                                        operations.put(Task((a, w, r, s, 0, 0, False, totalNumberOfEntities, codebase, entities,
                                                    linkageType, similarityMatrix), maxClusters))
                                    else:
                                        for c in range(remainder4, -1, -1):
                                            remainder5 = remainder4 - c
                                            if remainder5 == 0:
                                                operations.put(Task((a, w, r, s, c, 0, False, totalNumberOfEntities, codebase,
                                                            entities,
                                                            linkageType, similarityMatrix), maxClusters))
                                            else:
                                                operations.put(Task((a, w, r, s, c, remainder5, False, totalNumberOfEntities,
                                                            codebase, entities,
                                                            linkageType, similarityMatrix), maxClusters))

        # last request to discover max Complexity possible (each cluster is singleton)
        operations.put(Task((10, 0, 0, 0, 0, 0, True, totalNumberOfEntities, codebase, entities, linkageType, similarityMatrix), maxClusters))
    except Exception as e:
        print(e)


    # Poison pill for consumers to stop
    for i in range(num_consumers):
        operations.put(None)

    operations.join()

    print("Left operations.join...")
    print(operations.qsize())



