package com.ablylabs.pubcrawler.realtime

import android.util.Log
import com.ablylabs.pubcrawler.pubs.Pub
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "SuspendyRealtimePub"
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
        return suspendCoroutine { continuation ->
            continuation.resume(realtimePub.numberOfPeopleInPub(pub))
        }
    }

    override suspend fun join(who: PubGoer, which: Pub): JoinResult {
        return suspendCoroutine { continuation ->
            realtimePub.enter(who, which) { success ->
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
                    if (success) MessageSentResult.Success(toWhom) else MessageSentResult.Failed(
                        toWhom,
                        "Fail"
                    )
                )
            }
        }
    }

    override suspend fun registerToTextMessage(pub: Pub, receiver: PubGoer): Flow<ReceivedMessage> {
        return suspendCoroutine { continuation ->
            val flow = callbackFlow {
                realtimePub.registerToTextMessage(pub, receiver) { from, message ->
                    trySend(ReceivedMessage(from, message))
                }
                awaitClose { cancel() }
            }
            continuation.resume(flow)
        }
    }

    override suspend fun offerDrink(who: PubGoer, toWhom: PubGoer): OfferSentResult {
        return suspendCoroutine { continuation ->
            realtimePub.offerDrink(who, toWhom) { success ->
                continuation.resume(
                    if (success) OfferSentResult.Success(toWhom) else OfferSentResult.Failed(toWhom)
                )
            }
        }
    }

    override suspend fun registerToDrinkOfferResponse(
        offered: PubGoer,
        offeree: PubGoer
    ): DrinkOfferResponse {
        return suspendCoroutine { continuation ->
            realtimePub.registerToDrinkOfferResponse(offered, offeree) { accept ->
                continuation.resume(
                    if (accept) DrinkOfferResponse.Accept(offered) else DrinkOfferResponse.Reject(
                        offered
                    )
                )
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
        return suspendCoroutine { continuation ->
            continuation.resume(realtimePub.pubgoersOf(which))
        }
    }

    override suspend fun registerToDrinkOffers(pub: Pub, receiver: PubGoer): Flow<PubGoer> {
        return suspendCoroutine { continuation ->
            val flow = callbackFlow {
                realtimePub.registerToDrinkOffers(pub, receiver) { from ->
                    trySend(from)
                }
                awaitClose { cancel() }
            }
            continuation.resume(flow)
        }
    }

    override suspend fun registerToPresenceUpdates(pub: Pub): Flow<PubPresenceUpdate> {
        return suspendCoroutine { continuation ->
            val flow = callbackFlow {
                realtimePub.registerToPresenceUpdates(pub) { update ->
                    Log.d(TAG, "registerToPresenceUpdates: $update")
                    trySend(update)
                }
                awaitClose { cancel() }
            }
            continuation.resume(flow)
        }
    }

    override suspend fun unRegisterFromPubUpdates(pub: Pub) {
        realtimePub.unRegisterFromPubUpdates(pub)
    }

}

sealed class PubActions {
    data class SomeoneSentMessage(val who: PubGoer, val message: String) : PubActions()
    data class SomeoneOfferedDrink(val who: PubGoer) : PubActions()
    data class SomeoneRespondedToDrinkOffer(val who: PubGoer, val accepted: Boolean) : PubActions()
}

//need to create a different class for presence actions as they might need to be passed to different channels
sealed class PubPresenceActions {
    data class SomeoneJoined(val who: PubGoer) : PubPresenceActions()
    data class SomeoneLeft(val who: PubGoer) : PubPresenceActions()
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
    data class Success(val toWhom: PubGoer) : MessageSentResult()
    data class Failed(val toWhom: PubGoer, val reason: String) : MessageSentResult()
}

sealed class OfferSentResult {
    data class Success(val toWhom: PubGoer) : OfferSentResult()
    data class Failed(val toWhom: PubGoer) : OfferSentResult()
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
    data class Accept(val who: PubGoer) : DrinkOfferResponse()
    data class Reject(val who: PubGoer) : DrinkOfferResponse()
}

// messageReceived: (from: PubGoer, message: String)
data class ReceivedMessage(val from: PubGoer, val message: String)


