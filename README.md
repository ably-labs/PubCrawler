# PubCrawler
Pub crawler demo app.

Note: Work in progress

This is an app listing pubs in the UK (pubs data downloaded from https://www.getthedata.com/open-pubs) with a realtime layer
built on top to demonstrate Ably capabilities

Features
- Nearby location search
- Keyword search for pubs
- Observation of presence at particular pubs
- Join a pub using Ably's presence feature
- Option to send a simple message when in a particular pub using Ably's publish/subscribe features
- Offer a drink and follow up with back and forth communication using Ably's publish/subscribe features

To run this app simply checkout the repo
- Open project in Android Studio (Arctic Fox 2020.3.1 Patch 2 was used to create the project)

 For Google maps, you should set your API key on your local.properties file
 - MAPS_API_KEY = {your maps API Key} Please reach to me internally if you want to use my key

 For Ably key 
 - ABLY_KEY={your Ably API key)
 
-Simply build and run the project on a device or emulator
