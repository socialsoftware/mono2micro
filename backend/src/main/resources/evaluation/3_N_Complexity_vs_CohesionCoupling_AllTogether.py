from os import walk
import pandas as pd
import statsmodels.api as sm
import plotly.express as px

# Plots the relation between Complexity and Coupling and Complexity and Cohesion
# for different values of N

# OLS Regression Model calculator
# Estimates the coupling values based on the N and Complexity
# Estimates the cohesion values based on the N and Complexity

files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break

df = {
    'n': [],
    'coup': [],
    'coh': [],
    'pComplexity': []
}

for file in files:
    data = pd.read_csv("./data/" + file)

    for entry in data.values:
        df['n'].append(entry[0])
        df['coh'].append(entry[5])
        df['coup'].append(entry[6])
        df['pComplexity'].append(entry[8])

df = pd.DataFrame(df)


# percentage of points with less or equal 0.2 Pondered Complexity for N = x
# print((df[(df['n'] == 3) & (df['pComplexity'] <= 0.2)].count()) / (df[(df['n'] == 3)].count()))
# print((df[(df['n'] == 6) & (df['pComplexity'] <= 0.2)].count()) / (df[(df['n'] == 5)].count()))
# print((df[(df['n'] == 9) & (df['pComplexity'] <= 0.2)].count()) / (df[(df['n'] == 10)].count()))

df['n'] = df['n'].astype(str)
fig1 = px.scatter(
    df,
    x='pComplexity',
    y='coup',
    color='n',
    title="Complexity X Coupling",
    range_y=[0, 1],
    trendline="ols",
    labels={'pComplexity': 'Uniform Complexity', 'coup': 'Coupling'}
)
fig1.show()

fig1 = px.scatter(
    df,
    x='pComplexity',
    y='coh',
    color='n',
    title="Complexity X Cohesion",
    range_y=[0, 1],
    trendline="ols",
    labels={'pComplexity': 'Uniform Complexity', 'coh': 'Cohesion'}
)
fig1.show()

df['n'] = df['n'].astype(float)

# Cohesion OLS Regression
X = df.loc[:, ['n', 'pComplexity']]
y = df.loc[:, 'coh']
X = sm.add_constant(X)
model = sm.OLS(y, X)
results = model.fit()
print(results.summary())
print()


# Coupling OLS Regression
X = df.loc[:, ['n', 'pComplexity']]
y = df.loc[:, 'coup']
X = sm.add_constant(X)
model = sm.OLS(y, X)
results = model.fit()
print(results.summary())
print()

