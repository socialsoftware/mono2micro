from fastapi import APIRouter
from scipyAlgorithm.createDendrogram import createDendrogram as createDendrogramScipy
from scipyAlgorithm.createDecomposition import createDecomposition as createDecompositionScipy

scipyRouter = APIRouter()


@scipyRouter.get("/scipy/{similarityName}/{similarityMatrixName}/{linkageType}/createDendrogram")
async def createDendrogram(similarityName, similarityMatrixName, linkageType):
    return createDendrogramScipy(similarityName, similarityMatrixName, linkageType)


@scipyRouter.get("/scipy/{similarityMatrixName}/{linkageType}/{cutType}/{cutValue}/createDecomposition")
async def createDecomposition(similarityMatrixName, linkageType, cutType, cutValue):
    return createDecompositionScipy(similarityMatrixName, linkageType, cutType, float(cutValue))
