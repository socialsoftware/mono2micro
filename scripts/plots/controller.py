from fastapi import APIRouter
from plots.plot import plotStats as plotStatsService
import env

router = APIRouter()

@router.get("/{codebaseName}/{analysisType}/{strategy}")
async def plotStats(codebaseName, analysisType, strategy):
    plotStatsService(env.CODEBASES_PATH, codebaseName, analysisType, strategy)
    return {"codebaseName": codebaseName, "operation": "plotStats", "analysisType": analysisType, "strategy": strategy}

@router.get("/{codebaseName}/{analysisType}/")
async def plotStats(codebaseName, analysisType):
    plotStatsService(env.CODEBASES_PATH, codebaseName, analysisType, None)
    return {"codebaseName": codebaseName, "operation": "plotStats", "analysisType": analysisType}

@router.get("/{codebaseName}")
async def plotStats(codebaseName):
    plotStatsService(env.CODEBASES_PATH, codebaseName, "", None)
    return {"codebaseName": codebaseName, "operation": "plotStats", "analysisType": "static"}