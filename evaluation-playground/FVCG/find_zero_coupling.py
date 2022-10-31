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

def find_zero_coupling(strategy):

	codebases = [d for d in listdir(RESULTS_PATH) if not isfile(join(RESULTS_PATH, d))]
	text = ""

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
				cw = body['controllersWeight']
				sw = body['servicesWeight']
				iw = body['intermediateMethodsWeight']
				ew = body['entitiesWeight']

				if n > 2 and n < 11 and body['coupling'] == 0:
					print(f"CODEBASE: {cb}\n")
					text += f'>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n'
					text += f"CODEBASE: {cb}\n"
					text += f"coupling: {body['coupling']}\n"
					text += f"cohesion: {body['cohesion']}\n"
					text += f"complexity: {body['complexity']} - max_complexity: {max_complexity} - uniform_complexity: {uniform_complexity}\n"
					text += f"combined: {(1 + uniform_complexity + body['coupling'] - body['cohesion']) / 3}\n"
					text += f'--------------------------------------------------------\n'
					text += f"#clusters: {body['numberClusters']} - #NonEmptyClusters: {body['numberOfEntitiesClusters']}\n"
					text += f"LinkageType: {body['linkageType']}\n"
					text += f'MaxDepth: {maxDepth}\n'
					text += f'Weights: cw-{cw}, sw-{sw}, iw-{iw}, ew-{ew}\n'
					text += f'<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n'
					
		counter += 1

	with open(EVALUATION_PATH + f'/find_zero_coupling.txt', 'w') as file:
		file.write(text)

find_zero_coupling('FAMC')