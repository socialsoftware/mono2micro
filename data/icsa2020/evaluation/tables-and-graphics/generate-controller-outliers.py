import json
import sys

controllerOutliersComplexities = []
analyserResultPath = str(sys.argv[1])
decompositionName = str(sys.argv[2])
numberOutliers = int(sys.argv[3])

with open(analyserResultPath) as f:
    analyserResult = json.load(f)


for controllerComplexity in analyserResult[decompositionName]["controllerComplexities"]:
	controllerOutliersComplexities += [[analyserResult[decompositionName]["controllerComplexities"][controllerComplexity], controllerComplexity]]


controllerOutliersComplexities.sort(reverse=True, key=lambda x: x[0])

for outlier in controllerOutliersComplexities[:numberOutliers]:
	print(','.join(map(str,outlier)))