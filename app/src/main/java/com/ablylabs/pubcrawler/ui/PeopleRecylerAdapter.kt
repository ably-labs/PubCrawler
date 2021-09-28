package com.ablylabs.pubcrawler.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ablylabs.pubcrawler.R
import com.ablylabs.pubcrawler.realtime.PubGoer

class PeopleRecylerAdapter : RecyclerView.Adapter<PeopleRecylerAdapter.ViewHolder>() {
    private val people = mutableListOf<PubGoer>()
    private lateinit var onTapOnUser : (pubgoer:PubGoer) -> Unit
    fun setPubGoers(list:List<PubGoer>,onTapOnUser : (pubgoer:PubGoer) -> Unit){
        people.clear()
        people.addAll(list)
        this.onTapOnUser = onTapOnUser
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pubgoer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.personTextView.text = people[position].name
        holder.itemView.setOnClickListener { onTapOnUser(people[position]) }
    }

    override fun getItemCount() = people.size
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val personTextView: TextView

        init {
            personTextView = view.findViewById(R.id.personTextView)

        }
    }
}