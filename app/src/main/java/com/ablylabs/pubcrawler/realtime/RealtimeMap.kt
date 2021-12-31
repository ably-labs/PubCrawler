package com.ablylabs.pubcrawler.realtime

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import io.ably.lib.realtime.AblyRealtime
import io.ably.lib.realtime.CompletionListener
import io.ably.lib.types.ErrorInfo
import io.ably.lib.types.PresenceMessage
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class PubgoerPresenceUpdate{
    data class Join(val pubGoer: PubGoer, val location: LatLng):PubgoerPresenceUpdate()
    data class Update(val pubGoer: PubGoer, val location: LatLng):PubgoerPresenceUpdate()
    data class Leave(val pubGoer: PubGoer):PubgoerPresenceUpdate()
}

private const val TAG = "RealtimeMap"
interface RealtimeMap {
    //all pubgoers on map
    suspend fun allPubGoers(): Map<PubGoer, LatLng>
    fun enter(pubGoer: PubGoer, location: LatLng, joinResult: (success: Boolean) -> Unit)
    fun updateLocation(
        pubGoer: PubGoer,
        location: LatLng,
        updateResult: (success: Boolean) -> Unit
    )

    fun registerToPresenceUpdates(updated: (update: PubgoerPresenceUpdate) -> Unit)
}

const val GLOBAL_CHANNEL_NAME = "global"

class DefaultRealtimeMap(private val ably: AblyRealtime) : RealtimeMap {
    private val gson = Gson() //use for encoding/decoding data

    override suspend fun allPubGoers(): Map<PubGoer, LatLng> {
        return suspendCoroutine { continuation ->
            val presence = ably.channels[GLOBAL_CHANNEL_NAME].presence.get()
            //this transformations should happen somewhere else
            val pubgoers = hashMapOf<PubGoer, LatLng>()
            for (present in presence) {
                val location = Gson().fromJson(present.data as String, LatLng::class.java)
                location?.let {
                    pubgoers[PubGoer(present.clientId)] = it
                }
            }
            continuation.resume(pubgoers)
        }
    }

    override fun enter(pubGoer: PubGoer, location: LatLng, joinResult: (success: Boolean) -> Unit) {
        ably.channels[GLOBAL_CHANNEL_NAME].presence.run {
            enterClient(pubGoer.name, gson.toJson(location), object : CompletionListener {
                override fun onSuccess() {
                    joinResult(true)
                }

                override fun onError(reason: ErrorInfo?) {
                    joinResult(false)
                }
            })

        }
    }

    override fun updateLocation(
        pubGoer: PubGoer,
        location: LatLng,
        updateResult: (success: Boolean) -> Unit
    ) {
        ably.channels[GLOBAL_CHANNEL_NAME].presence.run {
            updateClient(pubGoer.name, gson.toJson(location), object : CompletionListener {
                override fun onSuccess() {
                    updateResult(true)
                }

                override fun onError(reason: ErrorInfo?) {
                    updateResult(false)
                }
            })
        }
    }

    override fun registerToPresenceUpdates( updated: (update: PubgoerPresenceUpdate) -> Unit) {
        val observedActions = EnumSet.of(PresenceMessage.Action.enter, PresenceMessage.Action.leave)
        ably.channels[GLOBAL_CHANNEL_NAME].presence.subscribe(observedActions) {
            Log.d(TAG, "registerToPresenceUpdates: $it")
            when (it.action) {
                PresenceMessage.Action.enter -> {
                    val location:LatLng = Gson().fromJson(it.data as String,LatLng::class.java)
                    updated(PubgoerPresenceUpdate.Join(PubGoer(it.clientId),location))
                }
                PresenceMessage.Action.update -> {
                    val location:LatLng = Gson().fromJson(it.data as String,LatLng::class.java)
                    updated(PubgoerPresenceUpdate.Update(PubGoer(it.clientId),location))
                }
                PresenceMessage.Action.leave -> updated(PubgoerPresenceUpdate.Leave(PubGoer(it.clientId)))
            }
        }

    }

}