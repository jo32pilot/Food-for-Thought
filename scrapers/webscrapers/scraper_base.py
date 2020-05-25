"""

File for base class of web scrapers. Also implements logic for uploading to
database.

Author: John Li

"""

import requests
from recipe_scrapers import scrape_me
from firebase_admin import firestore
from .etc import IngredientParser

class ScraperBase():
    """ Base class for recipe scrapers.

    Attributes:
        site_name (String): Name of the website. Should match the name in
            base_links.py.

        base_link (String): Where the scraper subclass will start parsing links 
            from. Should be the same as the link in base_links.py

        links (set): The set of recipe links from the parsed website.

        recipes (dict): Holds (link, dict) pairs where link is the recipe link
            and the inner dict holds (attribute, value) pairs where attributes
            are types of recipe information and value is the actual information.

        
    """


    def __init__(self, site_name, base_link):
        """ Constructor to specify which website this object is scraping for.

        Params:
            site_name (String): Name of the website. Should match the name in
                base_links.py.

            base_link (String): Where the scraper subclass will start parsing 
                links from. Should be the same as the link in base_links.py

        """
        self.site_name = site_name
        self.base_link = base_link
        self.links = set()
        self.recipes = dict()
        self.ingredients = dict()

    def scrape(self):
        """ Parses the links in the links set for the recipe information. """
        # TODO Temp key, need to make better later
        recipe_key = 0

        # go through every link
        for link in self.links:
            scraper = scrape_me(link)
            recipe_ingredients = []
            all_parsed = []

            # for every ingredient 
            for ingredient in scraper.ingredients():

                # parse ingredients
                parsed_list = IngredientParser(ingredient).parse()
                recipe_ingredients.append({
                    'ingredient': ingredient,
                    'parsed_ingredient': parsed_list
                })

                # add recipe key to list of recipes containing this ingredient
                for option_ingredient in parsed_list:
                    all_parsed.append(option_ingredient)
                    if option_ingredient in self.ingredients:
                        self.ingredients[option_ingredient]['recipes'].append(recipe_key)
                    else:
                        self.ingredients[option_ingredient] = {
                            'name': option_ingredient,
                            'recipes': [recipe_key]
                        }

            try:
                # create entry
                recipe = {
                    'name': scraper.title(),
                    'total_time': scraper.total_time(),
                    'yield': scraper.yields(),
                    'ingredients': recipe_ingredients,
                    'all_ingredients': all_ingredients,
                    'instructions': scraper.instructions(),
                    'image': scraper.image(),
                    'user_created': ''
                }
                self.recipes[recipe_key] = recipe
                recipe_key += 1
            except (AttributeError, NotImplementedError) as e: 
                continue
                # Log later
            


    def upload(self, db):
        """ Uploads recipes to database. 
        
        Params:
            db: databse to upload to.
        
        """
        # write recipes to db
        recipes_ref = db.collection('recipes')
        for key, recipe in self.recipes.items():
            recipe_ref = recipes_ref.document(f'{key}')
            recipe_ref.set(recipe)

        # write ingredients to db
        ingredients_ref = db.collection('ingredients')
        for ingredient in self.ingredients:
            ingredient_doc = ingredients_ref.document(ingredient)
            ingredient_exists = ingredient_doc.get().exists

            # merge with existing recipes if doc already exists in db
            if ingredient_exists:
                ingredient_doc.update({'recipes': firestore.ArrayUnion(ingredient['recipes'])})
            else:
                ingredient_doc.set(ingredient)

    def _print_recipes(self):
        """ Prints recipes dictionary for testing / debugging. """
        print(self.recipes)

    def _print_links(self):
        """ Prints links set for testing / debugging. """
        print(self.links)

