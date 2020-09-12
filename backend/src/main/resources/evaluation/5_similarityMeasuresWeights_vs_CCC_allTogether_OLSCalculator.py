from os import walk

import pandas as pd
import statsmodels.api as sm


# OLS Regression Model calculator
# Estimates the complexity values based on the N value and
# the weights given to each similarity measure

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
    'complexity': [],
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
        df['complexity'].append(entry[8])

df = pd.DataFrame(df)

X = df.loc[:, ['n', 'A', 'W', 'R', 'S']]
y = df.loc[:, 'complexity']
X = sm.add_constant(X)
model = sm.OLS(y, X)
results = model.fit()
print(results.summary())
print()

X = df.loc[:, ['n', 'A', 'W', 'R', 'S']]
y = df.loc[:, 'coupling']
X = sm.add_constant(X)
model = sm.OLS(y, X)
results = model.fit()
print(results.summary())
print()

X = df.loc[:, ['n', 'A', 'W', 'R', 'S']]
y = df.loc[:, 'cohesion']
X = sm.add_constant(X)
model = sm.OLS(y, X)
results = model.fit()
print(results.summary())
print()
