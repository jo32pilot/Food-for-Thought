import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

from .scraper_base import ScraperBase
from .foodnetwork import FoodNetwork
from .allrecipes import AllRecipes

def get_db(key):
    cred = credentials.Certificate(key)
    firebase_admin.initialize_app(cred)
    return firestore.client()
