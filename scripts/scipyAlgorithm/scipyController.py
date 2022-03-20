from fastapi import APIRouter
from scipyAlgorithm.createDendrogram import createDendrogram as createDendrogramScipy
from scipyAlgorithm.createDecomposition import createDecomposition as createDecompositionScipy
from scipyAlgorithm.analyser import analyser as analyserScipy
import env

scipyRouter = APIRouter()


@scipyRouter.get("/scipy/{codebaseName}/{strategyName}/createDendrogram")
async def createDendrogram(codebaseName, strategyName):
    createDendrogramScipy(env.CODEBASES_PATH, codebaseName, strategyName)
    return {"codebaseName": codebaseName, "strategyName": strategyName, "operation": "createDendrogram"}


@scipyRouter.get("/scipy/{codebaseName}/{strategyName}/{graphName}/{cutType}/{cutValue}/createDecomposition")
async def createDecomposition(codebaseName, strategyName, graphName, cutType, cutValue):
    createDecompositionScipy(env.CODEBASES_PATH, codebaseName, strategyName, graphName, cutType, float(cutValue))
    return {"codebaseName": codebaseName, "strategyName": strategyName, "graphName": graphName,
            "cutType": cutType, "cutValue": cutValue, "operation": "createDecomposition"}

# @scipyRouter.get("/scipy/{codebaseName}/{totalNumberOfEntities}/analyser")
# async def anayser(codebaseName, totalNumberOfEntities):
#    analyserScipy(env.CODEBASES_PATH, codebaseName, int(totalNumberOfEntities))
#    return {"codebaseName": codebaseName, "totalNumberOfEntities": totalNumberOfEntities, "operation": "analyser"}
