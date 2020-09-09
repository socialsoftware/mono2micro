import json
from os import walk
import plotly
import plotly.express as px
import numpy as np
import pandas as pd


def getClusters(complexityWeights):
    cutName = ",".join(
        [
            str(int(complexityWeights[0])),
            str(int(complexityWeights[1])),
            str(int(complexityWeights[2])),
            str(int(complexityWeights[3])),
            str(int(n))
        ]
    ) + ".json"

    with open('/home/samuel/ProjetoTese/mono2micro/backend/src/main/resources/codebases/' +
              parsedFileName + '/analyser/cuts/' + cutName) as f:
        dataFile = json.load(f)
        return dataFile['clusters']


files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break

df = {
    'codebase': [],
    'n': [],
    'dev': []
}
for file in files:
    print(file)

    data = pd.read_csv("./data/" + file)

    for n in range(3, 11):
        minComplexity = float("inf")
        minComplexityWeights = []  # a, w, r, s
        for entry in data.values:
            if entry[0] != n:
                continue

            if entry[8] < minComplexity:
                minComplexity = entry[8]
                minComplexityWeights = [entry[1], entry[2], entry[3], entry[4]]

        if minComplexity == float("inf"):  # no entries for this N
            continue

        parsedFileName = "_".join(file.split("_")[2:])
        parsedFileName = parsedFileName[0:len(parsedFileName) - 4]

        clusters = getClusters(minComplexityWeights)
        clusterSizes = []
        for cluster in clusters:
            clusterSizes.append(len(clusters[cluster]))

        maxDev = []
        for i in range(1, n):
            maxDev.append(1)
        maxDev.append(int(file.split("_")[0])-(n-1))

        df['codebase'].append(parsedFileName)
        df['n'].append(n)
        df['dev'].append(round(np.std(clusterSizes) / np.std(maxDev), 2))

df = pd.DataFrame(df)
# boxFig = px.box(
#     df,
#     x="n",
#     y="dev",
#     title="Devs",
# )
# boxFig.show()
for n in range(3, 11):
    df_n = df[df.n == n]
    print(str(round(np.mean(df_n['dev']), 2)))

