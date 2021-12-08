package com.ablylabs.pubcrawler.ui

import android.graphics.Color
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.ablylabs.pubcrawler.R
import com.ablylabs.pubcrawler.realtime.PubGoer

class PeopleRecylerAdapter(
    private val onSayHi: (pubgoer: PubGoer) -> Unit,
    private val onBuyDrink: (pubgoer: PubGoer) -> Unit
) : RecyclerView.Adapter<PeopleRecylerAdapter.ViewHolder>() {
    private val people = mutableListOf<PubGoer>()
    fun setPubGoers(list: List<PubGoer>) {
        people.clear()
        people.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pubgoer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.personTextView.text = people[position].name
        checkName(holder.itemView.context) {
            if (people[position].name != it) {
                holder.sayHiButton.visibility = View.VISIBLE
                holder.offerDrinkButton.visibility = View.VISIBLE
                holder.sayHiButton.setOnClickListener { onSayHi(people[position]) }
                holder.offerDrinkButton.setOnClickListener { onBuyDrink(people[position]) }
            } else {
                holder.sayHiButton.visibility = View.GONE
                holder.offerDrinkButton.visibility = View.GONE
                holder.personTextView.append(" (you)")
            }
        }
    }

    override fun getItemCount() = people.size
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val personTextView: TextView = view.findViewById(R.id.personTextView)
        val sayHiButton: Button = view.findViewById(R.id.sayHiButton)
        val offerDrinkButton: Button = view.findViewById(R.id.offerDrinkButton)
    }
}