package com.ablylabs.pubcrawler.realtime

import com.ablylabs.pubcrawler.pubs.Pub

//this should be an inexpensive pub
class DefaultRealtimePub :RealtimePub {
    override fun numberOfPeopleInPub(pub: Pub): Int {
        TODO("Not yet implemented")
    }

    override fun join(who: PubGoer, which: Pub, joinResult: (success: Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun leave(who: PubGoer, which: Pub, leaveResult: (success: Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun sendTextMessage(
        who: PubGoer,
        toWhom: PubGoer,
        messageText: String,
        messageSentResult: (success: Boolean) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun registerToTextMessage(
        pub: Pub,
        receiver: PubGoer,
        messageReceived: (from: PubGoer, message: String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun offerDrink(
        who: PubGoer,
        toWhom: PubGoer,
        offerSentResult: (success: Boolean) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun registerToDrinkOffers(
        pub: Pub,
        receiver: PubGoer,
        offerReceived: (from: PubGoer) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun registerToDrinkOfferResponse(
        offered: PubGoer,
        offeree: PubGoer,
        acceptResult: (success: Boolean) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun acceptDrink(
        who: PubGoer,
        fromWhom: PubGoer,
        acceptResult: (success: Boolean) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun rejectDrink(
        who: PubGoer,
        fromWhom: PubGoer,
        rejectResult: (success: Boolean) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun allPubGoers(which: Pub): List<PubGoer> {
        TODO("Not yet implemented")
    }

    override fun registerToPresenceUpdates(pub: Pub, updated: (update: PubPresenceUpdate) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun unRegisterFromPubUpdates(pub: Pub) {
        TODO("Not yet implemented")
    }
}