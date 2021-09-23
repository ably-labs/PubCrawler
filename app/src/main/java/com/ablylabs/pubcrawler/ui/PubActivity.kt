package com.ablylabs.pubcrawler.ui

import android.os.Bundle
import android.util.Log
import android.view.View
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
import kotlin.math.log

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

    override fun onBackPressed() {
        /* val realtimePub = PubCrawlerApp.instance().realtimePub
         realtimePub.leave(pubGoer,pub){
             if (it){onBackPressed()}else{
                 Toast.makeText(this,"Sorry, can't leave the pub",Toast.LENGTH_SHORT).show()
             }
         }*/
        super.onBackPressed()
    }

    private fun listPeople(pub: Pub) {
        Log.d(TAG, "listPeople: ")
        val realtimePub = PubCrawlerApp.instance().realtimePub
        peopleAdapter.setPubGoers(realtimePub.allPubGoers(pub))
        peopleAdapter.notifyDataSetChanged()
    }

    private fun registerToUpdates(pub: Pub) {
        val contentView = findViewById<View>(android.R.id.content)
        val realtimePub = PubCrawlerApp.instance().realtimePub
        realtimePub.registerToPubUpdates(pub) {
            Log.d(TAG, "registerToUpdates: $it")
            when (it) {
                is PubUpdate.Join -> {
                    peopleAdapter.add(it.pubGoer)
                    Snackbar.make(
                        contentView,
                        "${it.pubGoer.name} joined the pub",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                is PubUpdate.Leave -> {
                    peopleAdapter.remove(it.pubGoer)
                    Snackbar.make(
                        contentView,
                        "${it.pubGoer.name} left the pub",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            listPeople(pub)
        }
    }

    companion object {
        val EXTRA_PUB_JSON = "EXTRA_PUB_JSON"
        val EXTRA_PUBGOER_JSON = "EXTRA_PUBGOER_JSON"
    }
}