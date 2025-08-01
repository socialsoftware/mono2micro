#!/bin/bash

if [ -d "./endToEnd/django-banking-app-test/django-banking-app-test-db" ]; then
  echo "CodeQL database django-banking-app-test-db already exists. Skipping creation."
else
  echo "Creating CodeQL database django-banking-app-test-db..."
  codeql database create --language=python --source-root=./endToEnd/django-banking-app-test -- ./endToEnd/django-banking-app-test/django-banking-app-test-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi

if [ -d "./endToEnd/django-blog-app-test/django-blog-app-test-db" ]; then
  echo "CodeQL database django-blog-app-test-db already exists. Skipping creation."
else
  echo "Creating CodeQL database django-blog-app-test-db..."
  codeql database create --language=python --source-root=./endToEnd/django-blog-app-test -- ./endToEnd/django-blog-app-test/django-blog-app-test-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi

if [ -d "./endToEnd/django-socialmedia-app-test/django-socialmedia-app-test-db" ]; then
  echo "CodeQL database django-socialmedia-app-test-db already exists. Skipping creation."
else
  echo "Creating CodeQL database django-socialmedia-app-test-db..."
  codeql database create --language=python --source-root=./endToEnd/django-socialmedia-app-test -- ./endToEnd/django-socialmedia-app-test/django-socialmedia-app-test-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi

if [ -d "./endToEnd/fenix-banking-app-test/fenix-banking-app-test-db" ]; then
  echo "CodeQL database fenix-banking-app-test-db already exists. Skipping creation."
else
  echo "Creating CodeQL database fenix-banking-app-test-db..."
  codeql database create --language=java --source-root=./endToEnd/fenix-banking-app-test -- ./endToEnd/fenix-banking-app-test/fenix-banking-app-test-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi



if [ -d "./endToEnd/springboot-Angular-CRUD-Full-Stack-App/springboot-Angular-CRUD-Full-Stack-App-db" ]; then
  echo "CodeQL database springboot-Angular-CRUD-Full-Stack-App-db already exists. Skipping creation."
else
  echo "Creating CodeQL database springboot-Angular-CRUD-Full-Stack-App-db..."
  codeql database create --language=java --source-root=./endToEnd/springboot-Angular-CRUD-Full-Stack-App -- ./endToEnd/springboot-Angular-CRUD-Full-Stack-App/springboot-Angular-CRUD-Full-Stack-App-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi

if [ -d "./endToEnd/springboot-REACT-CRUD-Full-Stack-App/springboot-REACT-CRUD-Full-Stack-App-db" ]; then
  echo "CodeQL database springboot-REACT-CRUD-Full-Stack-App-db already exists. Skipping creation."
else
  echo "Creating CodeQL database springboot-REACT-CRUD-Full-Stack-App-db..."
  codeql database create --language=java --source-root=./endToEnd/springboot-REACT-CRUD-Full-Stack-App -- ./endToEnd/springboot-REACT-CRUD-Full-Stack-App/springboot-REACT-CRUD-Full-Stack-App-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi

if [ -d "./endToEnd/springboot-thymeleaf-crud-pagination-sorting-webapp/springboot-thymeleaf-crud-pagination-sorting-webapp-db" ]; then
  echo "CodeQL database springboot-thymeleaf-crud-pagination-sorting-webapp-db already exists. Skipping creation."
else
  echo "Creating CodeQL database springboot-thymeleaf-crud-pagination-sorting-webapp-db..."
  codeql database create --language=java --source-root=./endToEnd/springboot-thymeleaf-crud-pagination-sorting-webapp -- ./endToEnd/springboot-thymeleaf-crud-pagination-sorting-webapp/springboot-thymeleaf-crud-pagination-sorting-webapp-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi