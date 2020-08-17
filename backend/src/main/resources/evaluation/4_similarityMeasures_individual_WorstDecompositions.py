from os import walk
import numpy as np
import pandas as pd
import plotly
plotly.io.orca.config.executable = '~/anaconda3/bin/orca'

files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break

dictTop = {}
for n in range(3, 11):
    print('N = ' + str(n))
    allTogetherWeights = []

    for file in files:
        data = pd.read_csv("./data/" + file)

        maxComplexity = float(file.split("_")[1])
        if maxComplexity == 0:
            continue

        maxComplexity = 0
        count = 0
        weights = []
        for entry in data.values:
            if entry[0] != n:
                continue

            if entry[7] > maxComplexity:
                maxComplexity = entry[7]
                count = 1
                weights.clear()
                weights.append([entry[1], entry[2], entry[3], entry[4]])

            elif entry[7] == maxComplexity:
                count += 1
                weights.append([entry[1], entry[2], entry[3], entry[4]])

        if 10 >= len(weights) > 0:
            a = []
            w = []
            r = []
            s = []
            for entry in weights:
                a.append(entry[0])
                w.append(entry[1])
                r.append(entry[2])
                s.append(entry[3])

            medWeights = [np.mean(a), np.mean(w), np.mean(r), np.mean(s)]
            allTogetherWeights.append(medWeights)

    a = []
    w = []
    r = []
    s = []
    for entry in allTogetherWeights:
        a.append(entry[0])
        w.append(entry[1])
        r.append(entry[2])
        s.append(entry[3])
    allTogetherMedWeights = [round(np.mean(a), 2), round(np.mean(w), 2), round(np.mean(r), 2), round(np.mean(s), 2)]
    allTogetherStdDev = [round(np.std(a), 2), round(np.std(w), 2), round(np.std(r), 2), round(np.std(s), 2)]
    print("[A, W, R, S]")
    print("med: ", allTogetherMedWeights)
    print("std: ", allTogetherStdDev)
    print()
