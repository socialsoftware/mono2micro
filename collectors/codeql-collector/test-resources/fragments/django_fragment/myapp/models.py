from django.db import models

# Base model
class Animal(models.Model):
    name = models.CharField(max_length=100)
    age = models.IntegerField()

# Inheriting model
class Dog(Animal):
    breed = models.CharField(max_length=100)
