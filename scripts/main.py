from fastapi import FastAPI
import scipyAlgorithm.scipyController as scipyController
import uvicorn
import env

app = FastAPI()

# ROUTERS
# To add different algorithms, create a new router and include it here
app.include_router(scipyController.scipyRouter)


if __name__ == "__main__":
    uvicorn.run("main:app", reload=True, host="0.0.0.0", port=env.PORT)
