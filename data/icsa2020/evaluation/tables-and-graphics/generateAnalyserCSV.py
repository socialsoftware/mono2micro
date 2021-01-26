import json
import sys

csvLines = []
analyserResultPath = str(sys.argv[1])

with open(analyserResultPath) as f:
    analyserResult = json.load(f)

for similarityCombination in analyserResult:
	accessWeight = str(analyserResult[similarityCombination]["accessWeight"])
	writeWeight = str(analyserResult[similarityCombination]["writeWeight"])
	readWeight = str(analyserResult[similarityCombination]["readWeight"])
	sequenceWeight = str(analyserResult[similarityCombination]["sequenceWeight"])
	numberClusters = str(analyserResult[similarityCombination]["numberClusters"])
	maxClusterSize = str(analyserResult[similarityCombination]["maxClusterSize"])
	cohesion = str(analyserResult[similarityCombination]["cohesion"])
	coupling = str(analyserResult[similarityCombination]["coupling"])
	complexity = str(analyserResult[similarityCombination]["complexity"])
	fmeasure = str(analyserResult[similarityCombination]["fmeasure"])
	accuracy = str(analyserResult[similarityCombination]["accuracy"])
	precision = str(analyserResult[similarityCombination]["precision"])
	recall = str(analyserResult[similarityCombination]["recall"])
	specificity = str(analyserResult[similarityCombination]["specificity"])
	csvLines += [','.join([accessWeight,writeWeight,readWeight,sequenceWeight,numberClusters,maxClusterSize,cohesion,coupling,complexity,fmeasure,accuracy,precision,recall,specificity])]

print("Access,Write,Read,Sequence,NumberClusters,MaxClusterSize,Cohesion,Coupling,Complexity,FMeasure,Accuracy,Precision,Recall,Specificity")
for combination in csvLines:
	print(combination)