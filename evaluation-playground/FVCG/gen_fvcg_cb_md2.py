from os import listdir, mkdir
from os.path import isfile, join, isdir
from matplotlib import pyplot as plt
import pandas as pd
import statsmodels.api as sm
import matplotlib.patches as mpatches
import numpy as np 
import requests
import json

API_URL = "http://localhost:8080/mono2micro/"

RESULTS_PATH = "../results"
EVALUATION_PATH = "../evaluation/FVCG"

plt.style.use('seaborn-whitegrid')

def best_evaluation_cb(strategy):

	codebases = [d for d in listdir(RESULTS_PATH) if not isfile(join(RESULTS_PATH, d))]

	df = {
		'n': [],
		'cb': [],
		'combined': [],
		'lt': []
	}
	counter = 1
	lcb = len(codebases)
	for cb in codebases:
		print(f'Progress: {counter}/{lcb}, Codebase: {cb}')

		cb_path = join(RESULTS_PATH, cb)
		s_path = join(cb_path, strategy)
		results_file_path = join(s_path, "analyserResult.json")

		with open(results_file_path) as results_file:
			stats = json.load(results_file)

			response = requests.get(url = API_URL + f"/codebase/{cb}/maxComplexity")
			response_body = response.json()
			max_complexity = response_body['maxComplexity']

			for metrics in stats.keys():
				weights = metrics.split(',')
				linkageType = weights[0]
				body = stats[metrics]
				complexity = body['complexity']
				uniform_complexity = 0
				if max_complexity != 0:
					uniform_complexity = complexity / max_complexity
				cohesion = body['cohesion']
				coupling = body['coupling']
				n = body['numberOfEntitiesClusters']
				maxDepth = body['maxDepth']
				linkageType = body['linkageType']

				if n > 2 and n < 11 and body['maxDepth'] == 2:
					df['n'].append(n)
					df['cb'].append(counter-1)
					df['combined'].append((1 + uniform_complexity + coupling - cohesion) / 3)
					df['lt'].append(linkageType)

		counter += 1

	tmp_data = {}
	for n in range(3, 11):
		tmp_data[n] = {}

	for i in range(len(df['n'])):
		if (not (df['cb'][i] in tmp_data[df['n'][i]])):
			tmp_data[df['n'][i]][df['cb'][i]] = { 'lt': df['lt'][i],  'combined': 1.1}
		if df['combined'][i] < tmp_data[df['n'][i]][df['cb'][i]]['combined']:
			tmp_data[df['n'][i]][df['cb'][i]]['combined'] = df['combined'][i]
			tmp_data[df['n'][i]][df['cb'][i]]['lt'] = df['lt'][i]

	tmp_df = {
			'n': [],
			'cb': [],
			'combined': [],
			'color': []
		}

	blue_color   = '#80C2CE'
	red_color    = '#E55D53'
	green_color  = '#928E5E'

	for n in tmp_data:
		for cb in tmp_data[n]:
			tmp_df['n'].append(n)
			tmp_df['cb'].append(cb)
			tmp_df['combined'].append(tmp_data[n][cb]['combined'])
			if tmp_data[n][cb]['lt'] == 'single':
				tmp_df['color'].append(red_color)
			elif tmp_data[n][cb]['lt'] == 'complete':
				tmp_df['color'].append(green_color)
			else:
				tmp_df['color'].append(blue_color)

	df = pd.DataFrame(tmp_df)

	X = df.loc[:, ['cb']]
	y = df.loc[:, 'combined']
	X = sm.add_constant(X)
	model = sm.OLS(y, X)
	results = model.fit()

	with open(EVALUATION_PATH + '/best_gen_FVCG_cb_md2.txt', 'w') as file:
		file.write(str(results.summary()))

	blue_color   = dict(color='#80C2CE')
	red_color    = dict(color='#E55D53')
	green_color  = dict(color='#928E5E')

	colors = [blue_color, red_color, green_color]

	handles = []
	c = 0
	for lt in ['average', 'single', 'complete']:
		handles.append(mpatches.Patch(color=colors[c]['color'], label=lt))
		c += 1

	sp = plt.scatter(df.cb, df.combined, s=3, color=df.color)
	plt.legend(handles=handles, bbox_to_anchor=(1.05, 1.0), loc='upper left')
	plt.xlabel('Codebases')
	plt.ylabel('Combined metric')
	plt.title('Combined metric versus Codebases')
	plt.xlim(-1, 86)
	plt.ylim(0, 1)
	plt.grid(True)
	plt.tight_layout()
	plt.savefig(EVALUATION_PATH + f"/best_gen_fvcg_cb_md2.png", format="png", bbox_inches='tight')
	plt.clf()

best_evaluation_cb('FAMC')