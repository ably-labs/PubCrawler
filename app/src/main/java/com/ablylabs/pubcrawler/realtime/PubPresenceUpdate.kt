package com.ablylabs.pubcrawler.realtime

sealed class PubPresenceUpdate{
    data class Join(val pubGoer: PubGoer):PubPresenceUpdate()
    data class Leave(val pubGoer: PubGoer):PubPresenceUpdate()
}
