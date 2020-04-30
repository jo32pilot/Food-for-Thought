"""

Template file for recipe scraper. Implment the followin TODOs for
full functionality.

Author: Your name

"""


from .scraper_base import ScraperBase
from selectolax.parser import HTMLParser
import json
import requests

# TODO change class name
class Example(ScraperBase):
    """ Template class for recipe scraping. Must subclass ScraperBase"""

    def __init__(self):
        """ Default constructor. Mainly sets up parent class. """
        with open('base_links.json', 'r') as f:
            links = json.load(f)
            site = '' # TODO replace with site name as in base_links.json
            super().__init__(site, links[site])
        
    def parse(self):
        """ Scrapes website for recipe links. """
        #TODO Implement
