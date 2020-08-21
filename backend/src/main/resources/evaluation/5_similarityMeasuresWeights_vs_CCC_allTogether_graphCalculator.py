from os import walk

import pandas as pd
import plotly.express as px

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
for i in [3, 6, 10]:
    df_n = df[df.n == i]
    # fig1 = px.scatter(
    #     df_n,
    #     x='weight',
    #     y='cohesion',
    #     color='metric',
    #     title="N = " + str(i),
    #     labels={"x": "Weight", "y": "Cohesion"},
    #     range_y=[0, 1],
    #     trendline='lowess',
    # )
    # fig1.show()
    #
    # fig2 = px.scatter(
    #     df_n,
    #     x='weight',
    #     y='coupling',
    #     color='metric',
    #     title="N = " + str(i),
    #     labels={"x": "Weight", "y": "Coupling"},
    #     range_y=[0, 1],
    #     trendline='lowess',
    # )
    # fig2.show()

    # fig3 = px.scatter(
    #     df_n,
    #     x='weight',
    #     y='complexity',
    #     color='metric',
    #     title="N = " + str(i),
    #     labels={"x": "Weight", "y": "Complexity"},
    #     range_y=[0, 1],
    #     trendline='lowess',
    # )
    # fig3.show()

    # box plot style
    boxFig = px.box(
        df_n,
        x="weight",
        y="complexity",
        color="measure",
        title="N = " + str(i),
        labels={"complexity": "Weighted Complexity", "weight": "Measure Weight"}
    )
    boxFig.show()

    boxFig = px.box(df_n, x="weight", y="coupling", color="measure", title="N = " + str(i))
    boxFig.show()

    boxFig = px.box(df_n, x="weight", y="cohesion", color="measure", title="N = " + str(i))
    boxFig.show()
