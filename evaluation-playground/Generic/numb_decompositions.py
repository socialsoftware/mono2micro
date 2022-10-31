from os import mkdir, listdir
from os.path import isfile, join, isdir
import numpy as np 
from matplotlib import pyplot as plt
import matplotlib.patches as mpatches

import requests
import json
import pandas as pd
import seaborn as sn

plt.style.use('seaborn-whitegrid')

RESULTS_PATH = "../results"
EVALUATION_PATH = "../evaluation"

def numb_decomp():

	data = {
		3: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
		4: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
		5: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
		6: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
		7: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
		8: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
		9: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
		10: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 }
	}

	codebases = [d for d in listdir(RESULTS_PATH) if not isfile(join(RESULTS_PATH, d))]

	i = 0

	for cb in codebases:
		cb_path = join(RESULTS_PATH, cb)

		data = {
			3: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
			4: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
			5: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
			6: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
			7: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
			8: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
			9: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 },
			10: { 'CA': 0, 'EA': 0, 'FAEA': 0, 'FAMC': 0, 'SA': 0 }
		}

		i += 1
		print(f'Progress: {i}/{len(codebases)}')

		for strategy in ['CA', 'FAEA', 'FAMC']:
			s_path = join(cb_path, strategy)
			result_file_path = join(s_path, "analyserResult.json")

			with open(result_file_path) as result_file:
				result = json.load(result_file)

				for key in result:
					nc = result[key]['numberOfEntitiesClusters']
					if nc > 2 and nc < 11:
						data[nc][strategy] += 1

		for strategy in ['EA','SA']:
			s_path = join(cb_path, strategy)
			result_file_path = join(s_path, "analyserResult.json")

			with open(result_file_path) as result_file:
				result = json.load(result_file)

				for key in result:
					nc = result[key]['numberClusters']
					if nc > 2 and nc < 11:
						data[nc][strategy] += 1

		for s in ['CA', 'EA', 'FAMC', 'FAEA', 'SA']:
			n = 0
			for nc in range(3, 11):
				n += data[nc][s]

			print(f'Codebase: {cb} -> Strategy: {s} -> NDec: {n}')

numb_decomp()
