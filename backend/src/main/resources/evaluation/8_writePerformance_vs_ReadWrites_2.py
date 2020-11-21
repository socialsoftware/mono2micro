from os import walk
import json
import pandas as pd
import statsmodels.api as sm
import plotly.express as px


# OLS Regression Model calculator
# Estimates the complexity values based on the N value and
# the weights given to each similarity measure

files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break


df_writes = {
    'writePercentage': [],
    'wPerformance': [],
    'file': []
}
for file in files:
    print(file)
    data = pd.read_csv("./data/" + file)

    df = {
        'n': [],
        'A': [],
        'W': [],
        'R': [],
        'S': [],
        'complexity': [],
    }

    for entry in data.values:
        df['n'].append(entry[0])
        df['A'].append(entry[1])
        df['W'].append(entry[2])
        df['R'].append(entry[3])
        df['S'].append(entry[4])
        df['complexity'].append(entry[8])

    df = pd.DataFrame(df)

    X = df.loc[:, ['n', 'A', 'W', 'R', 'S']]
    y = df.loc[:, 'complexity']
    X = sm.add_constant(X)
    model = sm.OLS(y, X)
    results = model.fit()
    # print(results.summary())
    # print()

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

    df_writes['wPerformance'].append(results.params['W'])
    df_writes['writePercentage'].append(writes / accessesCount)
    df_writes['file'].append(parsedFileName)

df_writes = pd.DataFrame(df_writes)
X = df_writes.loc[:, ['writePercentage']]
y = df_writes.loc[:, 'wPerformance']
X = sm.add_constant(X)
model = sm.OLS(y, X)
results = model.fit()
print(results.summary())
print()

fig = px.scatter(
    df_writes,
    x='writePercentage',
    y='wPerformance',
    hover_name='file',
    trendline='ols',
)
fig.show()
