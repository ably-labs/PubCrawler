package com.ablylabs.pubcrawler.pubservice

import com.ablylabs.pubcrawler.pubservice.geo.GeoPoint
import com.ablylabs.pubcrawler.pubservice.geo.Geolocation
import com.ablylabs.pubcrawler.pubservice.geo.GeolocationTree
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import java.io.InputStream

//create pubs reading inputstream
class PubsStore (private val locationTree: GeolocationTree,
                 private val inputStream: InputStream) {

    fun findNearbyPoints(latitude:Double,longitude:Double,maxPoints:Int) {

    }

    //data initialisation, add all pubs into tree
    fun loadData() {
        //read as stream for memory efficiency
        val jsonReader = JsonReader(inputStream.reader())
        jsonReader.beginArray()
        //read json
        val gson = Gson()
        while (jsonReader.hasNext()) {
            val pub = gson.fromJson<Pub>(jsonReader, Pub::class.java)
            locationTree.insert(GeoPoint(Geolocation(pub.latitude,pub.longitude),pub))
        }
        jsonReader.close()
    }
}