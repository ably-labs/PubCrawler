package com.ablylabs.pubcrawler.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ablylabs.pubcrawler.PubCrawlerApp
import com.ablylabs.pubcrawler.R
import com.ablylabs.pubcrawler.pubservice.Pub
import com.ablylabs.pubcrawler.realtime.PubGoer
import com.ablylabs.pubcrawler.realtime.PubUpdate
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson


private const val TAG = "PubActivity"

class PubActivity : AppCompatActivity() {
    private lateinit var peopleRecyclerView: RecyclerView
    private lateinit var peopleAdapter: PeopleRecylerAdapter
    private lateinit var pub: Pub
    private lateinit var pubGoer: PubGoer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pub)
        peopleRecyclerView = findViewById(R.id.peopleRecyclerView)
        peopleAdapter = PeopleRecylerAdapter(this::sayHiTo, this::onBuyDrink)
        findViewById<Button>(R.id.leaveButton).setOnClickListener {
            leavePub()
        }
        intent.extras?.let { bundle ->
            bundle.getString(EXTRA_PUB_JSON)?.let {
                pub = Gson().fromJson(it, Pub::class.java)
                supportActionBar?.title = pub.name
                val realtimePub = PubCrawlerApp.instance().realtimePub
                supportActionBar?.subtitle = "${realtimePub.numberOfPeopleInPub(pub)} people here"
                peopleRecyclerView.adapter = peopleAdapter
                listPeople(pub)
                registerToUpdates(pub)
            }
            bundle.getString(EXTRA_PUBGOER_JSON)?.let {
                pubGoer = Gson().fromJson(it, PubGoer::class.java)
            }
        }
    }

    private fun sayHiTo(to: PubGoer) {
        val realtimePub = PubCrawlerApp.instance().realtimePub
        realtimePub.sendMessage(pubGoer, to, "Hi \uD83D\uDC4B") {
            //Something to check, callback from Ably works on background thread?
            runOnUiThread {
                if (it) {
                    Toast.makeText(
                        this,
                        "Successfully sent message to ${to.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Couldn't send message to ${to.name}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun onBuyDrink(to: PubGoer) {
        Toast.makeText(this, "Offer drink to to ${to.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
    }

    private fun leavePub() {
        PubCrawlerApp.instance().realtimePub.leave(pubGoer, pub) {
            if (it) {
                finish()
            } else {
                Toast.makeText(this, "Sorry, cannot leave the pub", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        leavePub()
    }

    private fun listPeople(pub: Pub) {
        val realtimePub = PubCrawlerApp.instance().realtimePub
        val allPresent = realtimePub.allPubGoers(pub)
        peopleAdapter.setPubGoers(allPresent)
        peopleAdapter.notifyDataSetChanged()
    }

    private fun registerToUpdates(pub: Pub) {
        val realtimePub = PubCrawlerApp.instance().realtimePub
        realtimePub.registerToPresenceUpdates(pub) {
            runOnUiThread {
                when (it) {
                    is PubUpdate.Join -> {
                        someoneJustJoined(it.pubGoer)
                    }
                    is PubUpdate.Leave -> {
                        someoneJustLeft(it.pubGoer)
                    }
                }
                listPeople(pub)

                //also register to message updates and offer updates
                realtimePub.registerToMessages(pub, pubGoer) { from, message ->
                    runOnUiThread {
                        Toast.makeText(this, "${from.name} : $message", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    private fun someoneJustJoined(
        pubGoer: PubGoer
    ) {
        val contentView = findViewById<View>(android.R.id.content)
        Snackbar.make(
            contentView,
            "${pubGoer.name} joined the pub",
            Snackbar.LENGTH_LONG
        ).setAction(R.string.say_hi) {
            sayHiTo(pubGoer)
        }.show()
    }

    private fun someoneJustLeft(
        pubGoer: PubGoer
    ) {
        val contentView = findViewById<View>(android.R.id.content)
        Snackbar.make(
            contentView,
            "${pubGoer.name} left the pub",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    companion object {
        val EXTRA_PUB_JSON = "EXTRA_PUB_JSON"
        val EXTRA_PUBGOER_JSON = "EXTRA_PUBGOER_JSON"
    }
}