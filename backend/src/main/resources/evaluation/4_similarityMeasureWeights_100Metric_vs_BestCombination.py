from os import walk
import numpy as np
import pandas as pd
import math

files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break

for n in range(3, 11):
    print('N = ' + str(n))

    allTogetherWeights = []
    deltas100A = []
    deltasAvgDist = []

    for file in files:
        data = pd.read_csv("./data/" + file)

        maxComplexity = float(file.split("_")[1])
        if maxComplexity == 0:
            continue

        minComplexity = float("inf")
        count = 0
        weights = []
        A100Complexity = 0
        sumComplexities = 0

        for entry in data.values:
            if entry[0] != n:
                continue

            if entry[8] < minComplexity:
                minComplexity = entry[8]

            if entry[1] == 100:  # Access = 100
                A100Complexity = entry[8]  # pComplexity

            sumComplexities += entry[8]
            count += 1

        if minComplexity != float("inf"):  # nao ha entradas para o N iterado
            avgComplexities = sumComplexities / count

            deltaMed = avgComplexities - minComplexity
            deltaA100 = A100Complexity - minComplexity

            deltas100A.append(deltaA100)
            deltasAvgDist.append(deltaMed)

    distance = []
    for a, b in zip(deltas100A, deltasAvgDist):
        d = a/b
        if math.isnan(d):
            distance.append(0)
        else:
            distance.append(d)

    print('D:', round(np.mean(distance), 2), '\t', round(np.std(distance), 2))
