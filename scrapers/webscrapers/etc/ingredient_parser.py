"""

File for ingredient parser implemented using a linked list.
Used to make ingredients parsing faster relative to using a list to parse.
Each removal operation is O(1) instead of O(n) where n is the number of words
in the ingredient description.


Author: John Li

"""

import re

TO_STRIP = {'pinch', 'dash', 'teaspoons', 'teaspoon', 'fluid', 'oz', 
        'tablespoons', 'tablespoon', 'cup', 'cups', 'ounce', 'ounces', 
        'pint', 'pints', 'quart', 'quarts', 'milliliter', 'milliters', 
        'liter', 'liters', 'gram', 'grams', 'kilogram', 'kilograms', 
        'pound', 'pounds', 'large', 'medium', 'small', 'gallon', 'gallons',
        'sprig', 'sprigs', 'ground', 'piece', 'pieces', 'of', 'to', 
        'bottle', 'bottles', 'carton', 'cartons', 'slice', 'slices',
        'fresh', 'freshly', 'hard', 'boiled', 'boiling', 'chopped',
        'yolk', 'fine', 'finely', 'clove', 'cloves', 'zest', 'diced',
        'lbs', 'TBSP', 'tsp.', 'tsp', 'taste', 'g', 'ml', 'sharp',
        'grated', 'lb.', 'can', 'stick'}

RM_EVERYTHING_AFTER = {'for', ','}

DELIMITERS = {',', ' ', '-'}

PATTERNS = [r'[0-9]+/?(?<=/)[0-9]+|[0-9]*', r'\(.*\)']

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

    def join(self):
        """ Combines strings in list 
        
        Returns:
            All of the strings in the list combined

        """
        curr = self._head.next
        self._size = 0
        final_product = []
        while curr.data != None:
            self._size += 1
            final_product.append(curr.data)
            curr = curr.next

        return ' '.join(''.join(final_product).split())

    def parse(self):
        """ Parses ingredient to remove qualifiers and quantifiers. """
        curr = self._head.next

        # Remove unnecessary elements
        while curr.data != None:
            normalized = curr.data.lower()

            if normalized in RM_EVERYTHING_AFTER:
                # Remove curr node and everything after
                curr.prev.next = self._tail
                self._tail.prev = curr.prev
                curr = self._tail

            elif (any(re.fullmatch(pattern, normalized) for pattern in PATTERNS)
                    or normalized in TO_STRIP):
                self._rm_curr_node(curr)
                curr = curr.next

            else:
                curr = curr.next

        return self.join()


if __name__ == '__main__':
    while True:
        parser = IngredientParser(input())
        print(f'Unparsed: {parser.join()}')
        print(f'Parsed: {parser.parse()}')
