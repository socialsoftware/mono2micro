#!/bin/bash
set -e

echo "Generating CodeQL databases..."

codeql database create --language=python --source-root=./test-resources/test-1/django-blog-app --overwrite -- ./test-resources/test-1/test-1-db
codeql database create --language=python --source-root=./test-resources/test-2/django-socialmedia-app --overwrite -- ./test-resources/test-2/test-2-db
codeql database create --language=python --source-root=./test-resources/test-3/django-banking-app --overwrite -- ./test-resources/test-3/test-3-db

echo "All CodeQL databases created."