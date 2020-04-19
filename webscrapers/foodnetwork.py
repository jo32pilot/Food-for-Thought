"""

Scraper for foodnetwork.com. Collectis all recipe links from website.

Author: John Li

"""

from .scraper_base import ScraperBase
from selectolax.parser import HTMLParser
import json
import requests
import string
import re

class FoodNetwork(ScraperBase):
    """ Class for scraping recipe links from foodnetwork.com. """

    def __init__(self):
        """ Default constructor. Mainly sets up parent class. """
        with open('base_links.json', 'r') as f:
            links = json.load(f)
            site = 'foodnetwork'
            super().__init__(site, links[site])
        
    def parse(self):
        """ Scrapes website for recipe links. """
    
        # how recipe links should look like in regex
        pattern = r'.*foodnetwork\.com/recipes/.*\d{7}'

        # list or recipes are organized alphabetically on website,
        # so just append letters to base link.
        page_suffix = list(string.ascii_lowercase)
        page_suffix.append('123')
        page_suffix.append('xyz')
        for suffix in page_suffix:
            response = requests.get(self.base_link + suffix)
            parser = HTMLParser(response.text)
            anchors_nodes = parser.tags('a')
            for anchor_node in anchors_nodes: 
                link = anchor_node.attributes['href'] if 'href' in anchor_node.attributes else ''
                if re.fullmatch(pattern, link):
                    self.links.add('http:' + link)

