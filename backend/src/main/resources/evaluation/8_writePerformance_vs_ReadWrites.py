import json
from os import walk

import pandas as pd
import plotly.express as px
import statsmodels.api as sm


# Studies the relation between the performance of the Write similarity measure
# with the number of Write Accesses that the datafile has

# OLS Regression Model calculator
# Estimates the performance of the Write Similarity measure
# given the percentage of write accesses the datafile has

df = {
    'n': [],
    'wSlope': [],
    'writePercentage': [],
    'hover': []
}

files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break

for file in files:
    print(file)

    data = pd.read_csv("./data/" + file)

    for n in range(3, 11):

        temp = {
            'writeWeight': [],
            'pComplexity': []
        }

        for entry in data.values:
            if entry[0] != n:
                continue

            temp['writeWeight'].append(entry[2])
            temp['pComplexity'].append(entry[8])

        temp = pd.DataFrame(temp)

        try:
            fig = px.scatter(
                temp,
                x='writeWeight',
                y='pComplexity',
                range_y=[0, 1],
                trendline='ols'
            )
            # fig.show()
            results = px.get_trendline_results(fig)
            slope = results.px_fit_results.iloc[0].params[1]
            # cons = results.px_fit_results.iloc[0].params[0]

            accessesCount = 0
            writes = 0
            parsedFileName = "_".join(file.split("_")[2:])
            parsedFileName = parsedFileName[0:len(parsedFileName) - 4]
            with open('../codebases/' +
                      parsedFileName + '/datafile.json') as f:

                dataFile = json.load(f)
                for entry in dataFile.values():
                    for accessList in entry:
                        accessesCount += 1
                        if accessList[1] == 'W':
                            writes += 1

            df['n'].append(n)
            df['wSlope'].append(slope)
            df['writePercentage'].append(writes / accessesCount)
            df['hover'].append(parsedFileName)

        except Exception:
            pass

df = pd.DataFrame(df)

X = df.loc[:, ['n', 'writePercentage']]
y = df.loc[:, 'wSlope']
X = sm.add_constant(X)
model = sm.OLS(y, X)
results = model.fit()
print(results.summary())
print()

X = df.loc[:, ['writePercentage']]
y = df.loc[:, 'wSlope']
X = sm.add_constant(X)
model = sm.OLS(y, X)
results = model.fit()
print(results.summary())
print()

df['n'] = df['n'].astype(str)
fig = px.scatter(
    df,
    x='writePercentage',
    y='wSlope',
    color='n',
    hover_name='hover',
    trendline='ols',
    range_y=[-0.007, 0.01]
)
fig.show()
