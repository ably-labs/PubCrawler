package com.ablylabs.pubcrawler.pubs

import com.ablylabs.pubcrawler.pubs.geo.GeoPoint
import com.ablylabs.pubcrawler.pubs.geo.Geolocation
import com.ablylabs.pubcrawler.pubs.geo.GeolocationTree
import com.google.gson.Gson
import com.google.gson.stream.JsonReader


import java.io.InputStream

//create pubs reading inputstream
class PubsStore (private val locationTree: GeolocationTree,
                 private val inputStream: InputStream) {

    fun findNearbyPubs(latitude:Double, longitude:Double, maxPoints:Int) : NearestPubsResult{
        val nearest = locationTree.nearest(GeoPoint(Geolocation(latitude, longitude), null), maxPoints)
        nearest?.let {
            if (it.toList().isEmpty()){
                return NearestPubsResult.NoPubs
            }else{
                //this is not very nice, the whole structure should be generified later
                        return NearestPubsResult.PubsFound(it.toList().map { it.associatedLocation as Pub })
            }
        } ?: kotlin.run{
            return NearestPubsResult.Error("Some sort of error hapened")
        }
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

    sealed class NearestPubsResult{
        data class PubsFound(val pubs:List<Pub>):NearestPubsResult()
        object NoPubs:NearestPubsResult()
        data class Error(val errorMessage:String?):NearestPubsResult()
    }
}
