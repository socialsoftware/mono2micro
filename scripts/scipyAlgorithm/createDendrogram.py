import numpy as np
import matplotlib
import matplotlib.pyplot as plt
import json

import pymongo
import gridfs
from scipy.cluster import hierarchy
from io import BytesIO

import env

matplotlib.use('agg')


def createDendrogram(similarityName, similarityMatrixName, linkageType):
    # Database initialization in order to contact MongoDB
    client = pymongo.MongoClient(env.MONGO_DB)
    DB = client[env.MONGO_DB_NAME]
    fs = gridfs.GridFS(DB)  # To use with large files
    similarityMatrixFile = fs.find_one({"filename": similarityMatrixName})
    similarityMatrix = json.loads(similarityMatrixFile.read().decode("utf-8"))

    entities = similarityMatrix["elements"]
    matrix = np.array(similarityMatrix["matrix"])
    labels = similarityMatrix["labels"]

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)

    fig = plt.figure(figsize=(25, 10))

    hierarchy.dendrogram(hierarc, labels=labels, leaf_rotation=90.0, distance_sort='descending')

    img = BytesIO()
    plt.savefig(img, format="png", bbox_inches='tight')
    img.seek(0)
    dendrogramName = similarityName + "_image"
    copheneticDistanceName = similarityName + "_copheneticDistance"
    fs.put(img.getvalue(), filename=dendrogramName)
    fs.put(BytesIO(bytes(str(hierarchy.cophenet(hierarc).tolist()), 'ascii')).getvalue(), filename=copheneticDistanceName)
    client.close()

    return {"operation": "createDendrogram", "dendrogramName": dendrogramName, "copheneticDistanceName": copheneticDistanceName}
