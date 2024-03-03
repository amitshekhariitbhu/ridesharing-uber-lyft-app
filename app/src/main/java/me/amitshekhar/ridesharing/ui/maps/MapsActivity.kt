package me.amitshekhar.ridesharing.ui.maps

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import me.amitshekhar.ridesharing.R
import me.amitshekhar.ridesharing.data.network.NetworkService
import me.amitshekhar.ridesharing.databinding.ActivityMapsBinding
import me.amitshekhar.ridesharing.utils.AnimationUtils
import me.amitshekhar.ridesharing.utils.MapUtils
import me.amitshekhar.ridesharing.utils.PermissionUtils
import me.amitshekhar.ridesharing.utils.ViewUtils

class MapsActivity : AppCompatActivity(), MapsView, OnMapReadyCallback {

    companion object {
        private const val TAG = "MapsActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
        private const val PICKUP_REQUEST_CODE = 1
        private const val DROP_REQUEST_CODE = 2
    }

    private lateinit var binding: ActivityMapsBinding
    private lateinit var presenter: MapsPresenter
    private lateinit var googleMap: GoogleMap
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private var currentLatLng: LatLng? = null
    private var pickUpLatLng: LatLng? = null
    private var dropLatLng: LatLng? = null
    private val nearbyCabMarkerList = arrayListOf<Marker>()
    private var destinationMarker: Marker? = null
    private var originMarker: Marker? = null
    private var greyPolyLine: Polyline? = null
    private var blackPolyline: Polyline? = null
    private var previousLatLngFromServer: LatLng? = null
    private var currentLatLngFromServer: LatLng? = null
    private var movingCabMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewUtils.enableTransparentStatusBar(window)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        presenter = MapsPresenter(NetworkService())
        presenter.onAttach(this)
        setUpClickListener()
    }

    private fun setUpClickListener() {
        binding.pickUpTextView.setOnClickListener {
            launchLocationAutoCompleteActivity(PICKUP_REQUEST_CODE)
        }
        binding.dropTextView.setOnClickListener {
            launchLocationAutoCompleteActivity(DROP_REQUEST_CODE)
        }
        binding.requestCabButton.setOnClickListener {
            binding.statusTextView.visibility = View.VISIBLE
            binding.statusTextView.text = getString(R.string.requesting_your_cab)
            binding.requestCabButton.isEnabled = false
            binding.pickUpTextView.isEnabled = false
            binding.dropTextView.isEnabled = false
            presenter.requestCab(pickUpLatLng!!, dropLatLng!!)
        }
        binding.nextRideButton.setOnClickListener {
            reset()
        }
    }

    private fun launchLocationAutoCompleteActivity(requestCode: Int) {
        val fields: List<Place.Field> =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(this)
        startActivityForResult(intent, requestCode)
    }

    private fun moveCamera(latLng: LatLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun animateCamera(latLng: LatLng) {
        val cameraPosition = CameraPosition.Builder().target(latLng).zoom(15.5f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun addCarMarkerAndGet(latLng: LatLng): Marker? {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(this))
        return googleMap.addMarker(
            MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor)
        )
    }

    private fun addOriginDestinationMarkerAndGet(latLng: LatLng): Marker? {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getDestinationBitmap())
        return googleMap.addMarker(
            MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor)
        )
    }

    private fun setCurrentLocationAsPickUp() {
        pickUpLatLng = currentLatLng
        binding.pickUpTextView.text = getString(R.string.current_location)
    }

    private fun enableMyLocationOnMap() {
        googleMap.setPadding(0, ViewUtils.dpToPx(48f), 0, 0)
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
                            setCurrentLocationAsPickUp()
                            enableMyLocationOnMap()
                            moveCamera(currentLatLng!!)
                            animateCamera(currentLatLng!!)
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

    private fun checkAndShowRequestButton() {
        if (pickUpLatLng !== null && dropLatLng !== null) {
            binding.requestCabButton.visibility = View.VISIBLE
            binding.requestCabButton.isEnabled = true
        }
    }

    private fun reset() {
        binding.statusTextView.visibility = View.GONE
        binding.nextRideButton.visibility = View.GONE
        nearbyCabMarkerList.forEach { it.remove() }
        nearbyCabMarkerList.clear()
        previousLatLngFromServer = null
        currentLatLngFromServer = null
        if (currentLatLng != null) {
            moveCamera(currentLatLng!!)
            animateCamera(currentLatLng!!)
            setCurrentLocationAsPickUp()
            presenter.requestNearbyCabs(currentLatLng!!)
        } else {
            binding.pickUpTextView.text = ""
        }
        binding.pickUpTextView.isEnabled = true
        binding.dropTextView.isEnabled = true
        binding.dropTextView.text = ""
        movingCabMarker?.remove()
        greyPolyLine?.remove()
        blackPolyline?.remove()
        originMarker?.remove()
        destinationMarker?.remove()
        dropLatLng = null
        greyPolyLine = null
        blackPolyline = null
        originMarker = null
        destinationMarker = null
        movingCabMarker = null
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICKUP_REQUEST_CODE || requestCode == DROP_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    Log.d(TAG, "Place: " + place.name + ", " + place.id + ", " + place.latLng)
                    when (requestCode) {
                        PICKUP_REQUEST_CODE -> {
                            binding.pickUpTextView.text = place.name
                            pickUpLatLng = place.latLng
                            checkAndShowRequestButton()
                        }

                        DROP_REQUEST_CODE -> {
                            binding.dropTextView.text = place.name
                            dropLatLng = place.latLng
                            checkAndShowRequestButton()
                        }
                    }
                }

                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    Log.d(TAG, status.statusMessage!!)
                }

                RESULT_CANCELED -> {
                    Log.d(TAG, "Place Selection Canceled")
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
            nearbyCabMarkerList.add(nearbyCabMarker!!)
        }
    }

    override fun informCabBooked() {
        nearbyCabMarkerList.forEach { it.remove() }
        nearbyCabMarkerList.clear()
        binding.requestCabButton.visibility = View.GONE
        binding.statusTextView.text = getString(R.string.your_cab_is_booked)
    }

    override fun showPath(latLngList: List<LatLng>) {
        val builder = LatLngBounds.Builder()
        for (latLng in latLngList) {
            builder.include(latLng)
        }
        val bounds = builder.build()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2))
        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.GRAY)
        polylineOptions.width(5f)
        polylineOptions.addAll(latLngList)
        greyPolyLine = googleMap.addPolyline(polylineOptions)

        val blackPolylineOptions = PolylineOptions()
        blackPolylineOptions.width(5f)
        blackPolylineOptions.color(Color.BLACK)
        blackPolyline = googleMap.addPolyline(blackPolylineOptions)

        originMarker = addOriginDestinationMarkerAndGet(latLngList[0])
        originMarker?.setAnchor(0.5f, 0.5f)
        destinationMarker = addOriginDestinationMarkerAndGet(latLngList[latLngList.size - 1])
        destinationMarker?.setAnchor(0.5f, 0.5f)

        val polylineAnimator = AnimationUtils.polyLineAnimator()
        polylineAnimator.addUpdateListener { valueAnimator ->
            val percentValue = (valueAnimator.animatedValue as Int)
            val index = (greyPolyLine?.points!!.size * (percentValue / 100.0f)).toInt()
            blackPolyline?.points = greyPolyLine?.points!!.subList(0, index)
        }
        polylineAnimator.start()
    }

    override fun updateCabLocation(latLng: LatLng) {
        if (movingCabMarker == null) {
            movingCabMarker = addCarMarkerAndGet(latLng)
        }
        if (previousLatLngFromServer == null) {
            currentLatLngFromServer = latLng
            previousLatLngFromServer = currentLatLngFromServer
            movingCabMarker?.position = currentLatLngFromServer!!
            movingCabMarker?.setAnchor(0.5f, 0.5f)
            animateCamera(currentLatLngFromServer!!)
        } else {
            previousLatLngFromServer = currentLatLngFromServer
            currentLatLngFromServer = latLng
            val valueAnimator = AnimationUtils.cabAnimator()
            valueAnimator.addUpdateListener { va ->
                if (currentLatLngFromServer != null && previousLatLngFromServer != null) {
                    val multiplier = va.animatedFraction
                    val nextLocation = LatLng(
                        multiplier * currentLatLngFromServer!!.latitude + (1 - multiplier) * previousLatLngFromServer!!.latitude,
                        multiplier * currentLatLngFromServer!!.longitude + (1 - multiplier) * previousLatLngFromServer!!.longitude
                    )
                    movingCabMarker?.position = nextLocation
                    movingCabMarker?.setAnchor(0.5f, 0.5f)
                    val rotation = MapUtils.getRotation(previousLatLngFromServer!!, nextLocation)
                    if (!rotation.isNaN()) {
                        movingCabMarker?.rotation = rotation
                    }
                    animateCamera(nextLocation)
                }
            }
            valueAnimator.start()
        }
    }

    override fun informCabIsArriving() {
        binding.statusTextView.text = getString(R.string.your_cab_is_arriving)
    }

    override fun informCabArrived() {
        binding.statusTextView.text = getString(R.string.your_cab_has_arrived)
        greyPolyLine?.remove()
        blackPolyline?.remove()
        originMarker?.remove()
        destinationMarker?.remove()
    }

    override fun informTripStart() {
        binding.statusTextView.text = getString(R.string.you_are_on_a_trip)
        previousLatLngFromServer = null
    }

    override fun informTripEnd() {
        binding.statusTextView.text = getString(R.string.trip_end)
        binding.nextRideButton.visibility = View.VISIBLE
        greyPolyLine?.remove()
        blackPolyline?.remove()
        originMarker?.remove()
        destinationMarker?.remove()
    }

    override fun showRoutesNotAvailableError() {
        val error = getString(R.string.route_not_available_choose_different_locations)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        reset()
    }

    override fun showDirectionApiFailedError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        reset()
    }

}
