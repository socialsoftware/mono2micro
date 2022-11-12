FROM python:3.7.4

WORKDIR /app

RUN apt-get update && \
    apt-get install -y openjdk-11-jdk ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f

ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64/

RUN export JAVA_HOME

COPY requirements.txt requirements.txt

RUN pip install --no-cache-dir --upgrade -r requirements.txt

COPY . .

CMD [ "python", "./main.py" ]
