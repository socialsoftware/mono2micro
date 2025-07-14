from django.shortcuts import render
from django.http import JsonResponse
from django.views import View
from .models import Dog

# Function-based view
def create_dog(request):
    # Write Operation
    dog = Dog.objects.create(name="Buddy", age=3, breed="Labrador")

    # Function call
    return get_dog_info(dog.id)

# Helper function
def get_dog_info(dog_id):
    # Read Operation
    dog = Dog.objects.get(id=dog_id)
    return JsonResponse({
        "id": dog.id,
        "name": dog.name,
        "age": dog.age,
        "breed": dog.breed,
    })

# Class-based view
class DogListView(View):
    def get(self, request):
        dogs = Dog.objects.all()
        data = [{"name": dog.name, "breed": dog.breed} for dog in dogs]
        return JsonResponse({"dogs": data})
