FROM python:3.9-slim

WORKDIR /app

RUN apt-get update && \
    apt-get install -y \
    build-essential \
    libfreetype6-dev \
    libpng-dev \
    libopenblas-dev \
    liblapack-dev \
    git \
    default-jdk \
    ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f

RUN export JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:bin/java::") && \
    echo "JAVA_HOME=${JAVA_HOME}" >> /etc/environment

COPY requirements.txt requirements.txt

RUN pip install --no-cache-dir --upgrade -r requirements.txt

COPY . .

CMD [ "python", "./main.py" ]
