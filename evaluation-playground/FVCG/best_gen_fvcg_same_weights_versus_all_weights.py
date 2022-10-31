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

def welchs_ttest(data, filename):
	wt = ['same_weights', 'diff_weights']
	with open(EVALUATION_PATH + '/' + filename, 'w') as file:
		lines = []
		for nc in range(3, 11):
			i = 0
			while i < len(wt):
				j = i+1
				while j < len(wt):
					lines.append(f'NC: {nc} -> S: {wt[i]} vs {wt[j]} -> Test: {stats.ttest_ind(data[nc][wt[i]], data[nc][wt[j]], equal_var = False)}\n')
					j += 1
				i += 1
		file.writelines(lines)

def evaluation(strategy):

	codebases = [d for d in listdir(RESULTS_PATH) if not isfile(join(RESULTS_PATH, d))]

	df = {
		'n': [],
		'cb': [],
		'cohesion': [],
		'coupling': [],
		'complexity': [],
		'combined': [],
		'linkageType' : [],
		'controllersWeight': [],
		'entitiesWeight': [],
		'servicesWeight': [],
		'intermediateMethodsWeight': [],
		'maxDepth': []
	}
	counter = 1
	lcb = len(codebases)
	for cb in codebases:
		print(f'Progress: {counter}/{lcb}, Codebase: {cb}')
		counter += 1
		
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
				n = body['numberOfEntitiesClusters']

				if n > 2 and n < 11 and body['maxDepth'] == 2:
					cohesion = body['cohesion']
					coupling = body['coupling']
					df['n'].append(n)
					df['cb'].append(cb)
					df['complexity'].append(uniform_complexity)
					df['cohesion'].append(cohesion)
					df['coupling'].append(coupling)
					df['combined'].append((1 + uniform_complexity + coupling - cohesion) / 3)
					df['controllersWeight'].append(body['controllersWeight'])
					df['entitiesWeight'].append(body['entitiesWeight'])
					df['servicesWeight'].append(body['servicesWeight'])
					df['intermediateMethodsWeight'].append(body['intermediateMethodsWeight'])
					df['maxDepth'].append(body['maxDepth'])
					if (linkageType == 'single'):
						df['linkageType'].append(1)
					elif (linkageType == 'average'):
						df['linkageType'].append(2)
					else:
						df['linkageType'].append(3)

	tmp_data = { 'same_weights': {}, 'diff_weights': {} }
	for n in range(3, 11):
		tmp_data['same_weights'][n] = {}
		tmp_data['diff_weights'][n] = {}

	for i in range(len(df['n'])):
		if df['controllersWeight'][i] == 25 and df['entitiesWeight'][i] == 25 and df['servicesWeight'][i] == 25 and df['intermediateMethodsWeight'][i] == 25:
			if (not (df['cb'][i] in tmp_data['same_weights'][df['n'][i]])):
				tmp_data['same_weights'][df['n'][i]][df['cb'][i]] = { 'combined': 1.1 }
			if df['combined'][i] < tmp_data['same_weights'][df['n'][i]][df['cb'][i]]['combined']:
				tmp_data['same_weights'][df['n'][i]][df['cb'][i]]['combined'] = df['combined'][i]
		else:
			if (not (df['cb'][i] in tmp_data['diff_weights'][df['n'][i]])):
				tmp_data['diff_weights'][df['n'][i]][df['cb'][i]] = { 'combined': 1.1 }
			if df['combined'][i] < tmp_data['diff_weights'][df['n'][i]][df['cb'][i]]['combined']:
				tmp_data['diff_weights'][df['n'][i]][df['cb'][i]]['combined'] = df['combined'][i]

	data = {}
	for n in range(3, 11):
		data[n] = {}

	for w in ['same_weights', 'diff_weights']:
		for n in tmp_data[w]:
			if (not (w in data[n])):
				data[n][w] = []
			for cb in tmp_data[w][n]:
				data[n][w].append(tmp_data[w][n][cb]['combined'])

	X = [k for k in range(3, 11)]
	X_axis = np.arange(len(X)) * 4

	width = 0.1
	light_red_color  = dict(color='#E3502B')
	green_color      = dict(color='#98AD49')

	colors = [green_color, light_red_color]

	i = -0.3
	c = 0

	for w in ['same_weights', 'diff_weights']:
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
	for w in ['Same weights', 'Mixed weights']:
		handles.append(mpatches.Patch(color=colors[c]['color'], label=f'{w}'))
		c += 1

	plt.legend(handles=handles, bbox_to_anchor=(1.05, 1.0), loc='upper left')

	plt.xticks(X_axis, X)
	plt.ylim(0, 1)
	plt.xlabel("Number of clusters")
	plt.ylabel("Combined")
	plt.title(f"FVCG Combined Metric comparison of Same weights with Mixed weights")
	plt.savefig(EVALUATION_PATH + f"/best_gen_fvcg_md2_sw_vs_mw.png", format="png", bbox_inches='tight', figsize=(60,20))
	plt.clf()

	# Welch's t-Test
	welchs_ttest(data, 'best_gen_fvcg_same_weights_vx_mixed_weights.txt')

evaluation('FAMC')