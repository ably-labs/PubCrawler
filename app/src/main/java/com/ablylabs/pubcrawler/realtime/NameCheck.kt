package com.ablylabs.pubcrawler.realtime

import android.content.Context

fun existingUser(context:Context):String?{
    val prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    prefs.getString("name", null)?.let {
        return it
    }
    return null
}