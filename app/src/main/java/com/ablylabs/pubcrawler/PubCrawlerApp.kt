package com.ablylabs.pubcrawler

import android.app.Application
import android.util.Log
import com.ablylabs.pubcrawler.pubservice.PubsStore
import com.ablylabs.pubcrawler.pubservice.geo.GeolocationTree
import com.ablylabs.pubcrawler.realtime.RealtimePub
import io.ably.lib.realtime.AblyRealtime
import io.ably.lib.realtime.Channel
import io.ably.lib.realtime.ConnectionState
import io.ably.lib.realtime.ConnectionStateListener.ConnectionStateChange

import io.ably.lib.realtime.ConnectionStateListener
import io.ably.lib.types.Message


class PubCrawlerApp : Application() {
    //these shouldn't be exposed like this in production app!!
    lateinit var realtimePub: RealtimePub
    lateinit var pubsStore: PubsStore

    override fun onCreate() {
        super.onCreate()
        instance = this
        realtimePub = RealtimePub(AblyRealtime(getString(R.string.ably_key)))
        val inputStream = resources.openRawResource(R.raw.pubs)
        pubsStore = PubsStore(GeolocationTree(), inputStream)
    }

    companion object {
        private lateinit var instance: PubCrawlerApp
        fun instance() = instance
    }
}