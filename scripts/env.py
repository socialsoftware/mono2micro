import os

CODEBASES_PATH = os.getenv("CODEBASES_PATH", "/codebases/")
PORT = int(os.getenv('PORT', "5002"))
MONGO_DB = os.getenv('MONGO_DB', "mongodb://mono2micro:mono2microPass@mongo")
MONGO_DB_NAME = os.getenv('MONGO_DB_NAME', "mono2micro")
