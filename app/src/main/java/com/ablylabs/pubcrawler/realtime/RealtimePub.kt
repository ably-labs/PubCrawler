package com.ablylabs.pubcrawler.realtime

import android.util.Log
import com.ablylabs.pubcrawler.pubservice.Pub
import io.ably.lib.realtime.AblyRealtime
import io.ably.lib.realtime.CompletionListener
import io.ably.lib.realtime.ConnectionState
import io.ably.lib.realtime.ConnectionStateListener
import io.ably.lib.types.ErrorInfo
import io.ably.lib.types.PresenceMessage

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
    fun numberOfPeopleIn(pubs: List<Pub>): List<Pair<Pub, Int>> {
        return pubs.map { Pair(it, ably.channels[it.name].presence.get().size) }
    }
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

    fun leave(who: PubGoer, which: Pub) {

        TODO()
    }

    fun sendMessage(who: PubGoer, toWhom: PubGoer, message: String) {
        TODO()
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
            if (it.isNotEmpty()){
                return it.toList().map { PubGoer(it.clientId) }
            }
        }
        return listOf()
    }

    fun registerToJoinEvents(pub: Pub, join: (pubGoer: PubGoer) -> Unit) {
        TODO()
    }

    fun registerToLeaveEvents(pub: Pub, join: (pubGoer: PubGoer) -> Unit) {
        TODO()
    }

    fun registerToMessages(pub: Pub, messageReceived: (pubGoer: PubGoer, message: String) -> Unit) {
        TODO()
    }

    fun registerToDrinkOffers(pub: Pub, offerReceived: (pubGoer: PubGoer) -> Unit) {
        TODO()
    }

    //update function might contain a bit more things, actions
    fun registerToPubUpdates(pub: Pub, updated: (update:PubUpdate) -> Unit) {
        ably.channels[pub.name].presence.subscribe {
            when(it.action){
                PresenceMessage.Action.enter -> updated(PubUpdate.Join(PubGoer(it.clientId)))
                PresenceMessage.Action.leave -> updated(PubUpdate.Leave(PubGoer(it.clientId)))
                PresenceMessage.Action.absent -> TODO()
                PresenceMessage.Action.present -> TODO()
                PresenceMessage.Action.update -> TODO()
            }
        }
    }
}