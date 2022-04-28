from fastapi import APIRouter
from plots.plot import plotStats as plotStatsService
import env

plotsRouter = APIRouter()

@plotsRouter.get("/{codebaseName}")
async def plotStats(codebaseName):
    plotStatsService(env.CODEBASES_PATH, codebaseName)
    return {"codebaseName": codebaseName, "operation": "plotStats"}