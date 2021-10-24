package com.ablylabs.pubcrawler.realtime

import com.ablylabs.pubcrawler.pubs.Pub
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow


sealed class FlowJoinResult {
    data class Success(val flow: Flow<PubActions>) : FlowJoinResult()
    data class Failure(val reason: String) : FlowJoinResult()
}

interface FlowyRealtimePub {
    suspend fun join(pub: Pub, who: PubGoer): FlowJoinResult
    suspend fun leave(who: PubGoer, which: Pub): LeaveResult


    suspend fun sendTextMessage(
        who: PubGoer,
        toWhom: PubGoer,
        messageText: String
    ): MessageSentResult

    suspend fun acceptDrink(who: PubGoer, fromWhom: PubGoer): AcceptDrinkResult
    suspend fun rejectDrink(who: PubGoer, fromWhom: PubGoer): RejectDrinkResult

    //all pubgoers in a pub
    suspend fun allPubGoers(which: Pub): List<PubGoer>
}

class FlowyPubImpl(private val suspendyPub: SuspendyRealtimePub) : FlowyRealtimePub {
    override suspend fun join(pub: Pub, who: PubGoer): FlowJoinResult {
        val joinResult = suspendyPub.join(who, pub)
        return when (joinResult) {
            JoinResult.Success -> FlowJoinResult.Success(buildActionFlow(pub, who))
            is JoinResult.Failed -> FlowJoinResult.Failure(joinResult.reason)
        }
    }

    private suspend fun buildActionFlow(pub: Pub, who: PubGoer): Flow<PubActions> {
        return flow {
            suspendyPub.registerToPresenceUpdates(pub).collect {
                when (it) {
                    is PubPresenceUpdate.Join -> emit(PubActions.SomeoneJoined(it.pubGoer))
                    is PubPresenceUpdate.Leave -> emit(PubActions.SomeoneLeft(it.pubGoer))
                }
            }
            suspendyPub.registerToDrinkOffers(pub, who).collect { pubgoer ->
                emit(PubActions.SomeoneOfferedDrink(pubgoer))
            }
            suspendyPub.registerToTextMessage(pub,who).collect {
                emit(PubActions.SomeoneSentMessage(it.from,it.message))
            }
        }

    }


    override suspend fun leave(who: PubGoer, which: Pub): LeaveResult {
        return suspendyPub.leave(who,which)
    }

    override suspend fun sendTextMessage(
        who: PubGoer,
        toWhom: PubGoer,
        messageText: String
    ): MessageSentResult {
        return suspendyPub.sendTextMessage(who,toWhom,messageText)
    }

    override suspend fun acceptDrink(who: PubGoer, fromWhom: PubGoer): AcceptDrinkResult {
        return suspendyPub.acceptDrink(who,fromWhom)
    }

    override suspend fun rejectDrink(who: PubGoer, fromWhom: PubGoer): RejectDrinkResult {
        return suspendyPub.rejectDrink(who,fromWhom)
    }

    override suspend fun allPubGoers(which: Pub): List<PubGoer> {
       return suspendyPub.allPubGoers(which)
    }

}