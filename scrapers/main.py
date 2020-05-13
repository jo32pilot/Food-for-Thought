"""

!!!!!!!!!!!!!!!!!




DO NOT RUN THIS. NEEDS WORK.




!!!!!!!!!!!!!!!!!!

"""


from webscrapers import get_db
from webscrapers.etc import IngredientParser
import webscrapers
import sys
import re

NUM_RECIPES = 3630

MAX = 10000

if __name__ == '__main__':
    """
    scraper = webscrapers.FoodNetwork()
    scraper.parse()
    scraper._print_links()
    scraper.scrape()
    scraper._print_recipes()
    db = get_db('config/food-for-thought-3bdc1-firebase-adminsdk-gy070-37fbe7d047.json')
    scraper.upload(db)
    """
    not_working = False
    ingredients_dict = {}
    recipe_update = {}

    db = get_db('config/food-for-thought-3bdc1-firebase-adminsdk-gy070-37fbe7d047.json')
    ingredients_ref = db.collection('ingredients')
    recipes_ref = db.collection('recipes')
    for i in range(11, 15):
        recipes_ref.document(f'{i}').delete()


    for count in range(15, NUM_RECIPES + 1):
        if count == MAX:
            break

        doc = recipes_ref.document(f'{count}').get()
        doc_dict = doc.to_dict()
        recipe_update[doc.id] = {
            'ingredients': []
        }

        for ingredient in doc_dict['ingredients']:

            parsed_ingredient = IngredientParser(ingredient).parse()

            for option_ingredient in parsed_ingredient:

                if re.fullmatch(r'\s*', option_ingredient):
                    print(f'not working: {ingredient}')
                    not_working = True

                if option_ingredient in ingredients_dict:
                    ingredients_dict[option_ingredient]['recipes'].append(doc.id)
                    continue
                else:
                    ingredients_dict[option_ingredient] = {
                        'recipes': [doc.id]
                    }

            recipe_update[doc.id]['ingredients'].append({
                'ingredient': ingredient,
                'parsed_ingredient': parsed_ingredient
            })

    if not_working:
        sys.exit(1)

    for ingredient, recipes in ingredients_dict.items():
        print(f"ingredient: {ingredient}")
        ingredient_ref = ingredients_ref.document(f'{ingredient}')
        ingredient_ref.set(recipes)

    for recipe_id, ingredients in recipe_update.items():
        recipe_ref = recipes_ref.document(f'{recipe_id}')
        recipe_ref.update(ingredients)


