package me.amitshekhar.ridesharing.simulator

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import com.google.maps.model.LatLng
import com.google.maps.model.TravelMode
import org.json.JSONArray
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask

object Simulator {

    private const val TAG = "Simulator"
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    lateinit var geoApiContext: GeoApiContext
    private lateinit var currentLocation: LatLng
    private lateinit var pickUpLocation: LatLng
    private lateinit var dropLocation: LatLng
    private var nearbyCabLocations = arrayListOf<LatLng>()
    private var pickUpPath = arrayListOf<LatLng>()
    private var tripPath = arrayListOf<LatLng>()
    private val mainThread = Handler(Looper.getMainLooper())

    fun getFakeNearbyCabLocations(
        latitude: Double,
        longitude: Double,
        webSocketListener: WebSocketListener
    ) {
        nearbyCabLocations.clear()
        currentLocation = LatLng(latitude, longitude)
        val size = (4..6).random()

        for (i in 1..size) {
            val randomOperatorForLat = (0..1).random()
            val randomOperatorForLng = (0..1).random()
            var randomDeltaForLat = (10..50).random() / 10000.00
            var randomDeltaForLng = (10..50).random() / 10000.00
            if (randomOperatorForLat == 1) {
                randomDeltaForLat *= -1
            }
            if (randomOperatorForLng == 1) {
                randomDeltaForLng *= -1
            }
            val randomLatitude = (latitude + randomDeltaForLat).coerceAtMost(90.00)
            val randomLongitude = (longitude + randomDeltaForLng).coerceAtMost(180.00)
            nearbyCabLocations.add(LatLng(randomLatitude, randomLongitude))
        }

        val jsonObjectToPush = JSONObject()
        jsonObjectToPush.put("type", "nearByCabs")
        val jsonArray = JSONArray()
        for (location in nearbyCabLocations) {
            val jsonObjectLatLng = JSONObject()
            jsonObjectLatLng.put("lat", location.lat)
            jsonObjectLatLng.put("lng", location.lng)
            jsonArray.put(jsonObjectLatLng)
        }
        jsonObjectToPush.put("locations", jsonArray)
        mainThread.post {
            webSocketListener.onMessage(jsonObjectToPush.toString())
        }
    }

    fun requestCab(
        pickUpLocation: LatLng,
        dropLocation: LatLng,
        webSocketListener: WebSocketListener
    ) {
        this.pickUpLocation = pickUpLocation
        this.dropLocation = dropLocation

        val randomOperatorForLat = (0..1).random()
        val randomOperatorForLng = (0..1).random()

        var randomDeltaForLat = (5..30).random() / 10000.00
        var randomDeltaForLng = (5..30).random() / 10000.00

        if (randomOperatorForLat == 1) {
            randomDeltaForLat *= -1
        }
        if (randomOperatorForLng == 1) {
            randomDeltaForLng *= -1
        }
        val latFakeNearby = (pickUpLocation.lat + randomDeltaForLat).coerceAtMost(90.00)
        val lngFakeNearby = (pickUpLocation.lng + randomDeltaForLng).coerceAtMost(180.00)

        val bookedCabCurrentLocation = LatLng(latFakeNearby, lngFakeNearby)
        val directionsApiRequest = DirectionsApiRequest(geoApiContext)
        directionsApiRequest.mode(TravelMode.DRIVING)
        directionsApiRequest.origin(bookedCabCurrentLocation)
        directionsApiRequest.destination(this.pickUpLocation)
        directionsApiRequest.setCallback(object : PendingResult.Callback<DirectionsResult> {
            override fun onResult(result: DirectionsResult) {
                Log.d(TAG, "onResult : $result")
                val jsonObjectCabBooked = JSONObject()
                jsonObjectCabBooked.put("type", "cabBooked")
                mainThread.post {
                    webSocketListener.onMessage(jsonObjectCabBooked.toString())
                }
                pickUpPath.clear()
                val routeList = result.routes
                // Actually it will have zero or 1 route as we haven't asked Google API for multiple paths

                if (routeList.isEmpty()) {
                    val jsonObjectFailure = JSONObject()
                    jsonObjectFailure.put("type", "routesNotAvailable")
                    mainThread.post {
                        webSocketListener.onError(jsonObjectFailure.toString())
                    }
                } else {
                    for (route in routeList) {
                        val path = route.overviewPolyline.decodePath()
                        pickUpPath.addAll(path)
                    }

                    val jsonObject = JSONObject()
                    jsonObject.put("type", "pickUpPath")
                    val jsonArray = JSONArray()
                    for (pickUp in pickUpPath) {
                        val jsonObjectLatLng = JSONObject()
                        jsonObjectLatLng.put("lat", pickUp.lat)
                        jsonObjectLatLng.put("lng", pickUp.lng)
                        jsonArray.put(jsonObjectLatLng)
                    }
                    jsonObject.put("path", jsonArray)
                    mainThread.post {
                        webSocketListener.onMessage(jsonObject.toString())
                    }

                    startTimerForPickUp(webSocketListener)
                }


            }

            override fun onFailure(e: Throwable) {
                Log.d(TAG, "onFailure : ${e.message}")
                val jsonObjectFailure = JSONObject()
                jsonObjectFailure.put("type", "directionApiFailed")
                jsonObjectFailure.put("error", e.message)
                mainThread.post {
                    webSocketListener.onError(jsonObjectFailure.toString())
                }
            }
        })
    }

    fun startTimerForPickUp(webSocketListener: WebSocketListener) {
        val delay = 2000L
        val period = 3000L
        val size = pickUpPath.size
        var index = 0
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                val jsonObject = JSONObject()
                jsonObject.put("type", "location")
                jsonObject.put("lat", pickUpPath[index].lat)
                jsonObject.put("lng", pickUpPath[index].lng)
                mainThread.post {
                    webSocketListener.onMessage(jsonObject.toString())
                }

                if (index == size - 1) {
                    stopTimer()
                    val jsonObjectCabIsArriving = JSONObject()
                    jsonObjectCabIsArriving.put("type", "cabIsArriving")
                    mainThread.post {
                        webSocketListener.onMessage(jsonObjectCabIsArriving.toString())
                    }
                    startTimerForWaitDuringPickUp(webSocketListener)
                }

                index++
            }
        }

        timer?.schedule(timerTask, delay, period)
    }

    fun startTimerForWaitDuringPickUp(webSocketListener: WebSocketListener) {
        val delay = 3000L
        val period = 3000L
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                stopTimer()
                val jsonObjectCabArrived = JSONObject()
                jsonObjectCabArrived.put("type", "cabArrived")
                mainThread.post {
                    webSocketListener.onMessage(jsonObjectCabArrived.toString())
                }
                val directionsApiRequest = DirectionsApiRequest(geoApiContext)
                directionsApiRequest.mode(TravelMode.DRIVING)
                directionsApiRequest.origin(pickUpLocation)
                directionsApiRequest.destination(dropLocation)
                directionsApiRequest.setCallback(object :
                    PendingResult.Callback<DirectionsResult> {
                    override fun onResult(result: DirectionsResult) {
                        Log.d(TAG, "onResult : $result")
                        tripPath.clear()
                        val routeList = result.routes
                        // Actually it will have zero or 1 route as we haven't asked Google API for multiple paths

                        if (routeList.isEmpty()) {
                            val jsonObjectFailure = JSONObject()
                            jsonObjectFailure.put("type", "routesNotAvailable")
                            mainThread.post {
                                webSocketListener.onError(jsonObjectFailure.toString())
                            }
                        } else {
                            for (route in routeList) {
                                val path = route.overviewPolyline.decodePath()
                                tripPath.addAll(path)
                            }
                            startTimerForTrip(webSocketListener)
                        }

                    }

                    override fun onFailure(e: Throwable) {
                        Log.d(TAG, "onFailure : ${e.message}")
                        val jsonObjectFailure = JSONObject()
                        jsonObjectFailure.put("type", "directionApiFailed")
                        jsonObjectFailure.put("error", e.message)
                        mainThread.post {
                            webSocketListener.onError(jsonObjectFailure.toString())
                        }
                    }
                })

            }
        }
        timer?.schedule(timerTask, delay, period)
    }

    fun startTimerForTrip(webSocketListener: WebSocketListener) {
        val delay = 5000L
        val period = 3000L
        val size = tripPath.size
        var index = 0
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {

                if (index == 0) {
                    val jsonObjectTripStart = JSONObject()
                    jsonObjectTripStart.put("type", "tripStart")
                    mainThread.post {
                        webSocketListener.onMessage(jsonObjectTripStart.toString())
                    }

                    val jsonObject = JSONObject()
                    jsonObject.put("type", "tripPath")
                    val jsonArray = JSONArray()
                    for (trip in tripPath) {
                        val jsonObjectLatLng = JSONObject()
                        jsonObjectLatLng.put("lat", trip.lat)
                        jsonObjectLatLng.put("lng", trip.lng)
                        jsonArray.put(jsonObjectLatLng)
                    }
                    jsonObject.put("path", jsonArray)
                    mainThread.post {
                        webSocketListener.onMessage(jsonObject.toString())
                    }
                }

                val jsonObject = JSONObject()
                jsonObject.put("type", "location")
                jsonObject.put("lat", tripPath[index].lat)
                jsonObject.put("lng", tripPath[index].lng)
                mainThread.post {
                    webSocketListener.onMessage(jsonObject.toString())
                }

                if (index == size - 1) {
                    stopTimer()
                    startTimerForTripEndEvent(webSocketListener)
                }

                index++
            }
        }
        timer?.schedule(timerTask, delay, period)
    }

    fun startTimerForTripEndEvent(webSocketListener: WebSocketListener) {
        val delay = 3000L
        val period = 3000L
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                stopTimer()
                val jsonObjectTripEnd = JSONObject()
                jsonObjectTripEnd.put("type", "tripEnd")
                mainThread.post {
                    webSocketListener.onMessage(jsonObjectTripEnd.toString())
                }
            }
        }
        timer?.schedule(timerTask, delay, period)
    }

    fun stopTimer() {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
    }

}