package com.ablylabs.pubcrawler.pubs

import com.ablylabs.pubcrawler.pubs.geo.Geolocation
import com.ablylabs.pubcrawler.pubs.geo.LocationItem
import com.ablylabs.pubcrawler.pubs.search.TernaryObject
import com.google.gson.annotations.SerializedName

data class Pub(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("local_authority") val localAuthority: String
) : LocationItem, TernaryObject {
    override val text: String
        get() = name.uppercase()

    override fun getGeoLocation() = Geolocation(latitude, longitude)
}
