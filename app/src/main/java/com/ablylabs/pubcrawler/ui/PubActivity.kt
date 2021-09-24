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
    private val peopleAdapter = PeopleRecylerAdapter()
    private lateinit var pub: Pub
    private lateinit var pubGoer: PubGoer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pub)
        peopleRecyclerView = findViewById(R.id.peopleRecyclerView)
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
        Log.d(TAG, "listPeople: ")
        val realtimePub = PubCrawlerApp.instance().realtimePub
        peopleAdapter.setPubGoers(realtimePub.allPubGoers(pub))
        peopleAdapter.notifyDataSetChanged()
    }

    private fun registerToUpdates(pub: Pub) {
        val realtimePub = PubCrawlerApp.instance().realtimePub
        realtimePub.registerToPresenceUpdates(pub) {
            Log.d(TAG, "registerToUpdates: $it")
            when (it) {
                is PubUpdate.Join -> {
                    someoneJustJoined(it.pubGoer)
                }
                is PubUpdate.Leave -> {
                    someoneJustLeft(it.pubGoer)
                }
            }
            listPeople(pub)
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
            Toast.makeText(this, "Should say hi to joining user", Toast.LENGTH_SHORT).show()
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