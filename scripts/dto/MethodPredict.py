from pydantic import BaseModel
from typing import List, Optional

from dto.AttentionPath import AttentionPath
from dto.NamePrediction import NamePrediction

class MethodPredict(BaseModel):
    name: Optional[str]
    namePredictions: List[NamePrediction]
    attentionPaths: List[AttentionPath]
    code_vector: List[float]
