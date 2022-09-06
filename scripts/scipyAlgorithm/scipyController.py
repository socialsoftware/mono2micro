from fastapi import APIRouter
from scipyAlgorithm.createDendrogram import createDendrogram as createDendrogramScipy
from scipyAlgorithm.createDecomposition import createDecomposition as createDecompositionScipy

scipyRouter = APIRouter()


@scipyRouter.get("/scipy/{dendrogramName}/{similarityMatrixName}/createDendrogram")
async def createDendrogram(dendrogramName, similarityMatrixName):
    return createDendrogramScipy(dendrogramName, similarityMatrixName)


@scipyRouter.get("/scipy/{similarityMatrixName}/{cutType}/{cutValue}/createDecomposition")
async def createDecomposition(similarityMatrixName, cutType, cutValue):
    return createDecompositionScipy(similarityMatrixName, cutType, float(cutValue))
