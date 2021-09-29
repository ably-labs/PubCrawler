package com.ablylabs.pubcrawler.realtime

import android.util.Log
import com.ablylabs.pubcrawler.pubservice.Pub
import io.ably.lib.realtime.AblyRealtime
import io.ably.lib.realtime.CompletionListener
import io.ably.lib.realtime.ConnectionState
import io.ably.lib.realtime.ConnectionStateListener
import io.ably.lib.types.ErrorInfo
import io.ably.lib.types.Message
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

    private fun sendUnidirectionalMessage(
        who: PubGoer,
        toWhom: PubGoer,
        messageName: String,
        messageText: String,
        messageSentResult: (success: Boolean) -> Unit
    ) {
        val message = Message(messageName, messageText, who.name)
        val channelId = listOf(who, toWhom).hashCode().toString()
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

    fun sendMessage(
        who: PubGoer, toWhom: PubGoer, messageText: String,
        messageSentResult: (success: Boolean) -> Unit
    ) {
        sendUnidirectionalMessage(
            who,
            toWhom,
            "hi_message",
            messageText, messageSentResult
        )
    }

    fun registerToMessages(
        pub: Pub, receiver: PubGoer,
        messageReceived: (from: PubGoer, message: String) -> Unit
    ) {
        registerForChannelMessage(pub, receiver, "hi_message", messageReceived)
    }

    //Most of internals of this function can be shared with 'send message' function.
    fun offerDrink(
        who: PubGoer, toWhom: PubGoer,
        offerSentResult: (success: Boolean) -> Unit
    ) {
        sendUnidirectionalMessage(
            who,
            toWhom,
            "hi_message",
            "offer_drink",
            offerSentResult
        )
    }

    fun registerToDrinkOffers(
        pub: Pub, receiver: PubGoer,
        offerReceived: (from: PubGoer) -> Unit
    ) {
        registerForChannelMessage(pub, receiver, "offer_drink"){from: PubGoer, message: String ->
            offerReceived(from)
        }
    }

    private fun registerForChannelMessage(
        pub: Pub, receiver: PubGoer, channelName: String,
        messageReceived: (from: PubGoer, message: String) -> Unit
    ) {
        allPubGoers(pub).forEach {
            if (it != receiver) {
                val channelId = listOf(it, receiver).hashCode().toString()
                ably.channels[channelId].subscribe(channelName) {
                    if (it.data is String) {
                        val from = PubGoer(it.clientId)
                        messageReceived(from, it.data as String)
                    }
                }
            }
        }
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