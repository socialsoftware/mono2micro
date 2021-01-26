import json
import sys

controllerComplexities = []
analyserResultPath = str(sys.argv[1])
decompositionName = str(sys.argv[2])

with open(analyserResultPath) as f:
    analyserResult = json.load(f)

print("data")
for controllerComplexity in analyserResult[decompositionName]["controllerComplexities"]:
	controllerComplexities += [analyserResult[decompositionName]["controllerComplexities"][controllerComplexity]]

for complexity in sorted(controllerComplexities):
	print(complexity)
