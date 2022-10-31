from os import listdir, mkdir
from os.path import isfile, join, isdir
from matplotlib import pyplot as plt
import pandas as pd
import statsmodels.api as sm
import numpy as np 
import requests
import json

API_URL = "http://localhost:8080/mono2micro/"
MAX_CLUSTER_SIZE = 10

RESULTS_PATH = "../results"
EVALUATION_PATH = "../evaluation/FVCG"

plt.style.use('seaborn-whitegrid')

purple_color = dict(color='#BF80FF')

strategies_translator = { 'CA' : 'AM', 'FAMC' : 'FVMC', 'FAEA' : 'FVET', 'EA' : 'EV', 'SA' : 'MM' }

def evaluation_cb(strategy):

	codebases = [d for d in listdir(RESULTS_PATH) if not isfile(join(RESULTS_PATH, d))]

	df = {
		'n': [],
		'cb': [],
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
				maxDepth = body['maxDepth']

				if n > 2 and n < 11:
					cohesion = body['cohesion']
					coupling = body['coupling']
					df['n'].append(n)
					df['cb'].append(counter-1)
					df['combined'].append((1 + uniform_complexity + coupling - cohesion) / 3)
		counter += 1

	df = pd.DataFrame(df)

	X = df.loc[:, ['cb']]
	y = df.loc[:, 'combined']
	X = sm.add_constant(X)
	model = sm.OLS(y, X)
	results = model.fit()

	with open(EVALUATION_PATH + '/gen_fvcg_cb.txt', 'w') as file:
		file.write(str(results.summary()))

	#b, a = np.polyfit(df.cb, df.combined, deg=1)
	#xseq = np.linspace(0, 100, num=100)
	#plt.plot(xseq, a + b * xseq, color=purple_color['color'], lw=1.5)
	sp = plt.scatter(df.cb, df.combined, s=3)
	plt.xlabel('Codebases')
	plt.ylabel('Mix metric')
	plt.title('Mix metric versus Codebases')
	plt.xlim(-1, 86)
	plt.ylim(0, 1)
	plt.grid(True)
	plt.tight_layout()
	plt.savefig(EVALUATION_PATH + f"/gen_fvcg_cb.png", format="png", bbox_inches='tight')
	plt.clf()

def best_evaluation_cb(strategy):

	codebases = [d for d in listdir(RESULTS_PATH) if not isfile(join(RESULTS_PATH, d))]

	df = {
		'n': [],
		'cb': [],
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
				maxDepth = body['maxDepth']

				if n > 2 and n < 11:
					cohesion = body['cohesion']
					coupling = body['coupling']
					df['n'].append(n)
					df['cb'].append(counter-1)
					df['combined'].append((1 + uniform_complexity + coupling - cohesion) / 3)
		counter += 1

	tmp_data = {}
	for n in range(3, 11):
		tmp_data[n] = {}

	for i in range(len(df['n'])):
		if (not (df['cb'][i] in tmp_data[df['n'][i]])):
			tmp_data[df['n'][i]][df['cb'][i]] = 1.1
		if df['combined'][i] < tmp_data[df['n'][i]][df['cb'][i]]:
			tmp_data[df['n'][i]][df['cb'][i]] = df['combined'][i]

	tmp_df = {
			'n': [],
			'cb': [],
			'combined': []
		}

	for n in tmp_data:
		for cb in tmp_data[n]:
			tmp_df['n'].append(n)
			tmp_df['cb'].append(cb)
			tmp_df['combined'].append(tmp_data[n][cb])

	df = pd.DataFrame(tmp_df)

	X = df.loc[:, ['cb']]
	y = df.loc[:, 'combined']
	X = sm.add_constant(X)
	model = sm.OLS(y, X)
	results = model.fit()

	with open(EVALUATION_PATH + '/best_gen_fvcg_cb.txt', 'w') as file:
		file.write(str(results.summary()))

	#b, a = np.polyfit(df.cb, df.combined, deg=1)
	#xseq = np.linspace(0, 100, num=100)
	#plt.plot(xseq, a + b * xseq, color=purple_color['color'], lw=1.5)
	sp = plt.scatter(df.cb, df.combined, s=3)
	plt.xlabel('Codebases')
	plt.ylabel('Mix metric')
	plt.title('Mix metric versus Codebases')
	plt.xlim(-1, 86)
	plt.ylim(0, 1)
	plt.grid(True)
	plt.tight_layout()
	plt.savefig(EVALUATION_PATH + f"/best_gen_fvcg_cb.png", format="png", bbox_inches='tight')
	plt.clf()

best_evaluation_cb('FAMC')