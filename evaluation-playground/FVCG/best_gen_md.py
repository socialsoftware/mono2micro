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

def calculate_OLS(X, y, nc):
	X = sm.add_constant(X)
	model = sm.OLS(y, X)
	results = model.fit()

	with open(EVALUATION_PATH + f'/best_gen_fvcg_md_nc{nc}.txt', 'w') as file:
		file.write(str(results.summary()))

def welchs_ttest(data, filename):
	depths = [1, 2, 3, 4, 5, 6]
	with open(EVALUATION_PATH + '/' + filename, 'w') as file:
		lines = []
		for nc in range(3, 11):
			i = 0
			while i < len(depths):
				j = i+1
				while j < len(depths):
					lines.append(f'NC: {nc} -> S: {depths[i]} vs {depths[j]} -> Test: {stats.ttest_ind(data[nc][depths[i]], data[nc][depths[j]], equal_var = False)}\n')
					j += 1
				i += 1
		file.writelines(lines)

def evaluation_combined_md(strategy):

	codebases = [d for d in listdir(RESULTS_PATH) if not isfile(join(RESULTS_PATH, d))]

	df = {
		'n': [],
		'cb': [],
		'maxDepth': [],
		'combined': []
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

				if n > 2 and n < 11:
					cohesion = body['cohesion']
					coupling = body['coupling']
					df['n'].append(n)
					df['cb'].append(counter-1)
					df['maxDepth'].append(maxDepth)
					df['combined'].append((1 + uniform_complexity + coupling - cohesion) / 3)
		counter += 1

	
	# Filter the best 85 decompositions for number of clusters and depth
	tmp_data = {}
	for n in range(3, 11):
		tmp_data[n] = {}

	for i in range(len(df['n'])):
		if (not (df['maxDepth'][i] in tmp_data[df['n'][i]])):
			tmp_data[df['n'][i]][df['maxDepth'][i]] = {}
		if (not (df['cb'][i] in tmp_data[df['n'][i]][df['maxDepth'][i]])):
			tmp_data[df['n'][i]][df['maxDepth'][i]][df['cb'][i]] = 1.1
		if df['combined'][i] < tmp_data[df['n'][i]][df['maxDepth'][i]][df['cb'][i]]:
			tmp_data[df['n'][i]][df['maxDepth'][i]][df['cb'][i]] = df['combined'][i]
	
	data = {}
	for n in range(3, 11):
		data[n] = {}

	for n in tmp_data:
		for d in tmp_data[n]:
			if (not (d in data[n])):
				data[n][d] = []
			for cb in tmp_data[n][d]:
				data[n][d].append(tmp_data[n][d][cb])

	for n in range(3, 11):
		df = {
			'combined': [],
			'depth': []
		}
		for d in data[n]:
			if d > 1:
				for val in tmp_data[n][d]:
					df['combined'].append(val)
					df['depth'].append(d)
		df = pd.DataFrame(df)
		calculate_OLS(df[['depth']], df['combined'], n)

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

	i = -1.5
	c = 0

	for d in [1, 2, 3, 4, 5, 6]:
		s_data = np.array([data[k][d] for k in range(3, 11)])

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
	for d in [1, 2, 3, 4, 5, 6]:
		handles.append(mpatches.Patch(color=colors[c]['color'], label=f'depth = {d}'))
		c += 1

	plt.legend(handles=handles, bbox_to_anchor=(1.05, 1.0), loc='upper left')

	plt.xticks(X_axis, X)
	plt.ylim(0, 1)
	plt.xlabel("Number of clusters")
	plt.ylabel("Combined metric")
	plt.title(f"FVCG Combined metric by Depth")
	plt.savefig(EVALUATION_PATH + f"/best_gen_fvcg_md.png", format="png", bbox_inches='tight', figsize=(60,20))
	plt.clf()

	# Welch's t-Test
	welchs_ttest(data, 'best_gen_fvcg_md.txt')

evaluation_combined_md('FAMC')