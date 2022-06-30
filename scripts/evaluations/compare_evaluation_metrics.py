from os import listdir, mkdir
from os.path import isfile, join, isdir
from matplotlib import pyplot as plt
import pandas as pd
import statsmodels.api as sm
import numpy as np 
import requests
import json
import env

API_URL = "http://backend:8080/mono2micro"
MAX_CLUSTER_SIZE = 10

plt.style.use('seaborn-whitegrid')

beige_color  = dict(color='#EDC192')
green_color  = dict(color='#928E5E')
blue_color   = dict(color='#80C2CE')
red_color    = dict(color='#E55D53')
purple_color = dict(color='#BF80FF')

def compare_evaluation_metrics(strategy):
	try:
		mkdir(env.EVALUATION_PATH + "/" + strategy)
	except Exception as err:
		print(err)

	codebases = [d for d in listdir(env.RESULTS_PATH) if not isfile(join(env.RESULTS_PATH, d))]

	df = {
	    'n': [],
	    'cohesion': [],
	    'coupling': [],
	    'complexity': []
	}

	for cb in codebases:
		cb_path = join(env.RESULTS_PATH, cb)
		s_path = join(cb_path, strategy)
		stats_file_path = join(s_path, "analysisStats.json")

		with open(stats_file_path) as stats_file:
			stats = json.load(stats_file)

			response = requests.get(url = API_URL + f"/codebase/{cb}/maxComplexity")
			response_body = response.json()
			max_complexity = response_body['maxComplexity']

			if max_complexity == 0:
				print(f"Max complexity of {cb} is 0")
			else:

				for sm_idx in range(MAX_CLUSTER_SIZE):
					data_size = len(stats['complexity'][sm_idx]['data'])

					for data_idx in range(data_size):
						complexity = stats['complexity'][sm_idx]['data'][data_idx]
						uniform_complexity = complexity / max_complexity
						cohesion = stats['cohesion'][sm_idx]['data'][data_idx]
						coupling = stats['coupling'][sm_idx]['data'][data_idx]

						df['n'].append(sm_idx + 1)
						df['complexity'].append(uniform_complexity)
						df['cohesion'].append(cohesion)
						df['coupling'].append(coupling)

	df = pd.DataFrame(df)
	df['n'] = df['n'].astype(str)

	for nIdx in (3, 6, 9):
		plt.clf()

		dfi = df[df['n'].str.contains(str(nIdx))]

		if (len(dfi) != 0):
			
			b, a = np.polyfit(dfi.complexity, dfi.cohesion, deg=1)
			xseq = np.linspace(0, 10, num=100)
			plt.plot(xseq, a + b * xseq, color=purple_color['color'], lw=1.5)
			
			sp = plt.scatter(dfi.complexity, dfi.cohesion, s=3)
			plt.xlabel('Uniform Complexity')
			plt.ylabel('Cohesion')
			plt.title('Uniform Complexity versus Cohesion')
			plt.xlim(0, 1)
			plt.ylim(0, 1)
			plt.grid(True)
			plt.tight_layout()
			plt.savefig(env.EVALUATION_PATH + "/" + strategy + f"/ucomplexity_vs_cohesion_{nIdx}.png", format="png", bbox_inches='tight')

			plt.clf()

			b, a = np.polyfit(dfi.complexity, dfi.coupling, deg=1)
			# Create sequence of 100 numbers from 0 to 100
			xseq = np.linspace(0, 10, num=100)
			plt.plot(xseq, a + b * xseq, color=purple_color['color'], lw=1.5)

			sp = plt.scatter(dfi.complexity, dfi.coupling, s=3)
			plt.xlabel('Uniform Complexity')
			plt.ylabel('Coupling')
			plt.title('Uniform Complexity versus Coupling')
			plt.xlim(0, 1)
			plt.ylim(0, 1)
			plt.grid(True)
			plt.tight_layout()
			plt.savefig(env.EVALUATION_PATH + "/" + strategy + f"/ucomplexity_vs_coupling_{nIdx}.png", format="png", bbox_inches='tight')

	plt.clf()

	b, a = np.polyfit(df.complexity, df.cohesion, deg=1)
	# Create sequence of 100 numbers from 0 to 100 
	xseq = np.linspace(0, 10, num=100)
	plt.plot(xseq, a + b * xseq, color=purple_color['color'], lw=1.5)

	sp = plt.scatter(df.complexity, df.cohesion, s=3)
	plt.xlabel('Uniform Complexity')
	plt.ylabel('Cohesion')
	plt.title('Uniform Complexity versus Cohesion')
	plt.xlim(0, 1)
	plt.ylim(0, 1)
	plt.grid(True)
	plt.tight_layout()
	plt.savefig(env.EVALUATION_PATH + "/" + strategy + "/ucomplexity_vs_cohesion.png", format="png", bbox_inches='tight')

	plt.clf()

	b, a = np.polyfit(df.complexity, df.coupling, deg=1)
	# Create sequence of 100 numbers from 0 to 100 
	xseq = np.linspace(0, 10, num=100)
	plt.plot(xseq, a + b * xseq, color=purple_color['color'], lw=1.5)

	sp = plt.scatter(df.complexity, df.coupling, s=3)
	plt.xlabel('Uniform Complexity')
	plt.ylabel('Coupling')
	plt.title('Uniform Complexity versus Coupling')
	plt.xlim(0, 1)
	plt.ylim(0, 1)
	plt.grid(True)
	plt.tight_layout()
	plt.savefig(env.EVALUATION_PATH + "/" + strategy + "/ucomplexity_vs_coupling.png", format="png", bbox_inches='tight')

	plt.clf()

	grouped_df = df.groupby("n")
	data_to_plot = [np.array([]) for i in range(MAX_CLUSTER_SIZE)]
	for key, item in grouped_df:
		data_to_plot[int(key) - 1] = item.cohesion

	bp = plt.boxplot(data_to_plot, meanline=True, showmeans=True, meanprops=blue_color, boxprops=beige_color, medianprops=purple_color, whiskerprops=beige_color)
	caps = bp['caps']
	for i in range(len(caps)):
		if (i % 2) == 0: caps[i].set(color=green_color['color']) # Low cap
		else: caps[i].set(color=red_color['color']) # High cap

	plt.xlabel('Number of clusters')
	plt.ylabel('Cohesion')
	plt.title('Cohesion versus Number of clusters')
	plt.xlim(0, 11)
	plt.ylim(0, 1)
	plt.legend([bp['medians'][0], bp['means'][0]], ['Median', 'Mean'])
	plt.grid(True)
	plt.tight_layout()
	plt.savefig(env.EVALUATION_PATH + "/" + strategy + '/cohesion_vs_nclusters.png', format='png', bbox_inches='tight')

	plt.clf()

	grouped_df = df.groupby("n")
	data_to_plot = [np.array([]) for i in range(MAX_CLUSTER_SIZE)]
	for key, item in grouped_df:
		data_to_plot[int(key) - 1] = item.coupling

	bp = plt.boxplot(data_to_plot, meanline=True, showmeans=True, meanprops=blue_color, boxprops=beige_color, medianprops=purple_color, whiskerprops=beige_color)
	caps = bp['caps']
	for i in range(len(caps)):
		if (i % 2) == 0: caps[i].set(color=green_color['color']) # Low cap
		else: caps[i].set(color=red_color['color']) # High cap

	plt.xlabel('Number of clusters')
	plt.ylabel('Coupling')
	plt.title('Coupling versus Number of clusters')
	plt.xlim(0, 11)
	plt.ylim(0, 1)
	plt.legend([bp['medians'][0], bp['means'][0]], ['Median', 'Mean'])
	plt.grid(True)
	plt.tight_layout()
	plt.savefig(env.EVALUATION_PATH + "/" + strategy + '/coupling_vs_nclusters.png', format='png', bbox_inches='tight')

	plt.clf()

	grouped_df = df.groupby("n")
	data_to_plot = [np.array([]) for i in range(MAX_CLUSTER_SIZE)]
	for key, item in grouped_df:
		data_to_plot[int(key) - 1] = item.complexity

	bp = plt.boxplot(data_to_plot, meanline=True, showmeans=True, meanprops=blue_color, boxprops=beige_color, medianprops=purple_color, whiskerprops=beige_color)
	caps = bp['caps']
	for i in range(len(caps)):
		if (i % 2) == 0: caps[i].set(color=green_color['color']) # Low cap
		else: caps[i].set(color=red_color['color']) # High cap

	plt.xlabel('Number of clusters')
	plt.ylabel('Uniform Complexity')
	plt.title('Uniform Complexity versus Number of clusters')
	plt.xlim(0, 11)
	plt.ylim(0, 1)
	plt.legend([bp['medians'][0], bp['means'][0]], ['Median', 'Mean'])
	plt.grid(True)
	plt.tight_layout()
	plt.savefig(env.EVALUATION_PATH + "/" + strategy + '/ucomplexity_vs_nclusters.png', format='png', bbox_inches='tight')


	df['n'] = df['n'].astype(float)

	# Cohesion OLS Regression
	with open(env.EVALUATION_PATH + "/" + strategy + '/ucomplexity_vs_cohesion.txt', 'w') as f:
		X = df.loc[:, ['n', 'complexity']]
		y = df.loc[:, 'cohesion']
		X = sm.add_constant(X)
		model = sm.OLS(y, X)
		results = model.fit()
		f.write(str(results.summary()))

	# Coupling OLS Regression
	with open(env.EVALUATION_PATH + "/" + strategy + '/ucomplexity_vs_coupling.txt', 'w') as f:
		X = df.loc[:, ['n', 'complexity']]
		y = df.loc[:, 'coupling']
		X = sm.add_constant(X)
		model = sm.OLS(y, X)
		results = model.fit()
		f.write(str(results.summary()))
