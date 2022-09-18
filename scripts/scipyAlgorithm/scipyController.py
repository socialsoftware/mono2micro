from fastapi import APIRouter
from scipyAlgorithm.createDendrogram import createDendrogram as createDendrogramScipy
from scipyAlgorithm.createDecomposition import createDecomposition as createDecompositionScipy

scipyRouter = APIRouter()


@scipyRouter.get("/scipy/{similarityName}/{similarityMatrixName}/createDendrogram")
async def createDendrogram(similarityName, similarityMatrixName):
    return createDendrogramScipy(similarityName, similarityMatrixName)


@scipyRouter.get("/scipy/{similarityMatrixName}/{cutType}/{cutValue}/createDecomposition")
async def createDecomposition(similarityMatrixName, cutType, cutValue):
    return createDecompositionScipy(similarityMatrixName, cutType, float(cutValue))
