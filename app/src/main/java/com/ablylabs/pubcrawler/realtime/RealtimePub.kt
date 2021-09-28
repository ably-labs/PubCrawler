package com.ablylabs.pubcrawler.realtime

import android.util.Log
import com.ablylabs.pubcrawler.pubservice.Pub
import com.google.gson.JsonObject
import io.ably.lib.realtime.AblyRealtime
import io.ably.lib.realtime.CompletionListener
import io.ably.lib.realtime.ConnectionState
import io.ably.lib.realtime.ConnectionStateListener
import io.ably.lib.types.ErrorInfo
import io.ably.lib.types.Message
import io.ably.lib.types.MessageExtras
import io.ably.lib.types.PresenceMessage
import java.util.*

private const val TAG = "RealtimePub"

class RealtimePub(private val ably: AblyRealtime) {
    init {
        ably.connection.on(ConnectionStateListener { state ->
            when (state.current) {
                ConnectionState.connected -> {
                    //connected
                }
                ConnectionState.failed -> {
                }
            }
        })
    }

    //following two functions might merge later
    fun numberOfPeopleInPub(pub: Pub) = ably.channels[pub.name].presence.get().size

    fun join(who: PubGoer, which: Pub, joinResult: (success: Boolean) -> Unit) {
        ably.channels[which.name].presence.apply {
            enterClient(who.name, "no_data", object : CompletionListener {
                override fun onSuccess() {
                    joinResult(true)
                }

                override fun onError(reason: ErrorInfo?) {
                    joinResult(false)
                }
            })

        }
    }

    fun leave(who: PubGoer, which: Pub, leaveResult: (success: Boolean) -> Unit) {
        ably.channels[which.name].presence.apply {
            leaveClient(who.name, "no_data", object : CompletionListener {
                override fun onSuccess() {
                    leaveResult(true)
                }

                override fun onError(reason: ErrorInfo?) {
                    leaveResult(false)
                }
            })

        }
    }

    fun sendMessage(
        who: PubGoer, toWhom: PubGoer, messageText: String,
        messageSentResult: (success: Boolean) -> Unit
    ) {
        val message = Message("hi_message", messageText,who.name)
        //create a unidirectional channel
        val channelId = listOf(who, toWhom).hashCode().toString()
        Log.d(TAG, "sendMessage: $channelId")
        ably.channels[channelId]
            .publish(message, object : CompletionListener {
                override fun onSuccess() {
                    messageSentResult(true)
                }

                override fun onError(reason: ErrorInfo?) {
                    messageSentResult(false)
                }
            })
    }

    fun registerToMessages(pub: Pub, receiver: PubGoer,
                           messageReceived: (from: PubGoer, message: String) -> Unit) {
        allPubGoers(pub).forEach {
            //exclude yourself
            if (it != receiver){
                val channelId = listOf(it,receiver).hashCode().toString()
                Log.d(TAG, "registerToMessages: $channelId")
                ably.channels[channelId].subscribe("hi_message"){
                    Log.d(TAG, "registerToMessages: message received")
                    if (it.data is String) {
                        val from = PubGoer(it.clientId)
                        messageReceived(from,it.data as String)
                    }
                }
            }
        }
    }

    fun offerDrink(who: PubGoer, toWhom: PubGoer) {
        TODO()
    }

    fun acceptDrink(who: PubGoer, fromWhom: PubGoer) {
        TODO()
    }

    fun rejectDrink(who: PubGoer, fromWhom: PubGoer) {
        TODO()
    }

    //all pubgoers in a pub
    fun allPubGoers(which: Pub): List<PubGoer> {
        val messages = ably.channels[which.name].presence.get()
        messages?.let {
            if (it.isNotEmpty()) {
                return it.toList().map { PubGoer(it.clientId) }
            }
        }
        return listOf()
    }


    fun registerToDrinkOffers(pub: Pub, offerReceived: (pubGoer: PubGoer) -> Unit) {
        TODO()
    }

    fun registerToPresenceUpdates(pub: Pub, updated: (update: PubUpdate) -> Unit) {
        val observedActions = EnumSet.of(PresenceMessage.Action.enter, PresenceMessage.Action.leave)
        ably.channels[pub.name].presence.subscribe(observedActions) {
            Log.d(TAG, "registerToPresenceUpdates: ${it.action} ${it.clientId}")
            when (it.action) {
                PresenceMessage.Action.enter -> updated(PubUpdate.Join(PubGoer(it.clientId)))
                PresenceMessage.Action.leave -> updated(PubUpdate.Leave(PubGoer(it.clientId)))
            }
        }

    }

    fun unRegisterFromPubUpdates(pub: Pub) = ably.channels[pub.name].presence.unsubscribe()
}