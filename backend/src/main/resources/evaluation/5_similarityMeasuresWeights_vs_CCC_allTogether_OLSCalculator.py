from os import walk
import pandas as pd
import plotly
import statsmodels.api as sm

plotly.io.orca.config.executable = '~/anaconda3/bin/orca'

def iToChar(i):
    if i == 1:
        return 'A'
    if i == 2:
        return 'W'
    if i == 3:
        return 'R'
    if i == 4:
        return 'S'


files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break

df = {
    'n': [],
    'A': [],
    'W': [],
    'R': [],
    'S': [],
    'pComplexity': [],
    'coupling': [],
    'cohesion': [],
}

for file in files:
    print(file)
    data = pd.read_csv("./data/" + file)
    for entry in data.values:
        df['n'].append(entry[0])
        df['A'].append(entry[1])
        df['W'].append(entry[2])
        df['R'].append(entry[3])
        df['S'].append(entry[4])
        df['cohesion'].append(entry[5])
        df['coupling'].append(entry[6])
        df['pComplexity'].append(entry[8])

df = pd.DataFrame(df)

X = df.loc[:, ['n', 'A', 'W', 'R', 'S']]
y = df.loc[:, 'pComplexity']
X = sm.add_constant(X)
model = sm.OLS(y, X)
results = model.fit()
print(results.summary())
print()
