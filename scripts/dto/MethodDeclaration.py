from pydantic import BaseModel
from typing import Optional

class MethodDeclaration(BaseModel):
    name: Optional[str]
    body: str
