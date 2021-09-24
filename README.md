# PubCrawler
Pub crawler demo app demonstrating some Ably capabilities.

Note: Work in progress

This is an app listing pubs in the UK (pubs data downloaded from https://www.getthedata.com/open-pubs) 

Using this app, you can
- List the pubs nearby your location (For the demo, the center of the map is going to be used as your location)
- View a pub and see how many people are there, Number of people should change when someone enter or exits a particular pub
- Join a pub
- Be aware when someone joins, have options to say 'hi'
- Be able to offer a drink to somebody in the same pub.
- Be able to accept / reject a drink.

To run this app simply checkout the repo
- Open project in Android Studio (Arctic Fox 2020.3.1 Patch 2 was used to create the project)
- For Google maps, you should set your API key on your local.properties file
 MAPS_API_KEY = {your maps API Key} -Please reach to me internally if you want to use my key
 ABLY_KEY={your Ably API key)
 
-Simply build and run the project on a device or emulator
