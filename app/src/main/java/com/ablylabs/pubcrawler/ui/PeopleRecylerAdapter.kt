package com.ablylabs.pubcrawler.ui

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
        holder.itemView.setOnClickListener { onSayHi(people[position]) }
        holder.itemView.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.itemView)
            popup.inflate(R.menu.user_menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.say_hi_item -> onSayHi(people[position])
                    R.id.buy_drink_item -> onBuyDrink(people[position])
                }
                true
            }
            popup.show()
        }
    }

    override fun getItemCount() = people.size
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val personTextView: TextView

        init {
            personTextView = view.findViewById(R.id.personTextView)

        }
    }
}