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

if [ -d "./endToEnd/ruby-on-rails-blog-app/ruby-on-rails-blog-app-db" ]; then
  echo "CodeQL database ruby-on-rails-blog-app-db already exists. Skipping creation."
else
  echo "Creating CodeQL database ruby-on-rails-blog-app-db..."
  codeql database create --language=ruby --source-root=./endToEnd/ruby-on-rails-blog-app -- ./endToEnd/ruby-on-rails-blog-app/ruby-on-rails-blog-app-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi

if [ -d "./endToEnd/ruby-on-rails-realworld-example-app/ruby-on-rails-realworld-example-app-db" ]; then
  echo "CodeQL database ruby-on-rails-realworld-example-app-db already exists. Skipping creation."
else
  echo "Creating CodeQL database ruby-on-rails-realworld-example-app-db..."
  codeql database create --language=ruby --source-root=./endToEnd/ruby-on-rails-realworld-example-app -- ./endToEnd/ruby-on-rails-realworld-example-app/ruby-on-rails-realworld-example-app-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi

if [ -d "./endToEnd/ruby-on-rails-todo-app/ruby-on-rails-todo-app-db" ]; then
  echo "CodeQL database ruby-on-rails-todo-app-db already exists. Skipping creation."
else
  echo "Creating CodeQL database ruby-on-rails-todo-app-db..."
  codeql database create --language=ruby --source-root=./endToEnd/ruby-on-rails-todo-app -- ./endToEnd/ruby-on-rails-todo-app/ruby-on-rails-todo-app-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi

if [ -d "./fragments/django_fragment/django-db" ]; then
  echo "CodeQL database django-db already exists. Skipping creation."
else
  echo "Creating CodeQL django-db..."
  codeql database create --language=python --source-root=./fragments/django_fragment -- ./fragments/django_fragment/django-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi

if [ -d "./fragments/fenix-fragment/fenix-db" ]; then
  echo "CodeQL database fenix-db already exists. Skipping creation."
else
  echo "Creating CodeQL database fenix-db..."
  codeql database create --language=java --source-root=./fragments/fenix-fragment -- ./fragments/fenix-fragment/fenix-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi

if [ -d "./fragments/ruby-on-rails/ruby-on-rails-db" ]; then
  echo "CodeQL database ruby-on-rails-db already exists. Skipping creation."
else
  echo "Creating CodeQL database ruby-on-rails-db..."
  codeql database create --language=ruby --source-root=./fragments/ruby-on-rails -- ./fragments/ruby-on-rails/ruby-on-rails-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi

if [ -d "./fragments/spring-data-jpa/spring-db" ]; then
  echo "CodeQL database spring-db already exists. Skipping creation."
else
  echo "Creating CodeQL database ruby-on-rails-todo-app-db..."
  codeql database create --language=java --source-root=./fragments/spring-data-jpa -- ./fragments/spring-data-jpa/spring-db
  if [ $? -ne 0 ]; then
    echo "Failed to create CodeQL database."
    exit 1
  fi
fi