## Installing Dependencies

In the root of the `scrapers/` directory, run `pip install -r requirements.txt`.  

Now if you try to run the tester, you'll most likely get an error with a path
along the lines of 

```
 File "/home/jo32pilot/.local/lib/python3.5/site-packages/extruct/rdfa.py", line 14, in <module>
    from rdflib.plugins.parsers.pyRdfa import pyRdfa as PyRdfa, Options, logger as pyrdfa_logger
```

So open `rdfa.py` and comment out some lines until it looks like the following:

```
from rdflib import Graph, logger as rdflib_logger
#from rdflib.plugins.parsers.pyRdfa import pyRdfa as PyRdfa, Options, logger as pyrdfa_logger
#from rdflib.plugins.parsers.pyRdfa.initialcontext import initial_context

from extruct.utils import parse_xmldom_html


# silence rdflib/PyRdfa INFO logs
rdflib_logger.setLevel(logging.ERROR)
#pyrdfa_logger.setLevel(logging.ERROR)
"""
initial_context["http://www.w3.org/2011/rdfa-context/rdfa-1.1"].ns.update({
    "twitter": "https://dev.twitter.com/cards#",
    "fb": "http://ogp.me/ns/fb#"
})
"""
```

## Implementing the Scraper

To develop a scraper for one of the [recipe-scraper](https://github.com/hhursev/recipe-scrapers)
supported websites, perform the following 
1. In `webscrapers/` make a copy of the provided `example.py` file and rename it to 
`{name_of_website}.py`. For example, `foodnetwork.py`. 
2. In `{name_of_website}.py`, rename the class to `{NameOfWebsite}`. 
For example `FoodNetwork`.
3. In `base_links.json` add a `{key} : {value}` pair similar to the ones already
in there. The key should be the name of the website.
The value should be where you start scraping the website from. Generally this
will part of the websites that lists out all the recipes.
4. In `webscrapers/__init__.py` add the a line similar to the ones already in 
there. The format is  
`from .{name_of_website} import {NameOfWebsite}`.
5. In `{name_of_website}.py`, change the `site` variable in `__init__()`
to be the same as the key you added to `base_links.json`
6. Implement the `parse()` method. This should gather all of the website's links
to recipes into the `self.links` set of the `ScraperBase` parent class. 
Preferably you would use the `selectolax` HTML parser to parse the website as it
is 5 times faster than `BeautifulSoup`, but if you are more familiar with
`BeautifulSoup`, you can use that instead. I'd also advise using regex as it
might save you a lot of time.

## Running the Tester

The tester isn't really much of a tester as all it does is print out the results
of the scraping. To test your parser you can basically copy what's already in 
the given tester file. Be sure to be in the `tests/` directory when you run the 
tester.
