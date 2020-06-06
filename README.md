# Introduction:


Food for Thought is an Android app that helps people to find recipes based off of ingredients in their inventory. There are five main components to our app. From left to right in the bottom of the navigation bar: a recipe page, a recipe feed on the main page, an inventory page, a shopping page, and a profile page. 

Users can place all the ingredients they want to buy in the shopping list page. When users have purchased the ingredients at the store, they can click the checkmark next to the ingredient and it will transfer the ingredients to their inventory list. 

Alternatively, users can add ingredients that they already own to their inventory page. Once they have items in their inventory, the user can see the recipes that can be made with the ingredients in their inventory on the main page. 


The recipe feed is created by taking the ingredients in the inventory and comparing them to ingredients in the database of recipes. Recipes that have a higher ratio of ingredients to total number of ingredients will display first in the feed.
 When a user clicks on a recipe in their feed, they can view which ingredients they have in their ingredient. In the top right corner, the user can save the recipe. 
 
 Other features include the ability to like and dislike recipes as well as commenting on recipes.
In addition, users have the ability to create their own custom recipes. On the recipe page, they can view their saved recipes as well as create new recipes. They can view their custom recipes on their profile page. 


# Login Credentials: 
Populated Test

1. Email: "populated@test.com"
2. Password: “populated”


Empty Test (note: nothing will appear in home page until adding ingredients to inventory)
1. Email: “empty@test.com”
2. Password: “emptytest”


Test Cases Account (has specific inventory used in Test Cases document, use Populated Test account if you want to test around freely):
1. Email: “du4yu@test.com”
2. Password “du4yutest”

# Requirements: 


1. Android phone or emulator with version Android 6.0+ (Marshmallow/SDK 23) to install
2. Google Drive App


# Installation Instructions: 

Install on physical device

1. From the settings app on your android device, click on Apps & Notifications -> Advanced -> Special app access -> Install unknown apps 
(Special Note for Samsung phones: click on Apps -> click on the three dots -> Special access -> Install unknown apps)

2. Click on Google Drive and select Allow from this source 
3. From your phone, click [here](https://drive.google.com/drive/folders/1dIIXy3aO0oAqRmydvhjhIfa0oB5SQ_gm?usp=sharing) to download the APK file or download it from the github repository under the Install folder. 
4. Select the APK file and open file with Package installer
5. Select Food For Thought from your apps to run the application

OR using ADB (emulator or physical)

1. Click [here](https://drive.google.com/drive/folders/1dIIXy3aO0oAqRmydvhjhIfa0oB5SQ_gm?usp=sharing) to download the APK file or download it from the github repository under the Install folder. 
2. Launch the emulator or connect an Android phone with USB debugging on.
3. Make sure `adb` is in your PATH variable or navigate to its location.
4. In a terminal, type `adb devices` to verify connected devices.
5. Type `adb device-name install /path/to/apk` or `adb -s device-name install /path/to/apk` to install to SD card/internal memory (recommended for emulators).
   Device name is optional to include, try without if you have having difficulties.

# How to Run: 


1. Once you have the app installed, look for the Food for Thought app among your applications
2. Tap on the Food for Thought application to run the program
3. At the login page, select the create account button  
4. Enter your personal information along with an email address and password
5. Once you have created an account, return and login with the information you used during sign-up

# Known Bugs:
Android back button not programmed. Currently just takes the user back to login page.

Going between pages too quickly may result in errors.

Ingredients may not display as checked off in some cases where the names are slightly different. For example, “unsalted butter” may be different from “butter”. The application errs on the side of needing to be more specific rather than general.

Recipes will check off ingredients even if the user does not have enough of them in the inventory. This is intended.

Sometimes recipes list multiple ingredients in one line, such as “salt and pepper”. This line may be checked off even if the user only has 1 of the ingredients.

Images for recipes may not exist (this is not really a bug but just the recipes we scraped).

Cannot currently unlike/undislike. If a recipe is already liked, pressing the like button does not unlike it.

