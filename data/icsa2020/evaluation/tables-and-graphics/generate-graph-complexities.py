import json
import sys

graphComplexities = []
analyserResultPath = str(sys.argv[1])
numberClusters = float(sys.argv[2])

with open(analyserResultPath) as f:
    analyserResult = json.load(f)

print("data")
for graph in analyserResult:
	if analyserResult[graph]["numberClusters"] == numberClusters:
		graphComplexities += [analyserResult[graph]["complexity"]]

for complexity in sorted(graphComplexities):
	print(complexity)
