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

    fun setPubGoers(list:List<PubGoer>){
        people.clear()
        people.addAll(list)
    }
    fun add(pubGoer: PubGoer) {
        people.add(pubGoer)
    }

    fun remove(pubGoer: PubGoer) {
        people.remove(pubGoer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pubgoer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.personTextView.text = people[position].name
    }

    override fun getItemCount() = people.size
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val personTextView: TextView

        init {
            personTextView = view.findViewById(R.id.personTextView)

        }
    }
}