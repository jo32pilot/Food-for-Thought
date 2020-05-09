import sys
sys.path.append('..')
import webscrapers

scraper = webscrapers.FoodNetwork()
scraper.parse()
scraper._print_links()
scraper.scrape()
scraper._print_recipes()
scraper.upload()
