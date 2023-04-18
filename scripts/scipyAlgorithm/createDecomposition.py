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

	return {
		"operation": "createSciPyDecomposition",
		"silhouetteScore": "{0:.2f}".format(silhouetteScore),
		"clusters": json.dumps(clusters, indent=4)
	}


def createDecompositionFromFunctionalities(similarityMatrix, linkageType, cutType, cutValue, translation_json, features_entities_accesses):
	
	names = similarityMatrix["elements"]
	matrix = np.array(similarityMatrix["matrix"])
	totalNumberOfEntities = len(translation_json.keys())

	hierarc = hierarchy.linkage(y=matrix, method=linkageType)

	if cutType == "H":
		cut = hierarchy.cut_tree(hierarc, height=cutValue)
	elif cutType == "N":
		cut = hierarchy.cut_tree(hierarc, n_clusters=cutValue)

	clusters = {}
	for i in range(len(cut)):
		if str(cut[i][0]) in clusters.keys():
			clusters[str(cut[i][0])] += [i]
		else:
			clusters[str(cut[i][0])] = [i]

	
	entities_clusters_accesses = {}
	for entity in range(1, totalNumberOfEntities + 1):
		entities_clusters_accesses[entity] = {}
		for cluster in clusters.keys():
			entities_clusters_accesses[entity][cluster] = { "R" : 0, "W" : 0}

	for cluster in clusters.keys():

		for idx in clusters[cluster]:
			feature = names[idx]

			if feature in features_entities_accesses.keys():
				accesses = features_entities_accesses[feature]['t'][0]['a']

				for access in accesses:
					access_type = access[0]
					entity = access[1]
					entities_clusters_accesses[entity][cluster][access_type] += 1

	entities_cluster = {}
	for entity in entities_clusters_accesses.keys():
		max_nbr_accesses = 0
		attr_cluster = "0"

		for cluster in entities_clusters_accesses[entity].keys():
			nbr_accesses = entities_clusters_accesses[entity][cluster]["R"] + entities_clusters_accesses[entity][cluster]["W"]

			if nbr_accesses > max_nbr_accesses:
				max_nbr_accesses = nbr_accesses
				attr_cluster = cluster

		if attr_cluster in entities_cluster.keys():
			entities_cluster[attr_cluster] += [entity]
		else:
			entities_cluster[attr_cluster] = [entity]

	for cluster in clusters.keys():
		if cluster not in entities_cluster.keys():
			entities_cluster[cluster] = []

	nodes = hierarchy.fcluster(hierarc, len(clusters), criterion="maxclust")
	try:
		silhouetteScore = metrics.silhouette_score(matrix, nodes)
	except:
		silhouetteScore = 0

	return {
		"operation": "createSciPyDecomposition",
		"silhouetteScore": "{0:.2f}".format(silhouetteScore),
		"clusters": json.dumps(entities_cluster, indent=4)
	}

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
		translationFile = fs.find_one({"filename": similarityMatrix['translationFileName']})
		translation = json.loads(translationFile.read().decode("utf-8"))
		featuresEntitiesAccessesFile = fs.find_one({"filename": similarityMatrix['accessesFileName']})
		featuresEntitiesAccesses = json.loads(featuresEntitiesAccessesFile.read().decode("utf-8"))
		return createDecompositionFromFunctionalities(similarityMatrix, linkageType, cutType, cutValue, translation, featuresEntitiesAccesses)
		
	return createDecompositionFromEntities(similarityMatrix, linkageType, cutType, cutValue)
