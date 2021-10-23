package com.ablylabs.pubcrawler.realtime

import com.ablylabs.pubcrawler.pubs.Pub
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class PubActions {
    data class SomeoneJoined(val who: PubGoer)
    data class SomeoneLeft(val who: PubGoer)
    data class SomeoneSentMessage(val who: PubGoer, val message: String)
    data class SomeoneOfferedDrink(val who: PubGoer)
    data class SomeoneRespondedToDrinkOffer(val who: PubGoer, val accepted: Boolean)
}

sealed class PubgoerActionResult {
    data class JoinResult(val success: Boolean, val actionsFlow: Flow<PubActions>? = null) :
        PubgoerActionResult()

    data class LeaveResult(val success: Boolean) : PubgoerActionResult()
}

sealed class RealtimeActionResult {
    data class SendTextResult(val success: Boolean) : RealtimeActionResult()
    data class RegisterToTextMessagesResult(val from: PubGoer, val message: String) :
        RealtimeActionResult()

    data class DrinkOfferResult(val success: Boolean) : RealtimeActionResult()
    data class RegisterToDrinkOffersResult(val from: PubGoer) : RealtimeActionResult()
    data class RegisterToDrinkOffersResponseResult(val success: Boolean) : RealtimeActionResult()
    data class AcceptDrinkResult(val success: Boolean) : RealtimeActionResult()
    data class RejectDrinkResult(val success: Boolean) : RealtimeActionResult()
    data class PresenceUpdateResult(val update: PubPresenceUpdate) : RealtimeActionResult()
}


interface FlowyPub {
    suspend fun join(pub: Pub, who: PubGoer): PubgoerActionResult.JoinResult
    fun leave(): PubgoerActionResult.LeaveResult

    suspend fun sendTextMessage(
        who: PubGoer,
        toWhom: PubGoer,
        messageText: String
    ): RealtimeActionResult.SendTextResult

    suspend fun acceptDrink(who: PubGoer, fromWhom: PubGoer): RealtimeActionResult.AcceptDrinkResult
    suspend fun rejectDrink(who: PubGoer, fromWhom: PubGoer): RealtimeActionResult.RejectDrinkResult

    //all pubgoers in a pub
    suspend fun allPubGoers(which: Pub): List<PubGoer>
}

class FlowyPubImpl(private val realtimePub: RealtimePub) : FlowyPub {
    override suspend fun join(pub: Pub, who: PubGoer): PubgoerActionResult.JoinResult {
        suspendCoroutine<PubgoerActionResult.JoinResult> { continuation ->
            realtimePub.join(who, pub) {
                if (!it) continuation.resume(PubgoerActionResult.JoinResult(false))
                else continuation.resume(
                    PubgoerActionResult.JoinResult(
                        true,
                        buildActionFlow(pub, who)
                    )
                )
            }
        }

    }

    private  fun buildActionFlow(pub: Pub, who: PubGoer): Flow<PubActions> {
        flow {
            realtimePub.registerToPresenceUpdates(pub){
                emit(PubActions.SomeoneJoined())
            }
        }
    }

    override fun leave(): PubgoerActionResult.LeaveResult {
        TODO("Not yet implemented")
    }

    override suspend fun sendTextMessage(
        who: PubGoer,
        toWhom: PubGoer,
        messageText: String
    ): RealtimeActionResult.SendTextResult {
        TODO("Not yet implemented")
    }

    override suspend fun acceptDrink(
        who: PubGoer,
        fromWhom: PubGoer
    ): RealtimeActionResult.AcceptDrinkResult {
        TODO("Not yet implemented")
    }

    override suspend fun rejectDrink(
        who: PubGoer,
        fromWhom: PubGoer
    ): RealtimeActionResult.RejectDrinkResult {
        TODO("Not yet implemented")
    }

    override suspend fun allPubGoers(which: Pub): List<PubGoer> {
        TODO("Not yet implemented")
    }

}