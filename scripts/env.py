import os

CODEBASES_PATH = os.getenv("CODEBASES_PATH", "/codebases/")
PORT = int(os.getenv('PORT', "5002"))
RESULTS_PATH = "/results"
EVALUATION_PATH = "/evaluation"

