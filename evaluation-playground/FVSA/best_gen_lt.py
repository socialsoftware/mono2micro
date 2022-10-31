from os import listdir, mkdir
from os.path import isfile, join, isdir
from matplotlib import pyplot as plt
import matplotlib.patches as mpatches
import scipy.stats as stats
import pandas as pd
import statsmodels.api as sm
import numpy as np 
import requests
import json

API_URL = "http://localhost:8080/mono2micro/"
MAX_CLUSTER_SIZE = 10

RESULTS_PATH = "../results"
EVALUATION_PATH = "../evaluation/FVSA"

plt.style.use('seaborn-whitegrid')

def welchs_ttest(data, filename):
	linkageTypes = ['average', 'single', 'complete']
	with open(EVALUATION_PATH + '/' + filename, 'w') as file:
		lines = []
		for nc in range(3, 11):
			i = 0
			while i < len(linkageTypes):
				j = i+1
				while j < len(linkageTypes):
					lines.append(f'NC: {nc} -> S: {linkageTypes[i]} vs {linkageTypes[j]} -> Test: {stats.ttest_ind(data[nc][linkageTypes[i]], data[nc][linkageTypes[j]], equal_var = False)}\n')
					j += 1
				i += 1
		file.writelines(lines)

def evaluation(strategy):

	codebases = [d for d in listdir(RESULTS_PATH) if not isfile(join(RESULTS_PATH, d))]

	data = {
		3: { 'average': {}, 'single': {}, 'complete': {} },
		4: { 'average': {}, 'single': {}, 'complete': {} },
		5: { 'average': {}, 'single': {}, 'complete': {} },
		6: { 'average': {}, 'single': {}, 'complete': {} },
		7: { 'average': {}, 'single': {}, 'complete': {} },
		8: { 'average': {}, 'single': {}, 'complete': {} },
		9: { 'average': {}, 'single': {}, 'complete': {} },
		10: { 'average': {}, 'single': {}, 'complete': {} }
	}

	counter = 1
	lcb = len(codebases)
	for cb in codebases:
		print(f'Progress: {counter}/{lcb}, Codebase: {cb}')

		cb_path = join(RESULTS_PATH, cb)
		s_path = join(cb_path, strategy)
		results_file_path = join(s_path, "analyserResult.json")

		response = requests.get(url = API_URL + f"/codebase/{cb}/maxComplexity")
		response_body = response.json()
		max_complexity = response_body['maxComplexity']

		with open(results_file_path) as results_file:
			result = json.load(results_file)

			for key in result:
				lt = result[key]['linkageType']
				if result[key]['numberOfEntitiesClusters'] > 2 and result[key]['numberOfEntitiesClusters'] < 11:
					tc = 1.1
					cohesion = result[key]['cohesion']
					coupling = result[key]['coupling']
					if max_complexity != 0:
						tc = (1 + result[key]['complexity'] / max_complexity + coupling - cohesion) / 3
					else:
						tc = (1 + coupling - cohesion) / 3

					if (cb in data[result[key]['numberOfEntitiesClusters']][lt]):
						if data[result[key]['numberOfEntitiesClusters']][lt][cb] > tc:
							data[result[key]['numberOfEntitiesClusters']][lt][cb] = tc
					else:
						data[result[key]['numberOfEntitiesClusters']][lt][cb] = tc
	
		counter += 1

	for n in range(3, 11):
		for lt in ['average', 'single', 'complete']:
			l = [data[n][lt][cb] for cb in data[n][lt]]
			data[n][lt] = l

	X = [k for k in range(3, 11)]
	X_axis = np.arange(len(X)) * 4

	width = 0.1
	blue_color   = dict(color='#80C2CE')
	red_color    = dict(color='#E55D53')
	green_color  = dict(color='#928E5E')

	colors = [blue_color, red_color, green_color]

	i = -0.6
	c = 0

	for lt in ['average', 'single', 'complete']:
		s_data = [data[k][lt] for k in range(3, 11)]

		flierprops = dict(marker='o', markersize=2, linestyle='none', markeredgecolor=colors[c]['color'])

		bp = plt.boxplot(s_data, meanline=False, showmeans=False, medianprops=dict(color='#000000'), whiskerprops=colors[c], flierprops=flierprops, positions= X_axis + i, labels =[k for k in range(3, 11)], patch_artist=True)
		
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
	for lt in ['average', 'single', 'complete']:
		handles.append(mpatches.Patch(color=colors[c]['color'], label=lt))
		c += 1

	plt.legend(handles=handles, bbox_to_anchor=(1.05, 1.0), loc='upper left')

	plt.xticks(X_axis, X)
	plt.ylim(0, 1)
	plt.ylabel("Combined metric")
	plt.xlabel("Number of Clusters")
	plt.title(f"FVSA Combined metric by Linkage Type")
	plt.savefig(EVALUATION_PATH + f"/best_gen_fvsa_mix_lt.png", format="png", bbox_inches='tight')
	plt.clf()

	# Welch's t-Test
	welchs_ttest(data, 'best_gen_fvsa_mix_lt.txt')
	

evaluation('FAEA')