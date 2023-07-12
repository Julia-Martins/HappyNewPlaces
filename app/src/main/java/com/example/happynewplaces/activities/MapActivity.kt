package com.example.happynewplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happynewplaces.R
import com.example.happynewplaces.databinding.ActivityMapBinding
import com.example.happynewplaces.models.HappyPlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mHappyPlaceDetail: HappyPlaceModel? = null
    private var binding: ActivityMapBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetail = intent.getParcelableExtra<HappyPlaceModel>(
                MainActivity.EXTRA_PLACE_DETAILS
            ) as HappyPlaceModel
        }

        if(mHappyPlaceDetail != null){
            setSupportActionBar(binding?.toolMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mHappyPlaceDetail!!.title

            binding?.toolMap?.setNavigationOnClickListener {
                onBackPressed()
            }

            val supportMapFragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

            supportMapFragment.getMapAsync(this@MapActivity)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(mHappyPlaceDetail!!.latitude, mHappyPlaceDetail!!.longitude)
        googleMap!!.addMarker(MarkerOptions().position(position).title(mHappyPlaceDetail!!.location))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap.animateCamera(newLatLngZoom)
    }
}