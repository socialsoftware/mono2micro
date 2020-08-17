from os import walk

import pandas as pd
import plotly.express as px

files = []
for (dirpath, dirnames, filenames) in walk("./data/"):
    files.extend(filenames)
    break

x_data = []  # number of entities
y_data = []  # average decomposition complexity

for file in files:
    print(file)

    numberOfEntities = int(file.split("_")[0])
    x_data.append(numberOfEntities)

    data = pd.read_csv("./data/" + file)
    total = 0
    count = 0
    for entry in data.values:
        total += entry[8]  # pComplexity
        count += 1
    avg = total/count
    y_data.append(avg)

fig = px.scatter(x=x_data, y=y_data, title="Number of Entities vs AvgComplexity",
                 labels={'x': 'Number of Entities', 'y': 'Average Complexity'})
fig.show()
