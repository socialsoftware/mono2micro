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


def createDendrogram(dendrogramName, similarityMatrixName):
    # Database initialization in order to contact MongoDB
    client = pymongo.MongoClient(env.MONGO_DB)
    DB = client[env.MONGO_DB_NAME]
    fs = gridfs.GridFS(DB)  # To use with large files
    similarityMatrixFile = fs.find_one({"filename": similarityMatrixName})
    similarityMatrix = json.loads(similarityMatrixFile.read().decode("utf-8"))

    entities = similarityMatrix["entities"]
    linkageType = similarityMatrix["linkageType"]
    matrix = np.array(similarityMatrix["matrix"])

    hierarc = hierarchy.linkage(y=matrix, method=linkageType)

    fig = plt.figure(figsize=(25, 10))

    hierarchy.dendrogram(hierarc, labels=entities, distance_sort='descending')

    img = BytesIO()
    plt.savefig(img, format="png", bbox_inches='tight')
    img.seek(0)
    imageName = dendrogramName + "_image"
    copheneticDistanceName = dendrogramName + "_copheneticDistance"
    fs.put(img.getvalue(), filename=imageName)
    fs.put(BytesIO(bytes(str(hierarchy.cophenet(hierarc).tolist()), 'ascii')).getvalue(), filename=copheneticDistanceName)
    client.close()

    return {"operation": "createDendrogram", "imageName": imageName, "copheneticDistanceName": copheneticDistanceName}
