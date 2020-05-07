import sys
sys.path.append('/home/jo32pilot/cse110/')

import webscrapers

scraper = webscrapers.FoodNetwork()
scraper.parse()
scraper._print_links()
scraper.scrape()
scraper._print_recipes()
scraper.upload()
