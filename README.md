# Kialo Assistant
## Deployment
- Make .env file out of .env.example
- Optionally add OPENAI_API_KEY and GOOGLE_SECRET_KEY (application will run, but not properly)
- Downdload model.safetensors from "https://drive.google.com/drive/u/2/folders/1bAIetvpFpkq0zIOuoUn2xTVmc7sMIrb4" (skachdav-bp-shared -> checkpoint-1344)
- Put model.safetensors into /fallacy-detector/checkpoint-1344 folder
- run "docker compose up --build"