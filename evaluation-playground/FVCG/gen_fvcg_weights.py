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

def evaluation(strategy):

	codebases = [d for d in listdir(RESULTS_PATH) if not isfile(join(RESULTS_PATH, d))]

	df = {
		'n': [],
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

				if n > 2 and n < 11:
					cohesion = body['cohesion']
					coupling = body['coupling']
					df['n'].append(n)
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

	df = pd.DataFrame(df)

	X = df.loc[:, ['controllersWeight', 'entitiesWeight', 'servicesWeight', 'intermediateMethodsWeight', 'n']]
	y = df.loc[:, 'combined']
	X = sm.add_constant(X)
	model = sm.OLS(y, X)
	results = model.fit()
	with open(EVALUATION_PATH + f'/gen_fvcg_all_weights.txt', 'w') as file:
		file.write(str(['controllersWeight', 'servicesWeight', 'intermediateMethodsWeight', 'entitiesWeight']))
		file.write(str(results.summary()))

	depend_vars = ['controllersWeight', 'servicesWeight', 'intermediateMethodsWeight', 'entitiesWeight']
	columns = [''] * (len(depend_vars) - 1)

	for i in range(len(depend_vars)):
		for j in range(len(depend_vars) - 1):
			columns[j] = depend_vars[(i+j) % len(depend_vars)]

		X = df.loc[:, columns + ['n']]
		y = df.loc[:, 'combined']
		X = sm.add_constant(X)
		model = sm.OLS(y, X)
		results = model.fit()
		with open(EVALUATION_PATH + f'/gen_fvcg_weights_wo_{depend_vars[i]}.txt', 'w') as file:
			file.write(str(columns))
			file.write(str(results.summary()))

	b, a = np.polyfit(df.controllersWeight, df.combined, deg=1)
	xseq = np.linspace(0, 100, num=100)
	plt.plot(xseq, a + b * xseq, color='#BF80FF', lw=1.5)
	sp = plt.scatter(df.controllersWeight, df.combined, s=3)
	plt.xlabel('Controllers Weight')
	plt.ylabel('Combined metric')
	plt.title('Combined metric versus controllersWeight')
	plt.xlim(0, 100)
	plt.ylim(0, 1)
	plt.grid(True)
	plt.tight_layout()
	# plt.show()
	plt.savefig(EVALUATION_PATH + f"/gen_fvcg_cw.png", format="png", bbox_inches='tight')
	plt.clf()

	b, a = np.polyfit(df.entitiesWeight, df.combined, deg=1)
	xseq = np.linspace(0, 100, num=100)
	plt.plot(xseq, a + b * xseq, color='#BF80FF', lw=1.5)
	sp = plt.scatter(df.entitiesWeight, df.combined, s=3)
	plt.xlabel('Entities Weight')
	plt.ylabel('Combined metric')
	plt.title('Combined metric versus entitiesWeight')
	plt.xlim(0, 100)
	plt.ylim(0, 1)
	plt.grid(True)
	plt.tight_layout()
	# plt.show()
	plt.savefig(EVALUATION_PATH + f"/gen_fvcg_ew.png", format="png", bbox_inches='tight')
	plt.clf()

	b, a = np.polyfit(df.servicesWeight, df.combined, deg=1)
	xseq = np.linspace(0, 100, num=100)
	plt.plot(xseq, a + b * xseq, color='#BF80FF', lw=1.5)
	sp = plt.scatter(df.servicesWeight, df.combined, s=3)
	plt.xlabel('Services Weight')
	plt.ylabel('Combined metric')
	plt.title('Combined metric versus servicesWeight')
	plt.xlim(0, 100)
	plt.ylim(0, 1)
	plt.grid(True)
	plt.tight_layout()
	# plt.show()
	plt.savefig(EVALUATION_PATH + f"/gen_fvcg_sw.png", format="png", bbox_inches='tight')
	plt.clf()

	b, a = np.polyfit(df.intermediateMethodsWeight, df.combined, deg=1)
	xseq = np.linspace(0, 100, num=100)
	plt.plot(xseq, a + b * xseq, color='#BF80FF', lw=1.5)
	sp = plt.scatter(df.intermediateMethodsWeight, df.combined, s=3)
	plt.xlabel('Intermediate Weight')
	plt.ylabel('Combined metric')
	plt.title('Combined metric versus intermediateMethodsWeight')
	plt.xlim(0, 100)
	plt.ylim(0, 1)
	plt.grid(True)
	plt.tight_layout()
	# plt.show()
	plt.savefig(EVALUATION_PATH + f"/gen_fvcg_iw.png", format="png", bbox_inches='tight')
	plt.clf()

evaluation('FAMC')