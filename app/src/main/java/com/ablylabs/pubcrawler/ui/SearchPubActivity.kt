package com.ablylabs.pubcrawler.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ablylabs.pubcrawler.PubCrawlerApp
import com.ablylabs.pubcrawler.R
import com.ablylabs.pubcrawler.pubs.Pub
import com.ablylabs.pubcrawler.pubs.PubsStore

class SearchPubActivity : AppCompatActivity(), TextWatcher {
    private lateinit var pubInputTextView:EditText
    private lateinit var pubsResultRecyclerView: RecyclerView
    private val resultsAdapter = ResultsAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_pub)

        supportActionBar?.title = "Search for pubs"
        pubInputTextView = findViewById(R.id.pubInputEditText)
        pubsResultRecyclerView = findViewById(R.id.pubsResultRecyclerView)
        pubsResultRecyclerView.adapter = resultsAdapter

        pubInputTextView.addTextChangedListener(this)
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
      //this is likely not needed
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        //this is also likely not needed
    }

    override fun afterTextChanged(editable: Editable?) {
        editable?.let {
            if (it.isEmpty()){
                resultsAdapter.setResult(listOf())
                return
            }
            val store = PubCrawlerApp.instance().pubsStore
            val result = store.searchForPubs(it.toString())
            when(result){
                is PubsStore.PubsResult.PubsFound -> resultsAdapter.setResult(result = result.pubs)
                is PubsStore.PubsResult.NoPubs -> resultsAdapter.setResult(listOf())//set empty view for better ui
            }
        }
    }

   class ResultsAdapter: RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {
       private val pubs = mutableListOf<Pub>()

       fun setResult(result:List<Pub>){
           pubs.clear()
           pubs.addAll(result)
           notifyDataSetChanged()
       }
       override fun onCreateViewHolder(
           parent: ViewGroup,
           viewType: Int
       ): ViewHolder {
           val view = LayoutInflater.from(parent.context)
               .inflate(R.layout.item_pub, parent, false)
           return ViewHolder(view)
       }

       override fun onBindViewHolder(holder: ViewHolder, position: Int) {
           holder.pubTextView.text = pubs[position].name
       }

       override fun getItemCount() = pubs.size
       class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
           val pubTextView: TextView = view.findViewById(R.id.pubNameTextView)
       }
   }
}