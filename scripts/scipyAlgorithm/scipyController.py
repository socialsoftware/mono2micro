from fastapi import APIRouter
from scipyAlgorithm.createDendrogram import createDendrogram as createDendrogramScipy
from scipyAlgorithm.createDecomposition import createDecomposition as createDecompositionScipy

scipyRouter = APIRouter()


@scipyRouter.get("/scipy/{strategyName}/{similarityMatrixName}/createDendrogram")
async def createDendrogram(strategyName, similarityMatrixName):
    return createDendrogramScipy(strategyName, similarityMatrixName)


@scipyRouter.get("/scipy/{similarityMatrixName}/{cutType}/{cutValue}/createDecomposition")
async def createDecomposition(similarityMatrixName, cutType, cutValue):
    return createDecompositionScipy(similarityMatrixName, cutType, float(cutValue))
