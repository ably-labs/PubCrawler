package com.ablylabs.pubcrawler

import android.app.Application
import com.ablylabs.pubcrawler.pubs.PubsStore
import com.ablylabs.pubcrawler.pubs.geo.GeolocationTree
import com.ablylabs.pubcrawler.pubs.search.TernarySearchTree
import com.ablylabs.pubcrawler.realtime.RealtimePub
import io.ably.lib.realtime.AblyRealtime


class PubCrawlerApp : Application() {
    //these shouldn't be exposed like this in production app!!
    lateinit var realtimePub: RealtimePub
    lateinit var pubsStore: PubsStore

    override fun onCreate() {
        super.onCreate()
        instance = this
        realtimePub = RealtimePub(AblyRealtime(getString(R.string.ably_key)))
        val inputStream = resources.openRawResource(R.raw.pubs)
        pubsStore = PubsStore(GeolocationTree(), TernarySearchTree(), inputStream)
    }

    companion object {
        private lateinit var instance: PubCrawlerApp
        fun instance() = instance
    }
}