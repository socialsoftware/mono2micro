import os
from os import walk
import pandas as pd
import plotly
import plotly.express as px
from pathlib import Path

# For each codebase and N value, calculates a plot that assesses the relation
# between the weights given to each measure and the complexity, coupling and cohesion
# obtained. Studies the performance of each measure, individually, for each codebase.

# Warning: You need orca installed to write plot images to filesystem
# Replace path with your own
home = str(Path.home())
plotly.io.orca.config.executable = home + '/anaconda3/bin/orca'

def iToChar(i):
    if i == 1:
        return 'A'
    if i == 2:
        return 'W'
    if i == 3:
        return 'R'
    if i == 4:
        return 'S'


dataFolderPath = os.getcwd() + "/simMeasuresVsComplexity/"
if not os.path.exists(dataFolderPath):
    os.mkdir(dataFolderPath)

dataFolderPath = os.getcwd() + "/simMeasuresVsCohesion/"
if not os.path.exists(dataFolderPath):
    os.mkdir(dataFolderPath)

dataFolderPath = os.getcwd() + "/simMeasuresVsCoupling/"
if not os.path.exists(dataFolderPath):
    os.mkdir(dataFolderPath)

files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break

for file in files:
    print(file)

    data = pd.read_csv("./data/" + file)

    df = {
        'n': [],
        'weight': [],
        'color': [],
        'pComplexity': [],
        'coupling': [],
        'cohesion': [],
    }

    for entry in data.values:
        for i in range(1, 5):
            df['n'].append(entry[0])
            df['weight'].append(entry[i])
            df['color'].append(iToChar(i))
            df['cohesion'].append(entry[5])
            df['coupling'].append(entry[6])
            df['pComplexity'].append(entry[8])

    df = pd.DataFrame(df)

    for i in range(3, 11):
        df_n = df[df.n == i]

        if len(df_n['n']) == 0:
            continue

        fig1 = px.scatter(
            df_n,
            x='weight',
            y='pComplexity',
            color='color',
            title="N = " + str(i),
            labels={"x": "Weight", "y": "Complexity"},
            range_y=[0, 1],
            trendline='ols',
        )
        fig1.write_image("simMeasuresVsComplexity/" + file + str(i) + '.jpeg')

        fig2 = px.scatter(
            df_n,
            x='weight',
            y='cohesion',
            color='color',
            title="N = " + str(i),
            labels={"x": "Weight", "y": "Cohesion"},
            range_y=[0, 1],
            trendline='ols',
        )
        fig2.write_image("simMeasuresVsCohesion/" + file + str(i) + '.jpeg')

        fig3 = px.scatter(
            df_n,
            x='weight',
            y='coupling',
            color='color',
            title="N = " + str(i),
            labels={"x": "Weight", "y": "Coupling"},
            range_y=[0, 1],
            trendline='ols',
        )
        fig3.write_image("simMeasuresVsCoupling/" + file + str(i) + '.jpeg')
