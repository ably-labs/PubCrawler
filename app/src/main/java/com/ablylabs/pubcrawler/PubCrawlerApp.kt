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
    override fun onCreate() {
        super.onCreate()
        val inputStream = resources.openRawResource(R.raw.pubs)

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