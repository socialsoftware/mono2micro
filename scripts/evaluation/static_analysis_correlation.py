from os import mkdir, listdir
from os.path import isfile, join, isdir
import numpy as np 
from matplotlib import pyplot as plt

import json
import pandas as pd
import seaborn as sn
import env

plt.style.use('seaborn-whitegrid')

def static_analysis_correlation():
	strategy = "SA"
	try:
		mkdir(env.EVALUATION_PATH + "/" + strategy)
	except Exception as err:
		print(err)

	data = {
		'accessWeight': [],
		'writeWeight': [],
		'readWeight': [],
		'sequenceWeight': [],
		'numberClusters': [],
		'cohesion': [],
		'coupling': [],
		'complexity': []
	}

	codebases = [d for d in listdir(env.RESULTS_PATH) if not isfile(join(RESULTS_PATH, d))]

	for cb in codebases:
		cb_path = join(env.RESULTS_PATH, cb)
		s_path = join(cb_path, strategy)
		result_file_path = join(s_path, "analyserResult.json")

		with open(result_file_path) as result_file:
			result = json.load(result_file)

			for key in result:
				data['accessWeight'].append(result[key]['accessWeight'])
				data['writeWeight'].append(result[key]['writeWeight'])
				data['readWeight'].append(result[key]['readWeight'])
				data['sequenceWeight'].append(result[key]['sequenceWeight'])
				data['numberClusters'].append(result[key]['numberClusters'])
				data['cohesion'].append(result[key]['cohesion'])
				data['coupling'].append(result[key]['coupling'])
				data['complexity'].append(result[key]['complexity'])


	df = pd.DataFrame(data, columns=[
		'accessWeight',
		'writeWeight',
		'readWeight',
		'sequenceWeight',
		'numberClusters',
		'cohesion',
		'coupling',
		'complexity'
	])
	corrMatrix = df.corr()
	plt.clf()
	sn.heatmap(corrMatrix, annot=True, cmap="Blues")
	plt.savefig(env.EVALUATION_PATH + "/" + strategy + "/correlation.png", format="png", bbox_inches='tight')

	depend_vars = ['accessWeight', 'writeWeight', 'readWeight', 'sequenceWeight']
	columns = [''] * (len(depend_vars) - 1) + ['numberClusters', 'cohesion', 'coupling', 'complexity']

	for i in range(len(depend_vars)):
		for j in range(len(depend_vars) - 1):
			columns[j] = depend_vars[(i+j) % len(depend_vars)]

		dp_var_rm = depend_vars[(i+len(depend_vars)-1) % len(depend_vars)]

		df = pd.DataFrame(data, columns=columns)
		corrMatrix = df.corr()
		plt.clf()
		sn.heatmap(corrMatrix, annot=True, cmap="Blues")
		plt.savefig(env.EVALUATION_PATH + "/" + strategy + "/correlation_wo_" + dp_var_rm + ".png", format="png", bbox_inches='tight')
