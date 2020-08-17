import json
from os import walk
import pandas as pd
import plotly.express as px

# Calculates a plot that assesses the relation between the number
# of singleton clusters present in a decomposition and the
# complexity of that decomposition

files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break

df = {
    'pComplexity': [],
    'maxClusterSize': [],
    'singletonClusters': [],
    'n': [],
    'text': []
}

for file in files:
    print(file)

    data = pd.read_csv("./data/" + file)

    for entry in data.values:
        singletonCount = 0
        maxClusterSize = 0
        # get name of file
        parsedFileName = "_".join(file.split("_")[2:])
        parsedFileName = parsedFileName[0:len(parsedFileName) - 4]
        a = entry[1]
        w = entry[2]
        r = entry[3]
        s = entry[4]
        n = entry[0]
        cutName = ",".join([str(int(a)), str(int(w)), str(int(r)), str(int(s)), str(int(n))]) + ".json"
        with open('../codebases/' +
                  parsedFileName + '/analyser/cuts/' + cutName) as f:
            dataFile = json.load(f)
            clusters = dataFile['clusters']
            for key in clusters.keys():
                lst = clusters[key]
                length = len(lst)
                if length == 1:
                    singletonCount += 1
                if length > maxClusterSize:
                    maxClusterSize = length

        numberOfEntities = int(file.split("_")[0])
        df['text'].append(parsedFileName)
        df['n'].append(str(entry[0]))
        df['pComplexity'].append(entry[8])
        df['maxClusterSize'].append(maxClusterSize/numberOfEntities)
        df['singletonClusters'].append(singletonCount)

# fig1 = px.scatter(
#     df,
#     x='maxClusterSize',
#     y='pComplexity',
#     color='n',
#     title="MaxClusterSize vs PComplexity",
#     labels={"x": "MaxClusterSize", "y": "PComplexity"},
#     range_x=[0, 1],
#     trendline='ols',
# )
# fig1.show()

# box plot style
boxFig = px.box(df,
                x="singletonClusters",
                y="pComplexity",
                color="n",
                title='#SingletonClusters vs pComplexity',
                points='all',
                range_y=[0, 1],
                hover_name='text')
boxFig.update_traces(marker=dict(size=2))
boxFig.show()
