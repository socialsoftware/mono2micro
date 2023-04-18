from sys import prefix
from fastapi import FastAPI
from starlette.middleware.cors import CORSMiddleware
import scipyAlgorithm.scipyController as scipyController
import code2vec.controller as code2vecController
import uvicorn
import env

app = FastAPI(title="Fast API Server")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ROUTERS
# To add different algorithms, create a new router and include it here
app.include_router(scipyController.scipyRouter)
app.include_router(code2vecController.router, prefix='/code2vec')

if __name__ == "__main__":
    uvicorn.run("main:app", reload=True, host="0.0.0.0", port=env.PORT, workers=3)
