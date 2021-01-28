import json
import sys

complexityCohesion = []
complexityCoupling = []
analyserResultPath = str(sys.argv[1])
numberClusters = float(sys.argv[2])
cohesionOrCoupling = str(sys.argv[3])

with open(analyserResultPath) as f:
    analyserResult = json.load(f)

for similarityCombination in analyserResult:
	if analyserResult[similarityCombination]["numberClusters"] == numberClusters:
		complexity = analyserResult[similarityCombination]["complexity"]
		cohesion = analyserResult[similarityCombination]["cohesion"]
		coupling = analyserResult[similarityCombination]["coupling"]
	
		complexityCohesion += [[complexity, cohesion]]
		complexityCoupling += [[complexity, coupling]]

complexityCohesion.sort(key=lambda x: x[0])
complexityCoupling.sort(key=lambda x: x[0])

if cohesionOrCoupling == "cohesion":
	print("complexity\tcohesion")
	for pair in complexityCohesion:
		print(str(pair[0]) + "\t" + str(pair[1]))
else:
	print("complexity\tcoupling")
	for pair in complexityCoupling:
		print(str(pair[0]) + "\t" + str(pair[1]))