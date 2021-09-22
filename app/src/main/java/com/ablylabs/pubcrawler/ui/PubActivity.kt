package com.ablylabs.pubcrawler.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ablylabs.pubcrawler.PubCrawlerApp
import com.ablylabs.pubcrawler.R
import com.ablylabs.pubcrawler.pubservice.Pub
import com.google.gson.Gson

class PubActivity : AppCompatActivity() {
    private lateinit var peopleRecyclerView: RecyclerView
    private val peopleAdapter = PeopleRecylerAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pub)
        peopleRecyclerView = findViewById(R.id.peopleRecyclerView)
        intent.extras?.let {
            it.getString(EXTRA_PUB_JSON)?.let {
                val pub = Gson().fromJson(it, Pub::class.java)
                pub?.let { pub->
                    supportActionBar?.title = pub.name
                    val realtimePub = PubCrawlerApp.instance().realtimePub
                    supportActionBar?.subtitle = "${realtimePub.numberOfPeopleInPub(pub)} people here"
                    peopleRecyclerView.adapter = peopleAdapter
                    listPeople(pub)
                    registerToUpdates(pub)
                }
            }
        }
    }

    private fun listPeople(pub: Pub) {
        val realtimePub = PubCrawlerApp.instance().realtimePub
        peopleAdapter.setPubGoers(realtimePub.allPubGoers(pub))
    }

    private fun registerToUpdates(pub: Pub) {
        val realtimePub = PubCrawlerApp.instance().realtimePub
        realtimePub.registerToPubUpdates(pub) {
            //for now fetch all, but for the feature, it might be better to locally modify this list
            listPeople(pub)
        }
    }

    companion object {
        val EXTRA_PUB_JSON = "EXTRA_PUB_JSON"
    }
}