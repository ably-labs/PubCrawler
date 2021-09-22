package com.ablylabs.pubcrawler

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.ably.lib.realtime.AblyRealtime
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener,GoogleMap.OnMarkerClickListener {

    private lateinit var bottomSheetBehaviour: BottomSheetBehavior<FrameLayout>
    private lateinit var pubsStore: PubsStore ///this should move somewhere else in production app
    private lateinit var progress: ProgressBar
    private lateinit var map: GoogleMap
    private lateinit var realtimePub: RealtimePub
    private lateinit var pubNameTextView: TextView
    private lateinit var pubAddressView: TextView
    private lateinit var numberOfPeopleTextView: TextView
    private lateinit var joinButton:Button
    private val markers =
        mutableListOf<Marker>() //reuse markers in here if not empty, should be the same count
    private lateinit var selectedPub:Pub
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progress = findViewById(R.id.progress)
        pubNameTextView = findViewById(R.id.pubNameTextView)
        pubAddressView = findViewById(R.id.addressTextView)
        numberOfPeopleTextView = findViewById(R.id.numberOfPeopleTextView)
        joinButton = findViewById(R.id.joinButton)
        joinButton.setOnClickListener {
            realtimePub.join(PubGoer("me"),selectedPub){
                Log.d(TAG, "join pub result $it")
                Toast.makeText(this,if (it) "Joined pub" else "Cannot join pub", Toast.LENGTH_SHORT).show()

                //todo create a new pub screen and show user that screen
            }
        }

        bottomSheetBehaviour = BottomSheetBehavior.from(findViewById(R.id.infobox))

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
        map.setOnMarkerClickListener(this)
        map.setOnMapClickListener { hideInfo() }

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
                    marker.tag = pubs[index]
                }
            }
        } else {
            pubs.forEach {
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.latitude, it.longitude))
                        .title(it.name)
                )
                marker.tag = it
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

    override fun onMarkerClick(marker: Marker): Boolean {
        selectedPub = marker.tag as Pub

        showInfoFor(selectedPub)
        return true
    }
    private fun showInfoFor(pub: Pub){
        pubNameTextView.text = pub.name
        pubAddressView.text = pub.address
        numberOfPeopleTextView.text = "${realtimePub.numberOfPeopleInPub(pub)} people here"
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED

        //also register for updates
        realtimePub.registerToPubUpdates(pub){
            numberOfPeopleTextView.text = "${realtimePub.numberOfPeopleInPub(pub)} people here"
        }
    }
    private fun hideInfo(){
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
    }
    private fun simulateJoin(pubs: List<Pub>){
        Handler(Looper.getMainLooper()).postDelayed({
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