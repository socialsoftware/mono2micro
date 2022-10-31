from os import listdir, mkdir
from os.path import isfile, join, isdir
from matplotlib import pyplot as plt
import pandas as pd
import statsmodels.api as sm
import matplotlib.patches as mpatches
import scipy.stats as stats
import numpy as np 
import requests
import json

API_URL = "http://localhost:8080/mono2micro/"
MAX_CLUSTER_SIZE = 10

RESULTS_PATH = "../results"
EVALUATION_PATH = "../evaluation/FVCG"

plt.style.use('seaborn-whitegrid')

strategies_translator = { 'CA' : 'AM', 'FAMC' : 'FVMC', 'FAEA' : 'FVET', 'EA' : 'EV', 'SA' : 'MM' }

def evaluation_combined_md(strategy):

	codebases = [d for d in listdir(RESULTS_PATH) if not isfile(join(RESULTS_PATH, d))]

	df = {
		'n': [],
		'cb': [],
		'combined': [],
		'cw': [],
		'ew': [],
		'sw': [],
		'iw': []
	}
	counter = 1
	lcb = len(codebases)
	for cb in codebases:
		print(f'Progress: {counter}/{lcb}, Codebase: {cb}')
		
		cb_path = join(RESULTS_PATH, cb)
		s_path = join(cb_path, strategy)
		results_file_path = join(s_path, "analyserResult.json")

		with open(results_file_path) as results_file:
			results = json.load(results_file)

			response = requests.get(url = API_URL + f"/codebase/{cb}/maxComplexity")
			response_body = response.json()
			max_complexity = response_body['maxComplexity']

			for metrics in results.keys():
				weights = metrics.split(',')
				linkageType = weights[0]
				body = results[metrics]
				complexity = body['complexity']
				uniform_complexity = 0
				if max_complexity != 0:
					uniform_complexity = complexity / max_complexity
				n = body['numberOfEntitiesClusters']
				maxDepth = body['maxDepth']

				if n > 2 and n < 11 and maxDepth == 2:
					cohesion = body['cohesion']
					coupling = body['coupling']
					df['n'].append(n)
					df['cb'].append(counter-1)
					df['combined'].append((1 + uniform_complexity + coupling - cohesion) / 3)
					df['cw'].append(body['controllersWeight'])
					df['sw'].append(body['servicesWeight'])
					df['iw'].append(body['intermediateMethodsWeight'])
					df['ew'].append(body['entitiesWeight'])
		counter += 1

	
	# Filter the best 85 decompositions for number of clusters and depth
	tmp_data = {}
	for n in range(3, 11):
		tmp_data[n] = {}

	for i in range(len(df['n'])):
		if (not (df['cb'][i] in tmp_data[df['n'][i]])):
			tmp_data[df['n'][i]][df['cb'][i]] = { 'comb': 1.1, 'cw': 0, 'sw': 0, 'iw': 0, 'ew': 0 }
		if df['combined'][i] < tmp_data[df['n'][i]][df['cb'][i]]['comb']:
			tmp_data[df['n'][i]][df['cb'][i]]['comb'] = df['combined'][i]
			tmp_data[df['n'][i]][df['cb'][i]]['cw'] = df['cw'][i]
			tmp_data[df['n'][i]][df['cb'][i]]['sw'] = df['sw'][i]
			tmp_data[df['n'][i]][df['cb'][i]]['iw'] = df['iw'][i]
			tmp_data[df['n'][i]][df['cb'][i]]['ew'] = df['ew'][i]
	
	data = {}
	for n in range(3, 11):
		data[n] = {}

	d = 2

	for n in tmp_data:
		for w in ['cw', 'sw', 'iw', 'ew']:
			if (not (d in data[n])):
				data[n][w] = []
			for cb in tmp_data[n]:
				data[n][w].append(tmp_data[n][cb][w])

	X = [k for k in range(3, 11)]
	X_axis = np.arange(len(X)) * 4

	width = 0.1
	red_color        = dict(color='#D62424')
	light_red_color  = dict(color='#E3502B')
	orange_color     = dict(color='#F28F38')
	beige_color      = dict(color='#EDC192')
	green_color      = dict(color='#98AD49')
	dark_green_color = dict(color='#2C871A')

	colors = [dark_green_color, green_color, beige_color, orange_color, light_red_color, red_color]

	i = -1.2
	c = 0

	for w in ['cw', 'sw', 'iw', 'ew']:
		s_data = np.array([data[k][w] for k in range(3, 11)])

		flierprops = dict(marker='o', markersize=2, linestyle='none', markeredgecolor=colors[c]['color'])

		bp = plt.boxplot(s_data, meanline=False, showmeans=False, medianprops=dict(color='#000000'), whiskerprops=colors[c], flierprops=flierprops, positions=X_axis + i, labels=[k for k in range(3, 11)], patch_artist=True)

		caps = bp['caps']
		for j in range(len(caps)):
			if (j % 2) == 0: caps[j].set(color=colors[c]['color']) # Low cap
			else: caps[j].set(color=colors[c]['color']) # High cap

		for patch in bp['boxes']:
			patch.set_color(colors[c]['color'])
		
		i += 0.6
		c += 1

	handles = []
	c = 0
	for w in ['cw', 'sw', 'iw', 'ew']:
		handles.append(mpatches.Patch(color=colors[c]['color'], label=f'{w}'))
		c += 1

	plt.legend(handles=handles, bbox_to_anchor=(1.05, 1.0), loc='upper left')

	plt.xticks(X_axis, X)
	plt.ylim(0, 100)
	plt.xlabel("Number of clusters")
	plt.ylabel("Weights")
	plt.title(f"FVCG Weights by Number of Clusters when the depth is 2")
	plt.savefig(EVALUATION_PATH + f"/best_gen_fvcg_md2_weights.png", format="png", bbox_inches='tight', figsize=(60,20))
	plt.clf()

evaluation_combined_md('FAMC')
