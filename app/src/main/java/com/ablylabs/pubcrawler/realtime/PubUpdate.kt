package com.ablylabs.pubcrawler.realtime

sealed class PubUpdate{
    data class Join(val pubGoer: PubGoer):PubUpdate()
    data class Leave(val pubGoer: PubGoer):PubUpdate()
}
