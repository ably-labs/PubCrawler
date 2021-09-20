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
    /*  AblyRealtime ably = new AblyRealtime("NLYSHA.zPeslg:0aBbLE54Dsylr0qW");
      Channel channel = ably.channels.get("test");

      channel.publish("greeting", "hello");*/
    private lateinit var ably: AblyRealtime
    private var channel: Channel? = null

    private val TAG = "PubCrawlerApp"
    lateinit var pubsStore: PubsStore
    override fun onCreate() {
        super.onCreate()
        val inputStream = resources.openRawResource(R.raw.pubs)
        pubsStore = PubsStore(GeolocationTree(), inputStream)

        //this needs to move to a background thread
        pubsStore.loadData()
        val now = System.currentTimeMillis()
        val nearestPubsResult = pubsStore.findNearbyPubs(51.454514, -2.587910, 2)
        val later = System.currentTimeMillis()
        Log.d(TAG, "Nearest point found time ${later - now}ms ")
        when (nearestPubsResult) {
            is PubsStore.NearestPubsResult.PubsFound ->
                nearestPubsResult.pubs.forEach { Log.d(TAG, it.name) }
            is PubsStore.NearestPubsResult.Error -> Log.d(
                TAG,
                "There was an error finding nearby pub"
            )
            PubsStore.NearestPubsResult.NoPubs -> Log.d(TAG, "Sorry, no pubs found here")
        }
        tryAbly()
    }

    //use ably
    fun tryAbly() {
        ably = AblyRealtime("NLYSHA.zPeslg:0aBbLE54Dsylr0qW")

        ably.connection.on(ConnectionStateListener { state ->
            Log.d(TAG, "connection state changed: ${state.current.name}")
            when (state.current) {
                ConnectionState.connected -> {
                    channel = ably.channels["test"]
                    channelSubscribe()
                    channel?.publish(Message("chat_messsage", " Hi there"))
                }
                ConnectionState.failed -> {
                }
            }
        })
        ably.connect()
    }
    fun channelSubscribe(){
        channel?.subscribe("chat_message", object : Channel.MessageListener {
            override fun onMessage(message: Message?) {
                Log.d(TAG, "onMessage: ${message?.data}")
            }

        })
    }
}