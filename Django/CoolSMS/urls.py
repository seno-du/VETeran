from django.urls import path
from . import views


urlpatterns = [
    path("verify/", views.phone_auth),
    path("send_reservation_status/", views.send_reservation_status),
]
