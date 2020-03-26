package com.mindorks.ridesharing.simulator

import com.google.maps.model.LatLng
import org.json.JSONObject

class WebSocket(private var webSocketListener: WebSocketListener) {

    fun connect() {
        webSocketListener.onConnect()
    }

    fun sendMessage(data: String) {
        val jsonObject = JSONObject(data)
        when (jsonObject.getString("type")) {
            "nearByCabs" -> {
                Simulator.getFakeNearbyCabLocations(
                    jsonObject.getDouble("lat"),
                    jsonObject.getDouble("lng"),
                    webSocketListener
                )
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