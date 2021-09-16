package com.ablylabs.pubcrawler.pubservice

import com.ablylabs.pubcrawler.pubservice.geo.Geolocation
import com.ablylabs.pubcrawler.pubservice.geo.LocationItem
import com.google.gson.annotations.SerializedName

data class Pub(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("local_authority") val localAuthority: String):LocationItem {
    override fun getGeoLocation() = Geolocation(latitude,longitude)
}
