package com.ablylabs.pubcrawler.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.ablylabs.pubcrawler.PubCrawlerApp
import com.ablylabs.pubcrawler.R
import com.ablylabs.pubcrawler.pubs.Pub
import com.ablylabs.pubcrawler.realtime.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


private const val TAG = "PubActivity"

class PubActivity : AppCompatActivity() {
    private lateinit var peopleRecyclerView: RecyclerView
    private lateinit var peopleAdapter: PeopleRecylerAdapter
    private lateinit var pub: Pub
    private lateinit var pubGoer: PubGoer

    private val viewModel: PubViewModel by viewModels {
        val expensivePub = PubCrawlerApp.instance().realtimePub
        PubViewModelFactory(this, FlowyPubImpl(SuspendyPubImpl(expensivePub)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pub)
        peopleRecyclerView = findViewById(R.id.peopleRecyclerView)
        peopleAdapter = PeopleRecylerAdapter(this::sayHiTo, this::offerDrinkTo)
        setupObservers()
        intent.extras?.let { bundle ->
            bundle.getString(EXTRA_PUB_JSON)?.let {
                pub = Gson().fromJson(it, Pub::class.java)
                supportActionBar?.title = pub.name
                peopleRecyclerView.adapter = peopleAdapter
            }
            bundle.getString(EXTRA_PUBGOER_JSON)?.let {
                pubGoer = Gson().fromJson(it, PubGoer::class.java)
                viewModel.joinPub(pubGoer, pub)
            }
        }
        findViewById<Button>(R.id.leaveButton).setOnClickListener {
            viewModel.leavePub(pubGoer, pub)
        }

    }

    private fun setupObservers() {
        viewModel.leaveResult.observe(this) { finish() }

        viewModel.joinResult.observe(this) {
            when (it) {
                is JoinResult.Success -> {
                    lifecycleScope.launch {
                        listenToTheFlow()
                    }
                }
                is JoinResult.Failed -> {
                    Toast.makeText(this, "Unable to join", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        viewModel.acceptDrinkResult.observe(this) {
            when (it) {
                AcceptDrinkResult.Success ->
                    Toast.makeText(this, "Accept received", Toast.LENGTH_LONG).show()
                is AcceptDrinkResult.Failed ->
                    Toast.makeText(this, "Accept not received", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.rejectDrinkResult.observe(this) {
            when (it) {
                RejectDrinkResult.Success ->
                    Toast.makeText(this, "Accept received", Toast.LENGTH_LONG).show()
                is RejectDrinkResult.Failed ->
                    Toast.makeText(this, "Accept not received", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.messageSentResult.observe(this) {
            when (it) {
                is MessageSentResult.Failed -> Toast.makeText(
                    this, "Couldn't send message to ${it.toWhom.name}", Toast
                        .LENGTH_SHORT
                )
                    .show()
                is MessageSentResult.Success -> Toast.makeText(
                    this, "Message sent to ${it.toWhom.name}", Toast
                        .LENGTH_SHORT
                )
                    .show()
            }
        }

        viewModel.offerDrinkResult.observe(this) {
            when (it) {
                is OfferSentResult.Failed -> Toast.makeText(
                    this, "Couldn't offer drink to ${it.toWhom.name}", Toast
                        .LENGTH_SHORT
                )
                    .show()
                is OfferSentResult.Success -> Toast.makeText(
                    this,
                    "Successfully offered drink to ${it.toWhom.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.offerResponse.observe(this) {
            when (it) {
                is DrinkOfferResponse.Accept -> someoneRespondedToDrinkOffer(it.who, true)
                is DrinkOfferResponse.Reject -> someoneRespondedToDrinkOffer(it.who, false)
            }
        }

        viewModel.allPubGoers.observe(this) {

            supportActionBar?.subtitle = "${it.size} people here"
            peopleAdapter.setPubGoers(it)
            peopleAdapter.notifyDataSetChanged()
        }

        viewModel.presenceActions.observe(this) {
            when (it) {
                is PubPresenceActions.SomeoneJoined -> someoneJustJoined(it.who)
                is PubPresenceActions.SomeoneLeft -> someoneJustLeft(it.who)
            }
        }

    }

    private fun listenToTheFlow() {
        viewModel.pubActions.observe(this) {
            when (it) {
                is PubActions.SomeoneOfferedDrink -> someoneOfferedDrink(it.who)
                is PubActions.SomeoneRespondedToDrinkOffer -> someoneRespondedToDrinkOffer(it.who, it.accepted)
                is PubActions.SomeoneSentMessage -> someoneSentMessage(it.who, it.message)
            }
        }
    }

    private fun someoneSentMessage(who: PubGoer, message: String) {
        Toast.makeText(this, "${who} : ${message}", Toast.LENGTH_LONG)
            .show()
    }

    private fun someoneRespondedToDrinkOffer(who: PubGoer, accepted: Boolean) {
        if (accepted) {
            Toast.makeText(
                this, "${who.name} :  Cheers " +
                        "\uD83C\uDF7B", Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this, "${who.name}: Too drunk, thank you " +
                        "\uD83E\uDD74", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun someoneOfferedDrink(who: PubGoer) {
        showDrinkOfferDialog(this, who) { accept ->
            if (accept) {
                viewModel.acceptDrink(pubGoer, who)
            } else {
                viewModel.rejectDrink(pubGoer, who)
            }
        }
    }

    private fun sayHiTo(to: PubGoer) {
        viewModel.sendTextMessage(pubGoer, to, "Hi \uD83D\uDC4B")
    }

    private fun offerDrinkTo(to: PubGoer) {
        viewModel.offerDrinkTo(pubGoer, to)
    }

    override fun onBackPressed() {
        viewModel.leavePub(pubGoer, pub)
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