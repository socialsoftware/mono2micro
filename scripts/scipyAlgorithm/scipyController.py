from fastapi import APIRouter
from scipyAlgorithm.createDendrogram import createDendrogram as createDendrogramScipy
import env
import glob

scipyRouter = APIRouter()

@scipyRouter.get("/status")
async def status():
    print("asdofijasdoifjasdfoij")
    mylist = [f for f in glob.glob("/codebases/ACME Academy/*")]
    print(mylist)
    return {"user_id": "HELLO WORLD"}


@scipyRouter.get("/scipy/{codebaseName}/{dendrogramName}/createDendrogram")
async def createDendrogram(codebaseName, dendrogramName):
    createDendrogramScipy(env.CODEBASES_PATH, codebaseName, dendrogramName)
    return {"codebaseName": codebaseName, "dendrogramName": dendrogramName, "operation": "createDendrogram"}
