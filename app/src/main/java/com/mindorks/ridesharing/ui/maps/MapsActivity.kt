package com.mindorks.ridesharing.ui.maps

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mindorks.ridesharing.R
import com.mindorks.ridesharing.data.network.NetworkService
import com.mindorks.ridesharing.utils.MapUtils
import com.mindorks.ridesharing.utils.PermissionUtils

class MapsActivity : AppCompatActivity(), MapsView, OnMapReadyCallback {

    companion object {
        private const val TAG = "MapsActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }

    private lateinit var presenter: MapsPresenter
    private lateinit var googleMap: GoogleMap
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private var currentLatLng: LatLng? = null
    private val nearbyCabMarkerList = arrayListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        presenter = MapsPresenter(NetworkService())
        presenter.onAttach(this)
    }

    private fun moveCamera(latLng: LatLng?) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun animateCamera(latLng: LatLng?) {
        googleMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder().target(
                    latLng
                ).zoom(15.5f).build()
            )
        )
    }

    private fun addCarMarkerAndGet(latLng: LatLng): Marker {
        return googleMap.addMarker(
            MarkerOptions().position(latLng).flat(true)
                .icon(BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(this)))
        )
    }

    private fun enableMyLocationOnMap() {
        googleMap.isMyLocationEnabled = true
    }

    private fun setUpLocationListener() {
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        // for getting the current location update after every 2 seconds
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (currentLatLng == null) {
                    for (location in locationResult.locations) {
                        if (currentLatLng == null) {
                            currentLatLng = LatLng(location.latitude, location.longitude)
                            enableMyLocationOnMap()
                            moveCamera(currentLatLng)
                            animateCamera(currentLatLng)
                            presenter.requestNearbyCabs(currentLatLng!!)
                        }
                    }
                }
                // Few more things we can do here:
                // For example: Update the location of user on server
            }
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    override fun onStart() {
        super.onStart()
        if (currentLatLng == null) {
            when {
                PermissionUtils.isAccessFineLocationGranted(this) -> {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            setUpLocationListener()
                        }
                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                }
                else -> {
                    PermissionUtils.requestAccessFineLocationPermission(
                        this,
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        presenter.onDetach()
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            setUpLocationListener()
                        }
                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_not_granted),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    override fun showNearbyCabs(latLngList: List<LatLng>) {
        nearbyCabMarkerList.clear()
        for (latLng in latLngList) {
            val nearbyCabMarker = addCarMarkerAndGet(latLng)
            nearbyCabMarkerList.add(nearbyCabMarker)
        }
    }

}
