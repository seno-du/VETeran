from django.urls import path
from .views import predict_diagnosis

urlpatterns = [
    path('', predict_diagnosis, name='predict_diagnosis'),
]
