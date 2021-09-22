package com.ablylabs.pubcrawler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ablylabs.pubcrawler.pubservice.Pub
import com.google.gson.Gson

class PubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pub)
        intent.extras?.let {
            it.getString(EXTRA_PUB_JSON)?.let {
                val pub = Gson().fromJson(it,Pub::class.java)
                supportActionBar?.title = pub.name
                supportActionBar?.subtitle = "lots of people here"
            }
        }
    }
    companion object {
        val EXTRA_PUB_JSON = "EXTRA_PUB_JSON"
    }
}