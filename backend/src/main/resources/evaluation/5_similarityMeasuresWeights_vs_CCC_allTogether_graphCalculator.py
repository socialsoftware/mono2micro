from os import walk

import pandas as pd
import plotly
import plotly.express as px
from pathlib import Path

home = str(Path.home())
plotly.io.orca.config.executable = home + '/anaconda3/bin/orca'

# Plot that studies the performances of each similarity measure individually
# For each N value, compares the weights given to a similarity measure with the
# complexity obtained for the decomposition

def iToChar(i):
    if i == 1:
        return 'A'
    if i == 2:
        return 'W'
    if i == 3:
        return 'R'
    if i == 4:
        return 'S'


df = {
    'n': [],
    'weight': [],
    'complexity': [],
    'coupling': [],
    'cohesion': [],
    'measure': [],
}

files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break

for file in files:
    data = pd.read_csv("./data/" + file)
    for entry in data.values:
        for i in range(1, 5):  # A, W, R, S
            df['n'].append(entry[0])
            df['weight'].append(entry[i])
            df['complexity'].append(entry[8])
            df['coupling'].append(entry[6])
            df['cohesion'].append(entry[5])
            df['measure'].append(iToChar(i))

df = pd.DataFrame(df)
for i in range(3, 11):
    df_n = df[df.n == i]
    # fig1 = px.scatter(
    #     df_n,
    #     x='weight',
    #     y='cohesion',
    #     color='measure',
    #     title="N = " + str(i),
    #     labels={"x": "Weight", "y": "Cohesion"},
    #     range_y=[0, 1],
    #     trendline='ols',
    # )
    # fig1.show()
    # # fig1.write_html('cohesion' + str(i))
    #
    # fig2 = px.scatter(
    #     df_n,
    #     x='weight',
    #     y='coupling',
    #     color='measure',
    #     title="N = " + str(i),
    #     labels={"x": "Weight", "y": "Coupling"},
    #     range_y=[0, 1],
    #     trendline='ols',
    # )
    # fig2.show()
    # # fig2.write_html('coupling' + str(i))
    #
    # fig3 = px.scatter(
    #     df_n,
    #     x='weight',
    #     y='complexity',
    #     color='measure',
    #     title="N = " + str(i),
    #     labels={"x": "Weight", "y": "Complexity"},
    #     range_y=[0, 1],
    #     trendline='ols',
    # )
    # fig3.show()
    # # fig3.write_html('complexity' + str(i))

    # box plot style
    boxFig = px.box(
        df_n,
        x="weight",
        y="complexity",
        color="measure",
        range_y=[0, 1],
        title="Comparison between the weights given to the measures and the complexity achieved for N = " + str(i),
        labels={
            'complexity': 'Uniform Complexity',
            "weight": "Measure Weight"
        }
    )
    boxFig.show()
    # boxFig.write_html('similarityMeasuresWeightsVsComplexity_N' + str(i) + '.html')

    # boxFig = px.box(
    #     df_n,
    #     x="weight",
    #     y="coupling",
    #     color="measure",
    #     range_y=[0, 1],
    #     title="Comparison between the weights given to the measures and the coupling achieved for N = " + str(i),
    #     labels={
    #         'coupling': 'Coupling',
    #         "weight": "Measure Weight"
    #     }
    # )
    # boxFig.show()
    # # boxFig.write_html('similarityMeasuresWeightsVsCoupling_N' + str(i) + '.html')
    #
    # boxFig = px.box(
    #     df_n,
    #     x="weight",
    #     y="cohesion",
    #     color="measure",
    #     range_y=[0, 1],
    #     title="Comparison between the weights given to the measures and the cohesion achieved for N = " + str(i),
    #     labels={
    #         'cohesion': 'Cohesion',
    #         "weight": "Measure Weight"
    #     }
    # )
    # boxFig.show()
    # # boxFig.write_html('similarityMeasuresWeightsVsCohesion_N' + str(i) + '.html')
