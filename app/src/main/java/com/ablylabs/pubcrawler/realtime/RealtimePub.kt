package com.ablylabs.pubcrawler.realtime

import android.util.Log
import com.ablylabs.pubcrawler.pubs.Pub
import io.ably.lib.realtime.AblyRealtime
import io.ably.lib.realtime.CompletionListener
import io.ably.lib.types.ErrorInfo
import io.ably.lib.types.Message
import io.ably.lib.types.PresenceMessage
import java.util.*

private const val TAG = "RealtimePub"

class RealtimePub(private val ably: AblyRealtime) {

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
        Log.d(TAG, "sendUnidirectionalMessage: $channelId")
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

    fun sendTextMessage(
        who: PubGoer, toWhom: PubGoer, messageText: String,
        messageSentResult: (success: Boolean) -> Unit
    ) {
        sendUnidirectionalMessage(
            who=who,
            toWhom = toWhom,
            messageName = TEXT_MESSAGE,
            messageText = messageText,
            messageSentResult = messageSentResult
        )
    }

    fun registerToTextMessage(
        pub: Pub, receiver: PubGoer,
        messageReceived: (from: PubGoer, message: String) -> Unit
    ) {
        registerForPubActions(pub = pub,
            receiver = receiver,
            messageName = TEXT_MESSAGE,
            messageReceived = messageReceived)
    }

    fun offerDrink(
        who: PubGoer, toWhom: PubGoer,
        offerSentResult: (success: Boolean) -> Unit
    ) {
        sendUnidirectionalMessage(
            who = who,
            toWhom = toWhom,
            messageName = OFFER_DRINK,
            messageText = "Here, a pint for you!",
            messageSentResult = offerSentResult
        )
    }

    fun registerToDrinkOffers(
        pub: Pub, receiver: PubGoer,
        offerReceived: (from: PubGoer) -> Unit
    ) {
        registerForPubActions(pub, receiver, OFFER_DRINK) { from: PubGoer, message: String ->
            offerReceived(from)
        }
    }

    /**
     * @param offered PubGoer who was offered the drink
     * @param offeree PubGoer who offered the drink
     * @param acceptResult result lambda which takes a single Boolean param,
     * accept if true and reject if false.
     * Note: When setting channel here make sure that it is set in the correct direction. In this
     * case it should be offered -> offeree
     * \*/
    fun registerToDrinkOfferResponse(
        offered: PubGoer, offeree: PubGoer,
        acceptResult: (success: Boolean) -> Unit
    ) {
        val channelId = listOf(offered, offeree).hashCode().toString()
        Log.d(TAG, "registerToDrinkOfferResponse: $channelId")
        ably.channels[channelId].subscribe(ACCEPT_DRINK) {
            acceptResult(true)
        }
        ably.channels[channelId].subscribe(REJECT_DRINK) {
            acceptResult(false)
        }
    }

    private fun registerForPubActions(
        pub: Pub, receiver: PubGoer, messageName: String,
        messageReceived: (from: PubGoer, message: String) -> Unit
    ) {
        allPubGoers(pub)
            .filter { receiver != it }
            .forEach { from ->
                val channelId = listOf(from, receiver).hashCode().toString()
                Log.d(TAG, "${receiver.name} is now subscribed to $messageName from ${from.name} ")
                ably.channels[channelId].subscribe(messageName) {
                    messageReceived(from, it.data as String)
                }
            }
    }

    fun acceptDrink(
        who: PubGoer, fromWhom: PubGoer,
        acceptResult: (success: Boolean) -> Unit
    ) {
        sendUnidirectionalMessage(
            who, fromWhom,
            ACCEPT_DRINK, "Merci", acceptResult
        )

    }

    fun rejectDrink(
        who: PubGoer, fromWhom: PubGoer,
        rejectResult: (success: Boolean) -> Unit
    ) {
        sendUnidirectionalMessage(
            who, fromWhom,
            REJECT_DRINK, "No thanks", rejectResult
        )
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

    companion object {
        val TEXT_MESSAGE = "text_message"
        val OFFER_DRINK = "offer_drink"
        val ACCEPT_DRINK = "accept_drink"
        val REJECT_DRINK = "reject_drink"
    }
}