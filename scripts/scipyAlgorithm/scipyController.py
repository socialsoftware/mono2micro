from fastapi import APIRouter
from scipyAlgorithm.createDendrogram import createDendrogram as createDendrogramScipy
from scipyAlgorithm.createDecomposition import createDecomposition as createDecompositionScipy
import env

scipyRouter = APIRouter()


@scipyRouter.get("/scipy/{codebaseName}/{strategyName}/createDendrogram")
async def createDendrogram(codebaseName, strategyName):
    createDendrogramScipy(env.CODEBASES_PATH, codebaseName, strategyName)
    return {"codebaseName": codebaseName, "strategyName": strategyName, "operation": "createDendrogram"}

@scipyRouter.get("/scipy/{codebaseName}/{strategyFolder}/{strategyName}/{graphName}/{matrixFile}/{cutType}/{cutValue}/createDecomposition")
async def createDecomposition(codebaseName, strategyFolder, strategyName, graphName, matrixFile, cutType, cutValue):
    createDecompositionScipy(env.CODEBASES_PATH, codebaseName, strategyFolder, strategyName, graphName, matrixFile, cutType, float(cutValue))
    return {"codebaseName": codebaseName, "strategyName": strategyName, "graphName": graphName,
            "cutType": cutType, "cutValue": cutValue, "operation": "createDecomposition"}
