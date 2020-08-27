import pandas as pd
import plotly
import plotly.express as px
from pathlib import Path
import numpy as np

home = str(Path.home())
plotly.io.orca.config.executable = home + '/anaconda3/bin/orca'

file = '/home/samuel/Desktop/lineCounter/codebasesMetadata.csv'
data = pd.read_csv(file)

fig1 = px.scatter(
    data,
    x='domainEntities',
    y='controllerMethods',
    hover_name='codebase',
    title="Codebases Metadata",
    log_x=True,
    log_y=True,
    labels={"domainEntities": "#DomainEntities", "controllerMethods": "#Controllers"},
)
fig1.update_layout(
    xaxis={'dtick': 1},
    yaxis={'dtick': 1}
)
fig1.show()

print(str(round(np.mean(data['LOC']), 2)))
print(str(round(np.std(data['LOC']), 2)))
