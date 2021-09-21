package com.ablylabs.pubcrawler

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ablylabs.pubcrawler.pubservice.Pub
import com.ablylabs.pubcrawler.pubservice.PubsStore
import com.ablylabs.pubcrawler.pubservice.geo.GeolocationTree
import com.ablylabs.pubcrawler.realtime.PubGoer
import com.ablylabs.pubcrawler.realtime.RealtimePub
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.ably.lib.realtime.AblyRealtime
import java.util.*
import kotlin.concurrent.timerTask

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private lateinit var pubsStore: PubsStore ///this should move somewhere else in production app
    private lateinit var progress: ProgressBar
    private lateinit var map: GoogleMap
    private lateinit var realtimePub: RealtimePub
    private val markers =
        mutableListOf<Marker>() //reuse markers in here if not empty, should be the same count

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progress = findViewById(R.id.progress)

        realtimePub = RealtimePub(AblyRealtime("NLYSHA.zPeslg:0aBbLE54Dsylr0qW"))

        val inputStream = resources.openRawResource(R.raw.pubs)
        pubsStore = PubsStore(GeolocationTree(), inputStream)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!
        map.setOnCameraIdleListener(this)

        // Add a marker in Sydney and move the camera
        val bristol = LatLng(51.4684055, -2.7307999)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bristol, 10f))
        progress.apply {
            visibility = View.VISIBLE
            //this should move to a bg thread / coroutine later
            pubsStore.loadData()
            visibility = View.GONE
        }
    }

    private fun drawPubMarkers(pubs: List<Pub>) {
        if (markers.isNotEmpty()) {
            markers.forEachIndexed { index, marker ->
                run {
                    marker.position = LatLng(pubs[index].latitude, pubs[index].longitude)
                    marker.title = pubs[index].name
                }
            }
        } else {
            pubs.forEach {
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.latitude, it.longitude))
                        .title(it.name)
                )
                markers.add(marker)
            }
        }

    }

    override fun onCameraIdle() {
        map.cameraPosition.target.let {
            val result = pubsStore.findNearbyPubs(it.latitude, it.longitude, 2)
            when (result) {
                is PubsStore.NearestPubsResult.PubsFound -> {
                    result.pubs.apply {
                        drawPubMarkers(this)
                        registerForPubUpdates(this)
                        simulateJoin(result.pubs)
                        val numbers = realtimePub.numberOfPeopleIn(this)
                        numbers.forEach {
                            Log.d(
                                TAG,
                                "number of people in ${it.first.name} is ${it.second}"
                            )
                        }
                    }
                }
                is PubsStore.NearestPubsResult.Error -> TODO()
                PubsStore.NearestPubsResult.NoPubs -> TODO()
            }
        }
    }
    private fun simulateJoin(pubs: List<Pub>){
        Handler().postDelayed({
            realtimePub.join(PubGoer("Ikbal"),pubs[0]){
                Toast.makeText(this@MainActivity,if(it)  "Joined the pub" else " Couldn't join the pub",Toast.LENGTH_SHORT).show()
            }
        }, 1000)
    }

    private fun registerForPubUpdates(pubs: List<Pub>) {
        realtimePub.registerToPubUpdates(pubs) { updates ->
            updates.keys.forEach {
                Log.d(TAG, "registerForPubUpdates: ${it.name} ${updates[it]}")
            }
        }
    }

}