package com.ablylabs.pubcrawler.ui

import android.content.Context
import com.ablylabs.pubcrawler.R
import com.ablylabs.pubcrawler.realtime.PubGoer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input

fun checkName(context: Context, named: (name: String) -> Unit) {
    val prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    prefs.getString("name", null)?.let {
        named(it)
    } ?: run {
        MaterialDialog(context).show {
            input(hint = context.getString(R.string.your_name)) { dialog, text ->
                prefs.edit().putString("name", text.toString()).apply()
                named(text.toString())
            }
            title(res = R.string.your_name)
            positiveButton(R.string.submit)
        }
    }
}

fun showDrinkOfferDialog(context: Context, who: PubGoer, response: (accepted: Boolean) -> Unit) {
    MaterialDialog(context).show {
        this.title(R.string.new_drink_offer)
        this.message(text = "${who.name} wants to offer you a drink. Accept it?")
            .positiveButton(R.string.yes_please)
            .negativeButton(R.string.no_thanks)
            .positiveButton {
                response(true)
                dismiss()
            }
            .negativeButton {
                response(false)
            }
    }
}