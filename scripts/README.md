# Fast API Server

## Download a Code2Vec train model

```sh
mkdir models
cd models
wget https://code2vec.s3.amazonaws.com/model/java-large-released-model.tar.gz
tar -xf java-large-released-model.tar.gz
```

## Test Code2Vec predict endpoint

```
 curl http://localhost:5002/code2vec/predict -H "accept: application/json" -H "Content-Type: application/json" -d "{\"name\":\"helloWorld\",\"body\":\"public class HelloWorld { public String helloWorld() { return "Hello World!"; }}\" }"
```