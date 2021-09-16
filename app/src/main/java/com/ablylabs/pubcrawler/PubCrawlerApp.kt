package com.ablylabs.pubcrawler

import android.app.Application
import android.util.Log
import com.ablylabs.pubcrawler.pubservice.PubsStore
import com.ablylabs.pubcrawler.pubservice.geo.GeolocationTree

class PubCrawlerApp : Application() {
    private val TAG = "PubCrawlerApp"
    lateinit var pubsStore: PubsStore
    override fun onCreate() {
        super.onCreate()

        pubsStore = PubsStore(GeolocationTree(), resources.openRawResource(R.raw.pubs))
        pubsStore.loadData()

        Log.d(TAG, "onCreate: ")
    }
}