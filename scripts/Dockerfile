FROM python:3.7.4

COPY requirements.txt requirements.txt

RUN pip install --no-cache-dir --upgrade -r requirements.txt

COPY . .

CMD [ "python", "./main.py" ]
