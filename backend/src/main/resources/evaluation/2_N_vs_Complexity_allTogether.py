import pandas as pd
from os import walk
import plotly.express as px
import statsmodels.api as sm

# Plot that compares the number of clusters of each decomposition
# with its complexity
# And a plot with trendlines of the min max and median values of each Quartile

def find_min(sorted_list):
    return sorted_list[0]


def find_max(sorted_list):
    return sorted_list[len(sorted_list) - 1]


def find_median(sorted_list):
    indices = []

    list_size = len(sorted_list)
    median = 0

    if list_size % 2 == 0:
        indices.append(int(list_size / 2) - 1)  # -1 because index starts from 0
        indices.append(int(list_size / 2))

        median = (sorted_list[indices[0]] + sorted_list[indices[1]]) / 2
        pass
    else:
        indices.append(int(list_size / 2))

        median = sorted_list[indices[0]]
        pass

    return median, indices
    pass


def getMinMedianAndMax(elementsList):
    samples = sorted(elementsList)
    median, median_indices = find_median(samples)
    minV = find_min(samples)
    maxV = find_max(samples)
    return [minV, median, maxV]


csv_file_names = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    csv_file_names.extend(filenames)
    break

x_n = []
y_pComplexity = []
colors_all = []  # min, med, max

df = {
    'n': [],
    'pComplexity': [],
    'hover': []
}

for csv_file_name in csv_file_names:
    print(csv_file_name)

    data = pd.read_csv("./data/" + csv_file_name)

    dict1 = {}
    for entry in data.values:
        if entry[0] not in dict1:
            dict1[entry[0]] = []
        dict1[entry[0]].append(entry[8])

        df['n'].append(entry[0])
        df['pComplexity'].append(entry[8])
        df['hover'].append(csv_file_name)

    x_data = list(dict1.keys())
    y_data = []
    for i in x_data:
        y = dict1[i]
        y_data.append(y)

    # xd = N     yd = complexity points for that n in this codebase
    for xd, yd in zip(x_data, y_data):
        var = getMinMedianAndMax(yd)

        x_n.append(xd)
        y_pComplexity.append(var[0])
        colors_all += ['min']

        x_n.append(xd)
        y_pComplexity.append(var[1])
        colors_all += ['med']

        x_n.append(xd)
        y_pComplexity.append(var[2])
        colors_all += ['max']

boxFig = px.box(
    df,
    x="n",
    y="pComplexity",
    hover_name='hover',
    title="N vs PComplexity",
    points="all",
    labels={"n": "Number Of Clusters", "pComplexity": "Pondered Complexity"}
)
boxFig.update_traces(marker=dict(size=2))
boxFig.show()

fig1 = px.scatter(
    x=x_n,
    y=y_pComplexity,
    color=colors_all,
    labels={'x': 'N', 'y': 'PComplexity'},
    range_y=[0, 1],
    title="N x PComplexity (min, med and max regressions)",
    trendline="ols"
)
fig1.show()

