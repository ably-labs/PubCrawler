package com.ablylabs.pubcrawler

import android.app.Application
import android.util.Log
import com.ablylabs.pubcrawler.pubservice.PubsStore
import com.ablylabs.pubcrawler.pubservice.geo.GeolocationTree
import io.ably.lib.realtime.AblyRealtime
import io.ably.lib.realtime.Channel
import io.ably.lib.realtime.ConnectionState
import io.ably.lib.realtime.ConnectionStateListener.ConnectionStateChange

import io.ably.lib.realtime.ConnectionStateListener
import io.ably.lib.types.Message


class PubCrawlerApp : Application() {
    /*
      Channel channel = ably.channels.get("test");

      channel.publish("greeting", "hello");*/
    private lateinit var ably: AblyRealtime
    private var channel: Channel? = null

    private val TAG = "PubCrawlerApp"
    override fun onCreate() {
        super.onCreate()
        val inputStream = resources.openRawResource(R.raw.pubs)

    }


}