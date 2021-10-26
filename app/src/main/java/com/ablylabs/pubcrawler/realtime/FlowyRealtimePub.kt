package com.ablylabs.pubcrawler.realtime

import android.util.Log
import com.ablylabs.pubcrawler.pubs.Pub
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "FlowyRealtimePub"

interface FlowyRealtimePub {
    suspend fun join(pub: Pub, who: PubGoer): JoinResult
    suspend fun leave(who: PubGoer, which: Pub): LeaveResult
    suspend fun buildActionFlow(pub: Pub, who: PubGoer): Flow<PubActions>
    suspend fun presenceFlow(pub: Pub): Flow<PubPresenceActions>
    suspend fun sendTextMessage(
            who: PubGoer,
            toWhom: PubGoer,
            messageText: String
    ): MessageSentResult

    suspend fun acceptDrink(who: PubGoer, fromWhom: PubGoer): AcceptDrinkResult
    suspend fun rejectDrink(who: PubGoer, fromWhom: PubGoer): RejectDrinkResult
    suspend fun offerDrink(who: PubGoer, toWhom: PubGoer): OfferSentResult
    suspend fun registerToDrinkOfferResponse(offered: PubGoer, offeree: PubGoer): DrinkOfferResponse

    //all pubgoers in a pub
    suspend fun allPubGoers(which: Pub): List<PubGoer>
}

class FlowyPubImpl(private val suspendyPub: SuspendyRealtimePub) : FlowyRealtimePub {
    override suspend fun join(pub: Pub, who: PubGoer): JoinResult {
        return suspendyPub.join(who, pub)
    }

    @ExperimentalCoroutinesApi
    private fun buildPresenceActionFlow(pub: Pub) = flow {
        Log.d(TAG, "registering to presence updateds")
        suspendyPub.registerToPresenceUpdates(pub).collect {
            when (it) {
                is PubPresenceUpdate.Join -> emit(PubPresenceActions.SomeoneJoined(it.pubGoer))
                is PubPresenceUpdate.Leave -> emit(PubPresenceActions.SomeoneLeft(it.pubGoer))
            }
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun buildActionFlow(pub: Pub, who: PubGoer) =
            channelFlow {
                launch {
                    Log.d(TAG, "registering to drink offers")
                    suspendyPub.registerToDrinkOffers(pub, who).collect { pubgoer ->
                        Log.d(TAG, "Drink offer received from ${pubgoer.name}")
                        send(PubActions.SomeoneOfferedDrink(pubgoer))
                    }
                }
                launch {
                    Log.d(TAG, "registering to text messages")
                    suspendyPub.registerToTextMessage(pub, who).collect {
                        Log.d(TAG, "Text message receiived from ${it.from.name}")
                        send(PubActions.SomeoneSentMessage(it.from, it.message))
                    }
                }
            }

    override suspend fun presenceFlow(pub: Pub): Flow<PubPresenceActions> {
        return buildPresenceActionFlow(pub)
    }

    override suspend fun leave(who: PubGoer, which: Pub): LeaveResult {
        return suspendyPub.leave(who, which)
    }

    override suspend fun offerDrink(who: PubGoer, toWhom: PubGoer): OfferSentResult {
        return suspendyPub.offerDrink(who, toWhom)
    }

    override suspend fun registerToDrinkOfferResponse(offered: PubGoer, offeree: PubGoer): DrinkOfferResponse {
        return suspendyPub.registerToDrinkOfferResponse(offered, offeree)
    }

    override suspend fun sendTextMessage(
            who: PubGoer,
            toWhom: PubGoer,
            messageText: String
    ): MessageSentResult {
        return suspendyPub.sendTextMessage(who, toWhom, messageText)
    }

    override suspend fun acceptDrink(who: PubGoer, fromWhom: PubGoer): AcceptDrinkResult {
        return suspendyPub.acceptDrink(who, fromWhom)
    }

    override suspend fun rejectDrink(who: PubGoer, fromWhom: PubGoer): RejectDrinkResult {
        return suspendyPub.rejectDrink(who, fromWhom)
    }

    override suspend fun allPubGoers(which: Pub): List<PubGoer> {
        return suspendyPub.allPubGoers(which)
    }

}