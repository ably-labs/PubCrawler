package com.ablylabs.pubcrawler

import android.app.Application
import com.ablylabs.pubcrawler.pubs.PubsStore
import com.ablylabs.pubcrawler.pubs.geo.GeolocationTree
import com.ablylabs.pubcrawler.pubs.search.TernarySearchTree
import com.ablylabs.pubcrawler.realtime.ExpensiveRealtimePub
import io.ably.lib.realtime.AblyRealtime


class PubCrawlerApp : Application() {
    //these shouldn't be exposed like this in production app!!
    lateinit var realtimePub: ExpensiveRealtimePub
    lateinit var pubsStore: PubsStore
    lateinit var ablyRealtime: AblyRealtime

    override fun onCreate() {
        super.onCreate()
        instance = this
        ablyRealtime = AblyRealtime(getString(R.string.ably_key))
        realtimePub = ExpensiveRealtimePub(ablyRealtime)
        val inputStream = resources.openRawResource(R.raw.pubs)
        pubsStore = PubsStore(GeolocationTree(), TernarySearchTree(), inputStream)
    }

    companion object {
        private lateinit var instance: PubCrawlerApp
        fun instance() = instance
    }
}