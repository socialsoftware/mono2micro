# fastapi

## Download a code2vec train model

```sh
mkdir models
cd models
wget https://code2vec.s3.amazonaws.com/model/java-large-released-model.tar.gz
tar -xf java-large-released-model.tar.gz
```

## Test endpoint

```
 curl http://localhost:5002/code2vec/predict -H "accept: application/json" -H "Content-Type: application/json" -d "{\"name\":\"Vasco\",\"body\":\"public class Test { public String ola() { return "oi"; }}\" }"
```