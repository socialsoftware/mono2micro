from pydantic import BaseModel
from typing import List

class NamePrediction(BaseModel):
    probability: float
    name: List[str]
