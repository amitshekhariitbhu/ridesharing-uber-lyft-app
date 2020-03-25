package com.mindorks.ridesharing.simulator

import com.google.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject

class WebSocket(private var webSocketListener: WebSocketListener) {

    fun connect() {
        webSocketListener.onConnect()
    }

    fun sendMessage(data: String) {
        val jsonObject = JSONObject(data)
        when (jsonObject.getString("type")) {
            "nearByCabs" -> {
                val nearbyCabLocations = Simulator.getFakeNearbyCabLocations(
                    jsonObject.getDouble("lat"),
                    jsonObject.getDouble("lng")
                )
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
                webSocketListener.onMessage(jsonObjectToPush.toString())
            }
            "requestCab" -> {
                val pickUpLatLng =
                    LatLng(jsonObject.getDouble("pickUpLat"), jsonObject.getDouble("pickUpLng"))
                val dropLatLng =
                    LatLng(jsonObject.getDouble("dropLat"), jsonObject.getDouble("dropLng"))

                Simulator.requestCab(
                    pickUpLatLng,
                    dropLatLng,
                    webSocketListener
                )
            }
        }
    }

    fun disconnect() {
        Simulator.stopTimer()
        this.webSocketListener.onDisconnect()
    }

}