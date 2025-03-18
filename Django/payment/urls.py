from django.urls import path
from . import views

# urls.py
urlpatterns = [
    path('paymentandip', views.detect_fraud),
]
