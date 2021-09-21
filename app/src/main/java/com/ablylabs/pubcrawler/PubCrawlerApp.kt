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
    override fun onCreate() {
        super.onCreate()

    }


}