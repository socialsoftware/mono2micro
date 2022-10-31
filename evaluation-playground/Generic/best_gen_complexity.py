from os import mkdir, listdir
from os.path import isfile, join, isdir
import numpy as np 
from matplotlib import pyplot as plt
import matplotlib.patches as mpatches
import scipy.stats as stats

import requests
import json
import pandas as pd
import seaborn as sn

plt.style.use('seaborn-whitegrid')

API_URL = "http://localhost:8080/mono2micro/"
RESULTS_PATH = "../results"
EVALUATION_PATH = "../evaluation/GEN"

def welchs_ttest(data, filename):
	strategies = ['CA', 'FAEA', 'FAMC', 'EA','SA']
	with open(EVALUATION_PATH + '/' + filename, 'w') as file:
		lines = []
		for nc in range(3, 11):
			i = 0
			while i < len(strategies):
				j = i+1
				while j < len(strategies):
					lines.append(f'NC: {nc} -> S: {strategies[i]} vs {strategies[j]} -> Test: {stats.ttest_ind(data[nc][strategies[i]], data[nc][strategies[j]], equal_var = False)}\n')
					j += 1
				i += 1
		file.writelines(lines)

def best_general_metrics():

	data = {
		3: { 'CA': {}, 'EA': {}, 'FAEA': {}, 'FAMC': {}, 'SA': {} },
		4: { 'CA': {}, 'EA': {}, 'FAEA': {}, 'FAMC': {}, 'SA': {} },
		5: { 'CA': {}, 'EA': {}, 'FAEA': {}, 'FAMC': {}, 'SA': {} },
		6: { 'CA': {}, 'EA': {}, 'FAEA': {}, 'FAMC': {}, 'SA': {} },
		7: { 'CA': {}, 'EA': {}, 'FAEA': {}, 'FAMC': {}, 'SA': {} },
		8: { 'CA': {}, 'EA': {}, 'FAEA': {}, 'FAMC': {}, 'SA': {} },
		9: { 'CA': {}, 'EA': {}, 'FAEA': {}, 'FAMC': {}, 'SA': {} },
		10: { 'CA': {}, 'EA': {}, 'FAEA': {}, 'FAMC': {}, 'SA': {} }
	}

	codebases = [d for d in listdir(RESULTS_PATH) if not isfile(join(RESULTS_PATH, d))]

	i = 0

	for cb in codebases:
		cb_path = join(RESULTS_PATH, cb)

		i += 1
		print(f'Progress: {i}/{len(codebases)}')

		cb_path = join(RESULTS_PATH, cb)

		response = requests.get(url = API_URL + f"/codebase/{cb}/maxComplexity")
		response_body = response.json()
		max_complexity = response_body['maxComplexity']

		if max_complexity == 0:
			print("Max Complexity is 0...")

		for strategy in ['CA', 'FAEA', 'FAMC']:
			s_path = join(cb_path, strategy)
			result_file_path = join(s_path, "analyserResult.json")

			with open(result_file_path) as result_file:
				result = json.load(result_file)

				for key in result:
					if result[key]['numberOfEntitiesClusters'] > 2 and result[key]['numberOfEntitiesClusters'] < 11:
						if cb in data[result[key]['numberOfEntitiesClusters']][strategy]:
							if max_complexity != 0:
								if data[result[key]['numberOfEntitiesClusters']][strategy][cb] >= (result[key]['complexity'] / max_complexity):
									data[result[key]['numberOfEntitiesClusters']][strategy][cb] = result[key]['complexity'] / max_complexity
							else:
								data[result[key]['numberOfEntitiesClusters']][strategy][cb] = 0
						else:
							if max_complexity != 0:
								data[result[key]['numberOfEntitiesClusters']][strategy][cb] = result[key]['complexity'] / max_complexity
							else:
								data[result[key]['numberOfEntitiesClusters']][strategy][cb] = 0

		for strategy in ['EA','SA']:
			s_path = join(cb_path, strategy)
			result_file_path = join(s_path, "analyserResult.json")

			with open(result_file_path) as result_file:
				result = json.load(result_file)

				for key in result:
					if result[key]['numberClusters'] > 2 and result[key]['numberClusters'] < 11:
						if cb in data[result[key]['numberClusters']][strategy]:
							if max_complexity != 0:
								if data[result[key]['numberClusters']][strategy][cb] >= (result[key]['complexity'] / max_complexity):
									data[result[key]['numberClusters']][strategy][cb] = result[key]['complexity'] / max_complexity
							else:
								data[result[key]['numberClusters']][strategy][cb] = 0
						else:
							if max_complexity != 0:
								data[result[key]['numberClusters']][strategy][cb] = result[key]['complexity'] / max_complexity
							else:
								data[result[key]['numberClusters']][strategy][cb] = 0

	
	for nc in range(3, 11):
		for strategy in ['CA', 'FAEA', 'FAMC', 'EA', 'SA']:
			l = []
			for cb in codebases:
				if cb in data[nc][strategy]:
					l.append(data[nc][strategy][cb])
			data[nc][strategy] = l
	
	X = [k for k in range(3, 11)]
	X_axis = np.arange(len(X)) * 4

	width = 0.1
	beige_color  = dict(color='#EDC192')
	green_color  = dict(color='#928E5E')
	blue_color   = dict(color='#80C2CE')
	red_color    = dict(color='#E55D53')
	purple_color = dict(color='#BF80FF')

	colors = [blue_color, red_color, green_color, purple_color, beige_color]

	i = -1.5
	c = 0

	for strategy in ['CA', 'EA', 'FAEA', 'FAMC', 'SA']:
		s_data = [data[k][strategy] for k in range(3, 11)]

		flierprops = dict(marker='o', markersize=2, linestyle='none', markeredgecolor=colors[c]['color'])
		bp = plt.boxplot(s_data, meanline=False, showmeans=False, meanprops=dict(color='#C28C17'), medianprops=dict(color='#000000'), whiskerprops=colors[c], flierprops=flierprops, positions= X_axis + i, labels =[k for k in range(3, 11)], patch_artist=True) # , boxprops=colors[c]
		
		caps = bp['caps']
		for j in range(len(caps)):
			if (j % 2) == 0: caps[j].set(color=colors[c]['color']) # Low cap
			else: caps[j].set(color=colors[c]['color']) # High cap

		for patch in bp['boxes']:
			patch.set_facecolor('black')
			patch.set_color(colors[c]['color'])
		
		i += 0.6
		c += 1

	handles = []
	c = 0
	for strategy in ['CV', 'EV', 'FVSA', 'FVCG', 'SA']:
		handles.append(mpatches.Patch(color=colors[c]['color'], label=strategy))
		c += 1

	plt.legend(handles=handles, bbox_to_anchor=(1.05, 1.0), loc='upper left')
	plt.xticks(X_axis, X)
	plt.ylim(0, 1)
	plt.ylabel("Complexity")
	plt.xlabel("Number of Clusters")
	plt.title("Best Complexity Values by strategy")
	plt.savefig(EVALUATION_PATH + "/best_general_complexity.png", format="png", bbox_inches='tight')

	# Welch's t-Test
	welchs_ttest(data, 'best_general_complexity.txt')


best_general_metrics()
