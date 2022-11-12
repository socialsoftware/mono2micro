from pydantic import BaseModel

class AttentionPath(BaseModel):
    score: float
    startToken: str
    path: str
    endToken: str
