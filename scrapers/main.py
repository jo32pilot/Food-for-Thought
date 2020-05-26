"""
Main file to run to parse websites

Author: John Li

"""

from webscrapers import get_db
from webscrapers.etc import IngredientParser
from pprint import pprint
import webscrapers
import json
import sys
import re

if __name__ == '__main__':

    scraper = webscrapers.FoodNetwork()
    scraper.parse()
    scraper._print_links()
    scraper.scrape()
    scraper._print_recipes()
    db = get_db('config/food-for-thought-3bdc1-firebase-adminsdk-gy070-37fbe7d047.json')
    scraper.upload(db)
