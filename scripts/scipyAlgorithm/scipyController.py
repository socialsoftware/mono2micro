from fastapi import APIRouter, Body
from scipyAlgorithm.createDendrogram import createDendrogram as createDendrogramScipy
from scipyAlgorithm.cutDendrogram import cutDendrogram as cutDendrogramScipy
from scipyAlgorithm.analyser import analyser as analyserScipy
from scipyAlgorithm.commit_cut import cut

import env

scipyRouter = APIRouter()


@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/createDendrogram")
async def createDendrogram(codebaseName, dendrogramName):
    createDendrogramScipy(env.CODEBASES_PATH, codebaseName, dendrogramName)
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "operation": "createDendrogram"}


@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/{graphName}/{cutType}/{cutValue}/{commitBased}/cut")
async def cutDendrogram(codebaseName, dendrogramName, graphName, cutType, cutValue, commitBased):
    cutDendrogramScipy(env.CODEBASES_PATH, codebaseName, dendrogramName, graphName, cutType, float(cutValue), commitBased)
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "graphName": graphName,
            "cutType": cutType, "cutValue": cutValue, "base": commitBased, "operation": "cutDendrogram"}


@scipyRouter.get("/scipy/{codebaseName}/{totalNumberOfEntities}/analyser")
async def anayser(codebaseName, totalNumberOfEntities):
    analyserScipy(env.CODEBASES_PATH, codebaseName, int(totalNumberOfEntities))
    return {"codebaseName": codebaseName, "totalNumberOfEntities": totalNumberOfEntities, "operation": "analyser"}

@scipyRouter.post("/scipy/{codebaseName}/{commitMetricValue}/{authorsMetricValue}/{clusters}/cut")
async def commit_analyze(codebaseName, commitMetricValue, authorsMetricValue, clusters, matrix=Body(...)):
    cut_result = cut(env.CODEBASES_PATH, codebaseName, commitMetricValue, authorsMetricValue, clusters, matrix)
    return cut_result
