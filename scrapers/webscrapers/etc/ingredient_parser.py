"""

File for ingredient parser implemented using a linked list.
Used to make ingredients parsing faster relative to using a list to parse.
Each removal operation is O(1) instead of O(n) where n is the number of words
in the ingredient description.


Author: John Li

"""

import re

# special cases: wanted to get rid of garlic {cloves} but cloves are
# their own thing too
TO_STRIP = {'pinch', 'dash', 'teaspoons', 'teaspoon', 'fluid', 
        'tablespoons', 'tablespoon', 'cup', 'cups', 'ounce', 'ounces', 
        'pint', 'pints', 'quart', 'quarts', 'milliliter', 'milliters', 
        'liter', 'liters', 'gram', 'grams', 'kilogram', 'kilograms', 
        'pound', 'pounds', 'large', 'medium', 'small', 'gallon', 'gallons',
        'sprig', 'sprigs', 'ground', 'piece', 'pieces', 'of', 'to', 
        'bottle', 'bottles', 'carton', 'cartons', 'slice', 'slices',
        'fresh', 'freshly', 'hard', 'boiled', 'boiling', 'chopped',
        'yolk', 'fine', 'finely', 'zest', 'diced', 'thin', 'thinly',
        'taste', 'sharp', 'inch', 'inches', 'sliced', 'with', 'generous',
        'grated', 'can', 'stick', 'a', 'few', 'dash', 'dashes',
        'really', 'sheet', 'one', 'two', 'three', 'four', 'five', 'six',
        'seven', 'eight', 'nine', 'store-bought', '-one', '-oz.',
        '-size', 'about', 'the', 'rind', '/', 'an', 'from', 'bunch',
        'bunches', 'coarsely', 'cooked', 'cubed', 'cubes', 'container'
        'containers', 'bag', 'bags', 'box', 'boxes', 'crushed', 'each',
        'plus', 'yolks', 'envelope', 'envelopes', 'garnish', 'good', 'quality',
        'good', 'half', 'handful', 'jar', 'julienned', 'leftover', 'millileter',
        'milliliters', 'minced', 'natural', 'brewed', 'other', 'part', 'parts', 
        'pack', 'package', 'packages', 'packed', 'packet', 'packets',
        'peeled', 'pkg', 'pkgs', 'premium', 'prepared', 'roughly',
        'round', 'rounded', 'sauteed', 'scant', 'palmful', 'scoop', 'scoops',
        'serving', 'suggestions', 'shredded', 'splash', 'squeezed',
        'store', 'bought', 'tbs.', 'uncooked', 'very', 'your', 'favourite',
        'favorite', 'rounds', 'pkg.', 'percent', 'whites', 'sticks'}


RM_EVERYTHING_AFTER = {'for', ','}

DELIMITERS = {',', ' ', '-', '/', ':', '*'}
MULT_INGREDIENT_DELIM = r'([/,\s]?(and|or|and/or)[/,\s]?)'

PATTERNS = [
    r'[0-9]+', 
    r'.*/[0-9]+', 
    r'\(.*\)', 
    r'[0-9]*\.[0-9]+', 
    r'[0-9]*(g|g\.|oz|oz\.|ml|ml\.|lb\.|lb|lbs|lbs\.|tsp|tsp\.|tbsp|tbsp\.)']

END_STRIP = '.-,/:!*? '
WHITE_SPACE = r'\s*'

OPEN_PAR = '('
CLOSE_PAR = ')'

class Node:
    """ Linked list node class. 
    
    Attributes:
        data: Generic data stored in the node
        next: Reference to next node in linked list
        prev: Reference to previous node in linked list.

    """

    def __init__(self, data):
        """ Constructor to initialize node data.

        Args:
            data: Data to initialize node with.

        """
        self.data = data
        self.next = None
        self.prev = None

class IngredientParser:
    """ Linked list class to use for ingredients parsing 
    
    Uses sentinal nodes.

    Attributes:
        _head: Reference to head node 
        _tail: Reference to tail node

    """

    def __init__(self, ingredient):
        """ Constructor to initialize linked list using ingredient string 
        
        Args:
            ingredients (string): Ingredient to parse
        
        """
        self._head = Node(None)
        self._tail = Node(None)
        self._head.next = self._tail
        self._tail.prev = self._head
        self._size = 0
        curr = self._head

        parenthesis = False
        # each word in string is a node in the linked list
        word = []
        for char in ingredient:

            # group everything in parenthesis as one word
            if char == OPEN_PAR:
                parenthesis = True
            elif char == CLOSE_PAR:
                parenthesis = False

            # also want delimiters in list
            if not parenthesis and char in DELIMITERS:

                # could be two delimiters right after each other
                if len(word) > 0:
                    self.append(''.join(word))
                    word = []
                self.append(char)

            else:
                word.append(char)

        # Might be one more word leftover
        if len(word) > 0:
            self.append(''.join(word))

    def append(self, elem):
        """ Appends element to end of the list

        Args:
            elem: Element to append

        """
        self.insert(self._size, elem)

    def insert(self, idx, elem):
        """ Inserts element at specified index

        Args:
            elem: Element to insert
            idx: Index to insert at

        """
        curr = self._get(idx)
        elem_node = Node(elem)
        elem_node.next = curr.next
        elem_node.next.prev = elem_node
        elem_node.prev = curr
        curr.next = elem_node
        self._size += 1

    def _get(self, idx):
        """ Gets node at specified index - 1

        Args:
            idx: Index of node to return

        Returns:
            The node at the specified index - 1

        """
        curr = self._head
        curr_idx = 0
        while curr_idx != idx:
            curr = curr.next
            curr_idx += 1
        return curr

    def _rm_curr_node(self, curr):
        """ Remove passed in node from list

        Args:
            curr: Node to remove

        """
        curr.prev.next = curr.next
        curr.next.prev = curr.prev

    def _recount_size(self):
        """ Gets size by counting number of nodes in list 
        
        Return:
            The number of valid nodes in the list
        """
        self._size = 0
        curr = self._head.next
        while curr.data != None:
            self._size += 1
            curr = curr.next
        return self._size


    def join_list(self):
        """ Combines strings in list 
        
        Returns:
            List of ingredient strings, all in lowercase

        """
        curr = self._head.next
        final_product = [[]]
        while curr.data != None:
            if re.fullmatch(MULT_INGREDIENT_DELIM, curr.data):
                self._rm_curr_node(curr)

                # check if one of the optional ingredients (delimited
                # by or / and) isn't just white space
                option_ingredient = final_product[len(final_product) - 1]
                if not re.fullmatch(WHITE_SPACE, ''.join(option_ingredient)):
                    final_product.append([])

            else:
                final_product[len(final_product) - 1].append(curr.data.lower())
            curr = curr.next

        # nested join to remove extra white space
        list_of_ingredients = []
        for ingredient in final_product:
            final_ingredient = ' '.join(''.join(ingredient).split()).strip(END_STRIP)
            if not re.fullmatch(WHITE_SPACE, final_ingredient):
                list_of_ingredients.append(final_ingredient)
        
        return list_of_ingredients

    def join(self):
        """ Combines string in list into one whole string

        Returns:
            Combined strings from list data

        """
        curr = self._head.next
        final_product = []
        while curr.data != None:
            final_product.append(curr.data)
            curr = curr.next

        # nested join to remove extra white space
        return ' '.join(''.join(final_product).split()) 

    def parse(self):
        """ Parses ingredient to remove qualifiers and quantifiers.

        Returns:
            List of ingredient strings

        """
        curr = self._head.next

        # Remove unnecessary elements
        while curr.data != None:
            normalized = curr.data.lower()

            if normalized in RM_EVERYTHING_AFTER:
                # Remove curr node and everything after
                curr.prev.next = self._tail

                # save previous _tail.prev just in case we remove too much
                # and want to go back
                prev_tail = self._tail.prev

                self._tail.prev = curr.prev
                result_string = self.join()

                # if everything is whitespace, we removed too much
                if re.fullmatch(WHITE_SPACE, result_string):

                    # unremove everything
                    curr.prev.next = curr
                    self._tail.prev = prev_tail

                    # but remove current node
                    self._rm_curr_node(curr)
                    curr = curr.next

                else:
                    curr = self._tail

            # else if element matches a regex pattern or is one of the words we
            # don't want, remove
            elif normalized in TO_STRIP or \
                    any(re.fullmatch(pattern, normalized) for pattern in PATTERNS):
                self._rm_curr_node(curr)
                curr = curr.next

            # otherwise we keep the word
            else:
                curr = curr.next

        self._recount_size()

        return self.join_list()


if __name__ == '__main__':
    while True:
        parser = IngredientParser(input())
        print(f'Unparsed: {parser.join()}')
        print(f'Parsed: {parser.parse()}')
