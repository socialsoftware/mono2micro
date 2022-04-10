from fastapi import APIRouter
from scipyAlgorithm.createDendrogram import createDendrogram as createDendrogramScipy
from scipyAlgorithm.createDendrogramByClassesScipy import createDendrogramByClassesScipy as createDendrogramByClassesScipy
from scipyAlgorithm.createDendrogramByFeaturesMethodCallsScipy import createDendrogramByFeaturesMethodCallsScipy as createDendrogramByFeaturesMethodCallsScipy
from scipyAlgorithm.createDendrogramByFeaturesEntitiesScipy import createDendrogramByFeaturesEntitiesScipy as createDendrogramByFeaturesEntitiesScipy
from scipyAlgorithm.createDendrogramByFeaturesMixedScipy import createDendrogramByFeaturesMixedScipy as createDendrogramByFeaturesMixedScipy
from scipyAlgorithm.cutDendrogram import cutDendrogram as cutDendrogramScipy
from scipyAlgorithm.cutDendrogramByClassesScipy import cutDendrogramByClasses as cutDendrogramByClassesScipy
from scipyAlgorithm.cutDendrogramByFeaturesMethodCallsScipy import cutDendrogramByFeaturesMethodCalls as cutDendrogramByFeaturesMethodCallsScipy
from scipyAlgorithm.cutDendrogramByFeaturesEntitiesScipy import cutDendrogramByFeaturesEntities as cutDendrogramByFeaturesEntitiesScipy
from scipyAlgorithm.cutDendrogramByFeaturesMixedScipy import cutDendrogramByFeaturesMixed as cutDendrogramByFeaturesMixedScipy
from scipyAlgorithm.analyser import analyser as analyserScipy
import env

scipyRouter = APIRouter()


@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/createDendrogram")
async def createDendrogram(codebaseName, dendrogramName):
    createDendrogramScipy(env.CODEBASES_PATH, codebaseName, dendrogramName)
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "operation": "createDendrogram"}


@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/createDendrogram/classes")
async def createDendrogramByClasses(codebaseName, dendrogramName):
    createDendrogramByClassesScipy(env.CODEBASES_PATH, codebaseName, dendrogramName)
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "operation": "createDendrogram"}


@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/createDendrogram/features/methodsCalls")
async def createDendrogramByFeaturesMethodCalls(codebaseName, dendrogramName):
    createDendrogramByFeaturesMethodCallsScipy(env.CODEBASES_PATH, codebaseName, dendrogramName)
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "operation": "createDendrogram"}

@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/createDendrogram/features/entities")
async def createDendrogramByFeaturesEntities(codebaseName, dendrogramName):
    createDendrogramByFeaturesEntitiesScipy(env.CODEBASES_PATH, codebaseName, dendrogramName)
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "operation": "createDendrogram"}

@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/createDendrogram/features/mixed")
async def createDendrogramByFeaturesMixed(codebaseName, dendrogramName):
    createDendrogramByFeaturesMixedScipy(env.CODEBASES_PATH, codebaseName, dendrogramName)
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "operation": "createDendrogram"}


@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/{graphName}/{cutType}/{cutValue}/cut")
async def cutDendrogram(codebaseName, dendrogramName, graphName, cutType, cutValue):
    cutDendrogramScipy(env.CODEBASES_PATH, codebaseName, dendrogramName, graphName, cutType, float(cutValue))
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "graphName": graphName,
            "cutType": cutType, "cutValue": cutValue, "operation": "cutDendrogram"}

@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/{graphName}/{cutType}/{cutValue}/cut/classes")
async def cutDendrogramByClasses(codebaseName, dendrogramName, graphName, cutType, cutValue):
    cutDendrogramByClassesScipy(env.CODEBASES_PATH, codebaseName, dendrogramName, graphName, cutType, float(cutValue))
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "graphName": graphName,
            "cutType": cutType, "cutValue": cutValue, "operation": "cutDendrogram"}


@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/{graphName}/{cutType}/{cutValue}/cut/features/methodCalls")
async def cutDendrogramByFeaturesMethodCalls(codebaseName, dendrogramName, graphName, cutType, cutValue):
    cutDendrogramByFeaturesMethodCallsScipy(env.CODEBASES_PATH, codebaseName, dendrogramName, graphName, cutType, float(cutValue))
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "graphName": graphName,
            "cutType": cutType, "cutValue": cutValue, "operation": "cutDendrogram"}

@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/{graphName}/{cutType}/{cutValue}/cut/features/entities")
async def cutDendrogramByFeaturesEntities(codebaseName, dendrogramName, graphName, cutType, cutValue):
    cutDendrogramByFeaturesEntitiesScipy(env.CODEBASES_PATH, codebaseName, dendrogramName, graphName, cutType, float(cutValue))
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "graphName": graphName,
            "cutType": cutType, "cutValue": cutValue, "operation": "cutDendrogram"}
            
@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/{graphName}/{cutType}/{cutValue}/cut/features/mixed")
async def cutDendrogramByFeaturesMixed(codebaseName, dendrogramName, graphName, cutType, cutValue):
    cutDendrogramByFeaturesMixedScipy(env.CODEBASES_PATH, codebaseName, dendrogramName, graphName, cutType, float(cutValue))
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "graphName": graphName,
            "cutType": cutType, "cutValue": cutValue, "operation": "cutDendrogram"}

@scipyRouter.get("/scipy/{codebaseName}/{totalNumberOfEntities}/analyser")
async def anayser(codebaseName, totalNumberOfEntities):
    analyserScipy(env.CODEBASES_PATH, codebaseName, int(totalNumberOfEntities))
    return {"codebaseName": codebaseName, "totalNumberOfEntities": totalNumberOfEntities, "operation": "analyser"}
