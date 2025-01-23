FROM  quay.io/jupyter/scipy-notebook:2024-11-19 as runtime

#WORKDIR /home/jovyan
COPY requirements.txt requirements.txt

RUN pip install -r requirements.txt


ENTRYPOINT ["start.sh", "jupyter", "lab"]