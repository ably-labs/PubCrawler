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

        val inputStream = resources.openRawResource(R.raw.pubs)
        pubsStore = PubsStore(GeolocationTree(), inputStream)
        val now = System.currentTimeMillis()
        pubsStore.loadData()
        val later = System.currentTimeMillis()
        Log.d(TAG, "Pubs load time ${later-now}ms ")

        val nearestPubsResult = pubsStore.findNearbyPubs(51.454514, -2.587910, 2)
        when(nearestPubsResult){
            is PubsStore.NearestPubsResult.PubsFound ->
                nearestPubsResult.pubs.forEach { Log.d(TAG, it.name) }
            is PubsStore.NearestPubsResult.Error -> Log.d(TAG, "There was an error finding nearby pub")
            PubsStore.NearestPubsResult.NoPubs -> Log.d(TAG, "Sorry, no pubs found here")
        }
    }
}