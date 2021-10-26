package com.ablylabs.pubcrawler.realtime

import com.ablylabs.pubcrawler.pubs.Pub
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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
    private fun buildPresenceActionFlow(pub: Pub): Flow<PubPresenceActions> {
        return channelFlow {
            suspendyPub.registerToPresenceUpdates(pub).collect {
                when (it) {
                    is PubPresenceUpdate.Join -> launch { send(PubPresenceActions.SomeoneJoined(it.pubGoer)) }
                    is PubPresenceUpdate.Leave -> launch { send(PubPresenceActions.SomeoneLeft(it.pubGoer)) }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun buildActionFlow(pub: Pub, who: PubGoer): Flow<PubActions> {
        return channelFlow {
            suspendyPub.registerToDrinkOffers(pub, who).collect { pubgoer ->
                launch {
                    send(PubActions.SomeoneOfferedDrink(pubgoer))
                }
            }
            suspendyPub.registerToTextMessage(pub, who).collect {
                launch {
                    send(PubActions.SomeoneSentMessage(it.from, it.message))
                }
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