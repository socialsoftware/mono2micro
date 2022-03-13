from fastapi import APIRouter, Body, Depends, HTTPException

from dto.MethodDeclaration import MethodDeclaration
from dto.MethodPredict import MethodPredict

from code2vec.config import Config
from code2vec.tensorflow_model import Code2VecModel
from code2vec.rest_predict import InteractivePredictor

config = Config(set_defaults=True, load_from_args=False, verify=True)
# model = Code2VecModel(config)
# print('[+] Created session for code2vec model!')
# predictor = InteractivePredictor(config, model)
model = None
predictor = None
print('[+] Fast Load, model not initialized yet!')

router = APIRouter()

@router.post("/predict", response_model=MethodPredict)
async def predict(req: MethodDeclaration):
    global model, predictor
    print("[+] Predicting code embedding for method: " + req.name)
    if model == None:
        model = Code2VecModel(config)
        print('[+] Created session for code2vec model!')
        predictor = InteractivePredictor(config, model)
    return predictor.predict(req.name, req.body)

@router.on_event("shutdown")
def shut_down():
    global model
    if model != None:
        print("[+] Closing code2vec session...")
        model.close_session()