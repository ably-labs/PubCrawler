package com.ablylabs.pubcrawler.realtime

import com.ablylabs.pubcrawler.pubs.Pub

interface RealtimePub {
    fun numberOfPeopleInPub(pub: Pub): Int
    fun enter(who: PubGoer, which: Pub, joinResult: (success: Boolean) -> Unit)
    fun leave(who: PubGoer, which: Pub, leaveResult: (success: Boolean) -> Unit)
    fun sendTextMessage(
        who: PubGoer, toWhom: PubGoer, messageText: String,
        messageSentResult: (success: Boolean) -> Unit
    )

    fun registerToTextMessage(
        pub: Pub, receiver: PubGoer,
        messageReceived: (from: PubGoer, message: String) -> Unit
    )

    fun offerDrink(
        who: PubGoer, toWhom: PubGoer,
        offerSentResult: (success: Boolean) -> Unit
    )

    fun registerToDrinkOffers(
        pub: Pub, receiver: PubGoer,
        offerReceived: (from: PubGoer) -> Unit
    )

    /**
     * @param offered PubGoer who was offered the drink
     * @param offeree PubGoer who offered the drink
     * @param acceptResult result lambda which takes a single Boolean param,
     * accept if true and reject if false.
     * Note: When setting channel here make sure that it is set in the correct direction. In this
     * case it should be offered -> offeree
     * \*/
    fun registerToDrinkOfferResponse(
        offered: PubGoer, offeree: PubGoer,
        acceptResult: (success: Boolean) -> Unit
    )

    fun acceptDrink(
        who: PubGoer, fromWhom: PubGoer,
        acceptResult: (success: Boolean) -> Unit
    )

    fun rejectDrink(
        who: PubGoer, fromWhom: PubGoer,
        rejectResult: (success: Boolean) -> Unit
    )

    //all pubgoers in a pub
    fun pubgoersOf(which: Pub): List<PubGoer>
    fun registerToPresenceUpdates(pub: Pub, updated: (update: PubPresenceUpdate) -> Unit)
    fun unRegisterFromPubUpdates(pub: Pub)
}