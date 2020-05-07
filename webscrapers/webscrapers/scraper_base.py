"""

File for base class of web scrapers. Also implements logic for uploading to
database.

Author: John Li

"""

import requests
from recipe_scrapers import scrape_me

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

    def scrape(self):
        """ Parses the links in the links set for the recipe information. """
        for link in self.links:
            scraper = scrape_me(link)
            try:
                recipe = {
                    'name': scraper.title(),
                    'total_time': scraper.total_time(),
                    'yield': scraper.yields(),
                    'ingredients': scraper.ingredients(),
                    'instructions': scraper.instructions(),
                    'image': scraper.image(),
                    'user_created': ''
                }
                self.recipes[link] = recipe
            except (AttributeError, NotImplementedError) as e: 
                continue
                # Log later
            


    def upload(self, db):
        """ Uploads recipes to database. 
        
        Params:
            db: databse to upload to.
        
        """
        recipe_key = 0
        recipes_ref = db.collection('recipes')
        for link, recipe in self.recipes.items():
            # TODO Temp key, need to make better leter
            recipe_ref = recipes_ref.document(f'{recipe_key}')
            recipe_ref.set(recipe)
            recipe_key += 1

    def _print_recipes(self):
        """ Prints recipes dictionary for testing / debugging. """
        print(self.recipes)

    def _print_links(self):
        """ Prints links set for testing / debugging. """
        print(self.links)

