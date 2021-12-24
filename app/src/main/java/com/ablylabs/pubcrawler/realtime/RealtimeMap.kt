package com.ablylabs.pubcrawler.realtime

import io.ably.lib.realtime.AblyRealtime
import io.ably.lib.realtime.CompletionListener
import io.ably.lib.types.ErrorInfo
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface RealtimeMap {
    //all pubgoers on map
    suspend fun allPubGoers(): List<PubGoer>
    fun enter(pubGoer: PubGoer, joinResult: (success: Boolean) -> Unit)
}

const val GLOBAL_CHANNEL_NAME = "global"

class DefaultRealtimeMap(private val ably: AblyRealtime) : RealtimeMap {
    override suspend fun allPubGoers(): List<PubGoer> {
        return suspendCoroutine { continuation ->
            val presence = ably.channels[GLOBAL_CHANNEL_NAME].presence.get()
            //this transformations should happen somewhere else
            val pubgoers = mutableListOf<PubGoer>()
            for (present in presence) {
                pubgoers.add(PubGoer(present.clientId))
            }
            continuation.resume(pubgoers)
        }
    }

    override fun enter(pubGoer: PubGoer, joinResult: (success: Boolean) -> Unit) {
        ably.channels[GLOBAL_CHANNEL_NAME].presence.run {
            enterClient(pubGoer.name, "no_data", object : CompletionListener {
                override fun onSuccess() {
                    joinResult(true)
                }

                override fun onError(reason: ErrorInfo?) {
                    joinResult(false)
                }
            })

        }
    }

}