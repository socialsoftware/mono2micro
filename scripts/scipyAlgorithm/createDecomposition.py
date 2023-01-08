import numpy as np
import pymongo
import gridfs
from scipy.cluster import hierarchy
from sklearn import metrics
import json

import env

def createDecompositionFromEntities(similarityMatrix, linkageType, cutType, cutValue):

	entities = similarityMatrix["elements"]
	matrix = np.array(similarityMatrix["matrix"])

	hierarc = hierarchy.linkage(y=matrix, method=linkageType)

	if cutType == "H":
		cut = hierarchy.cut_tree(hierarc, height=cutValue)
	elif cutType == "N":
		cut = hierarchy.cut_tree(hierarc, n_clusters=cutValue)

	clusters = {}
	for i in range(len(cut)):
		if str(cut[i][0]) in clusters.keys():
			clusters[str(cut[i][0])] += [entities[i]]
		else:
			clusters[str(cut[i][0])] = [entities[i]]

	nodes = hierarchy.fcluster(hierarc, len(clusters), criterion="maxclust")
	try:
		silhouetteScore = metrics.silhouette_score(matrix, nodes)
	except:
		silhouetteScore = 0

	return {
		"operation": "createSciPyDecomposition",
		"silhouetteScore": "{0:.2f}".format(silhouetteScore),
		"clusters": json.dumps(clusters, indent=4)
	}


def createDecompositionFromClasses(similarityMatrix, linkageType, cutType, cutValue):

	names = similarityMatrix["elements"]
	ids = similarityMatrix["translationIds"]
	matrix = np.array(similarityMatrix["matrix"])

	hierarc = hierarchy.linkage(y=matrix, method=linkageType)

	if cutType == "H":
		cut = hierarchy.cut_tree(hierarc, height=cutValue)
	elif cutType == "N":
		cut = hierarchy.cut_tree(hierarc, n_clusters=cutValue)

	clusters = {}
	for i in range(len(cut)):
		if str(cut[i][0]) in clusters.keys():
			if ids[i] != -1:
				clusters[str(cut[i][0])] += [ids[i]]
		else:
			if ids[i] != -1:
				clusters[str(cut[i][0])] = [ids[i]]
			else:
				clusters[str(cut[i][0])] = []

	nodes = hierarchy.fcluster(hierarc, len(clusters), criterion="maxclust")
	try:
		silhouetteScore = metrics.silhouette_score(matrix, nodes)
	except:
		silhouetteScore = 0

	clustersJSON = {"silhouetteScore": "{0:.2f}".format(silhouetteScore), "clusters": clusters}

	return {
		"operation": "createSciPyDecomposition",
		"silhouetteScore": "{0:.2f}".format(silhouetteScore),
		"clusters": json.dumps(clustersJSON, indent=4)
	}


def createDecompositionFromFunctionalities(similarityMatrix, linkageType, cutType, cutValue):
	pass

def createDecomposition(similarityMatrixName, linkageType, cutType, cutValue):

	client = pymongo.MongoClient(env.MONGO_DB)
	DB = client[env.MONGO_DB_NAME]
	fs = gridfs.GridFS(DB)  # To use with large files
	similarityMatrixFile = fs.find_one({"filename": similarityMatrixName})
	similarityMatrix = json.loads(similarityMatrixFile.read().decode("utf-8"))

	clusterPrimitiveType = similarityMatrix["clusterPrimitiveType"]

	if (clusterPrimitiveType == 'Class'):
		return createDecompositionFromClasses(similarityMatrix, linkageType, cutType, cutValue)
	elif (clusterPrimitiveType == 'Functionality'):
		return createDecompositionFromFunctionalities(similarityMatrix, linkageType, cutType, cutValue)
	return createDecompositionFromEntities(similarityMatrix, linkageType, cutType, cutValue)