from fastapi import FastAPI
from pydantic import BaseModel
from transformers import pipeline
import torch

app = FastAPI()

pipe = pipeline(
    "text-classification", 
    model="./checkpoint-1344", 
    tokenizer="./checkpoint-1344"
)

class TextRequest(BaseModel):
    text: str

@app.post("/analyze")
async def analyze(request: TextRequest):
    result = pipe(request.text)[0]
    return {
        "fallacy": result['label'],
        "confidence": float(result['score'])
    }

@app.get("/health")
async def health():
    return {"status": "ready"}