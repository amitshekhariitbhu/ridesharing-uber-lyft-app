package me.amitshekhar.ridesharing.ui.maps

import com.google.android.gms.maps.model.LatLng

interface MapsView {

    fun showNearbyCabs(latLngList: List<LatLng>)

    fun informCabBooked()

    fun showPath(latLngList: List<LatLng>)

    fun updateCabLocation(latLng: LatLng)

    fun informCabIsArriving()

    fun informCabArrived()

    fun informTripStart()

    fun informTripEnd()

    fun showRoutesNotAvailableError()

    fun showDirectionApiFailedError(error: String)

}