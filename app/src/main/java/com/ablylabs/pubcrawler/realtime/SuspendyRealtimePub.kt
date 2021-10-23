package com.ablylabs.pubcrawler.realtime

import com.ablylabs.pubcrawler.pubs.Pub
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface SuspendyRealtimePub {
    suspend fun numberOfPeopleInPub(pub: Pub): Int
    suspend fun join(who: PubGoer, which: Pub): JoinResult
    suspend fun leave(who: PubGoer, which: Pub): LeaveResult
    suspend fun sendTextMessage(
        who: PubGoer,
        toWhom: PubGoer,
        messageText: String
    ): MessageSentResult

    suspend fun registerToTextMessage(pub: Pub, receiver: PubGoer): Flow<ReceivedMessage>

    suspend fun offerDrink(who: PubGoer, toWhom: PubGoer): OfferSentResult

    suspend fun registerToDrinkOffers(
        pub: Pub,
        receiver: PubGoer
    ): Flow<PubGoer> //offfers from pubgoer

    suspend fun registerToDrinkOfferResponse(offered: PubGoer, offeree: PubGoer): DrinkOfferResponse

    suspend fun acceptDrink(who: PubGoer, fromWhom: PubGoer): AcceptDrinkResult

    suspend fun rejectDrink(who: PubGoer, fromWhom: PubGoer): RejectDrinkResult

    suspend fun allPubGoers(which: Pub): List<PubGoer>
    suspend fun registerToPresenceUpdates(pub: Pub): Flow<PubPresenceUpdate>
    suspend fun unRegisterFromPubUpdates(pub: Pub)
}

class SuspendyPubImpl(private val realtimePub: RealtimePub) : SuspendyRealtimePub {
    override suspend fun numberOfPeopleInPub(pub: Pub): Int {
        //will this help unblocking?
        return realtimePub.numberOfPeopleInPub(pub)
    }

    override suspend fun join(who: PubGoer, which: Pub): JoinResult {
        return suspendCoroutine { continuation ->
            realtimePub.join(who, which) { success ->
                continuation.resume(if (success) JoinResult.Success else JoinResult.Failed("failed"))
            }
        }
    }

    override suspend fun leave(who: PubGoer, which: Pub): LeaveResult {
        return suspendCoroutine { continuation ->
            realtimePub.leave(who, which) { success ->
                continuation.resume(if (success) LeaveResult.Success else LeaveResult.Failed("failed"))
            }
        }
    }

    override suspend fun sendTextMessage(
        who: PubGoer,
        toWhom: PubGoer,
        messageText: String
    ): MessageSentResult {
        return suspendCoroutine { continuation ->
            realtimePub.sendTextMessage(who, toWhom, messageText) { success ->
                continuation.resume(
                    if (success) MessageSentResult.Success else MessageSentResult.Failed(
                        "Fail"
                    )
                )
            }
        }
    }

    override suspend fun registerToTextMessage(pub: Pub, receiver: PubGoer): Flow<ReceivedMessage> {
        return suspendCoroutine { continuation ->
            realtimePub.registerToTextMessage(pub, receiver) { from, message ->
                val flow = flow { emit(ReceivedMessage(from, message)) }
                continuation.resume(flow)
            }
        }
    }

    override suspend fun offerDrink(who: PubGoer, toWhom: PubGoer): OfferSentResult {
        return suspendCoroutine { continuation ->
            realtimePub.offerDrink(who, toWhom) { success ->
                continuation.resume(
                    if (success) OfferSentResult.Success else OfferSentResult.Failed(
                        "Fail"
                    )
                )
            }
        }
    }

    override suspend fun registerToDrinkOffers(pub: Pub, receiver: PubGoer): Flow<PubGoer> {
        return suspendCoroutine { continuation ->
            realtimePub.registerToDrinkOffers(pub, receiver) { from ->
                val flow = flow { emit(from) }
                continuation.resume(flow)
            }
        }
    }

    override suspend fun registerToDrinkOfferResponse(
        offered: PubGoer,
        offeree: PubGoer
    ): DrinkOfferResponse {
        return suspendCoroutine { continuation ->
            realtimePub.registerToDrinkOfferResponse(offered, offeree) { accept ->
                continuation.resume(if (accept) DrinkOfferResponse.Accept else DrinkOfferResponse.Reject)
            }
        }
    }

    override suspend fun acceptDrink(who: PubGoer, fromWhom: PubGoer): AcceptDrinkResult {
        return suspendCoroutine { continuation ->
            realtimePub.acceptDrink(who, fromWhom) { success ->
                continuation.resume(
                    if (success) AcceptDrinkResult.Success
                    else AcceptDrinkResult.Failed("fail")
                )
            }
        }
    }

    override suspend fun rejectDrink(who: PubGoer, fromWhom: PubGoer): RejectDrinkResult {
        return suspendCoroutine { continuation ->
            realtimePub.rejectDrink(who, fromWhom) { success ->
                continuation.resume(
                    if (success) RejectDrinkResult.Success
                    else RejectDrinkResult.Failed("fail")
                )
            }
        }
    }

    override suspend fun allPubGoers(which: Pub): List<PubGoer> {
        //is this blocking?
        return realtimePub.allPubGoers(which)
    }

    override suspend fun registerToPresenceUpdates(pub: Pub): Flow<PubPresenceUpdate> {
        return suspendCoroutine { continuation ->
            realtimePub.registerToPresenceUpdates(pub) { update ->
                val flow = flow { emit(update) }
                continuation.resume(flow)
            }
        }
    }

    override suspend fun unRegisterFromPubUpdates(pub: Pub) {
        realtimePub.unRegisterFromPubUpdates(pub)
    }

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
    object Success : LeaveResult()
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

sealed class DrinkOfferResponse {
    object Accept : DrinkOfferResponse()
    object Reject : DrinkOfferResponse()
}

// messageReceived: (from: PubGoer, message: String)
data class ReceivedMessage(val from: PubGoer, val message: String)


