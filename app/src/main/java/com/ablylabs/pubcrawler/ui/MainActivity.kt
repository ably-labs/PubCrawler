package com.ablylabs.pubcrawler.ui

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ablylabs.pubcrawler.PubCrawlerApp
import com.ablylabs.pubcrawler.R
import com.ablylabs.pubcrawler.pubs.Pub
import com.ablylabs.pubcrawler.pubs.PubsStore
import com.ablylabs.pubcrawler.realtime.PubGoer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {

    private lateinit var bottomSheetBehaviour: BottomSheetBehavior<FrameLayout>
    private lateinit var progress: ProgressBar
    private lateinit var map: GoogleMap
    private lateinit var pubNameTextView: TextView
    private lateinit var pubAddressView: TextView
    private lateinit var numberOfPeopleTextView: TextView
    private lateinit var joinButton: Button
    private val markers =
        mutableListOf<Marker>() //reuse markers in here if not empty, should be the same count
    private lateinit var selectedPub: Pub
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progress = findViewById(R.id.progress)
        pubNameTextView = findViewById(R.id.pubNameTextView)
        pubAddressView = findViewById(R.id.addressTextView)
        numberOfPeopleTextView = findViewById(R.id.numberOfPeopleTextView)
        joinButton = findViewById(R.id.joinButton)
        val realtimePub = PubCrawlerApp.instance().realtimePub
        joinButton.setOnClickListener {
            checkName(this) { name ->
                val who = PubGoer(name)

                Intent(this, PubActivity::class.java).apply {
                    val gson = Gson()
                    putExtra(PubActivity.EXTRA_PUB_JSON, gson.toJson(selectedPub))
                    putExtra(PubActivity.EXTRA_PUBGOER_JSON,gson.toJson(who))
                    startActivity(this)
                }
            }

        }

        bottomSheetBehaviour = BottomSheetBehavior.from(findViewById(R.id.infobox))
        bottomSheetBehaviour.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        //also register for updates
                        realtimePub.registerToPresenceUpdates(selectedPub) {
                            numberOfPeopleTextView.text =
                                "${realtimePub.numberOfPeopleInPub(selectedPub)} people here"
                        }
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        realtimePub.unRegisterFromPubUpdates(selectedPub)
                    }
                }
            }


            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    val  resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.extras?.getString(SearchPubActivity.SELECTED_PUB_JSON)?.let {pubJson->
               selectedPub =  Gson().fromJson(pubJson,Pub::class.java)
                val pubLoc = LatLng(selectedPub.latitude, selectedPub.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(pubLoc, DEFAULT_ZOOM))
                showInfoFor(selectedPub)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.search_item){
            val intent = Intent(this, SearchPubActivity::class.java)
            resultLauncher.launch(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!
        map.setOnCameraIdleListener(this)
        map.setOnMarkerClickListener(this)
        map.setOnMapClickListener { hideInfo() }
        val pubsStore = PubCrawlerApp.instance().pubsStore
        // Add a marker in Sydney and move the camera
        val bristol = LatLng(51.4684055, -2.7307999)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bristol, DEFAULT_ZOOM))
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
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources,R.drawable.pub_marker)))
                )
                marker.tag = it
                markers.add(marker)
            }
        }
    }

    override fun onCameraIdle() {
        map.cameraPosition.target.let {
            val pubsStore = PubCrawlerApp.instance().pubsStore
            val now = System.currentTimeMillis()
            val result = pubsStore.findNearbyPubs(it.latitude, it.longitude, 10)
            val later = System.currentTimeMillis()
            Log.d(TAG, "Nearby computation time ${later-now}ms")
            when (result) {
                is PubsStore.PubsResult.PubsFound -> {
                    result.pubs.apply {
                        drawPubMarkers(this)
                    }
                }
                is PubsStore.PubsResult.Error -> TODO()
                PubsStore.PubsResult.NoPubs -> TODO()
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        selectedPub = marker.tag as Pub

        showInfoFor(selectedPub)
        return true
    }

    private fun showInfoFor(pub: Pub) {
        val realtimePub = PubCrawlerApp.instance().realtimePub
        pubNameTextView.text = pub.name
        pubAddressView.text = pub.address
        numberOfPeopleTextView.text = "${realtimePub.numberOfPeopleInPub(pub)} people here"
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideInfo() {
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    companion object{
        val DEFAULT_ZOOM = 13f
    }

}