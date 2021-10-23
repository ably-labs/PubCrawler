package com.ablylabs.pubcrawler.realtime

import com.ablylabs.pubcrawler.pubs.Pub
import kotlinx.coroutines.flow.Flow

interface SuspendyRealtimePubs {
    suspend fun numberOfPeopleInPub(pub: Pub): Int
    suspend fun join(who: PubGoer, which: Pub): JoinResult
    suspend fun leave(who: PubGoer, which: Pub): LeaveResult
    suspend fun sendTextMessage(
        who: PubGoer,
        toWhom: PubGoer,
        messageText: String
    ): MessageSentResult

    fun registerToTextMessage(pub: Pub, receiver: PubGoer): Flow<ReceivedMessage>

    suspend fun offerDrink(who: PubGoer, toWhom: PubGoer): OfferSentResult

    fun registerToDrinkOffers(pub: Pub, receiver: PubGoer):Flow<PubGoer> //offfers from pubgoer

    suspend fun registerToDrinkOfferResponse(offered: PubGoer, offeree: PubGoer):DrinkOfferResponse

    suspend fun acceptDrink(who: PubGoer, fromWhom: PubGoer):AcceptDrinkResult

    suspend fun rejectDrink(who: PubGoer, fromWhom: PubGoer):RejectDrinkResult

    suspend fun allPubGoers(which: Pub): List<PubGoer>
    fun registerToPresenceUpdates(pub: Pub):Flow<PubPresenceUpdate>
    fun unRegisterFromPubUpdates(pub: Pub)
}

sealed class PubActions {
    data class SomeoneJoined(val who: PubGoer)
    data class SomeoneLeft(val who: PubGoer)
    data class SomeoneSentMessage(val who: PubGoer, val message: String)
    data class SomeoneOfferedDrink(val who: PubGoer)
    data class SomeoneRespondedToDrinkOffer(val who: PubGoer, val accepted: Boolean)
}

sealed class JoinResult {
    object Success : JoinResult()
    data class Failed(val reason: String) : JoinResult()
}

sealed class LeaveResult {
    object Success : JoinResult()
    data class Failed(val reason: String) : LeaveResult()
}

sealed class MessageSentResult {
    object Success : MessageSentResult()
    data class Failed(val reason: String) : MessageSentResult()
}

sealed class OfferSentResult {
    object Success : OfferSentResult()
    data class Failed(val reason: String) : OfferSentResult()
}

sealed class AcceptDrinkResult {
    object Success : AcceptDrinkResult()
    data class Failed(val reason: String) : AcceptDrinkResult()
}

sealed class RejectDrinkResult {
    object Success : RejectDrinkResult()
    data class Failed(val reason: String) : RejectDrinkResult()
}

sealed class OfferReceivedResult {
    object Success : OfferReceivedResult()
    data class Failed(val reason: String) : OfferReceivedResult()
}
sealed class DrinkOfferResponse{
    object Accept:DrinkOfferResponse()
    object Reject:DrinkOfferResponse()
}

// messageReceived: (from: PubGoer, message: String)
data class ReceivedMessage(val from: PubGoer, val message: String)


