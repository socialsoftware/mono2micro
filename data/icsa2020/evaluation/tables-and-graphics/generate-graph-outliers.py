import json
import sys

graphOutliersComplexities = []
analyserResultPath = str(sys.argv[1])
numberClusters = float(sys.argv[2])
numberOutliers = int(sys.argv[3])

with open(analyserResultPath) as f:
    analyserResult = json.load(f)

for graph in analyserResult:
	if analyserResult[graph]["numberClusters"] == numberClusters:
		c = analyserResult[graph]["complexity"]
		a = analyserResult[graph]["accessWeight"]
		w = analyserResult[graph]["writeWeight"]
		r = analyserResult[graph]["readWeight"]
		s = analyserResult[graph]["sequenceWeight"]
		graphOutliersComplexities += [[c,a,w,r,s]]

graphOutliersComplexities.sort(reverse=True, key=lambda x: x[0])

for outlier in graphOutliersComplexities[:numberOutliers]:
	print(','.join(map(str,outlier)))
