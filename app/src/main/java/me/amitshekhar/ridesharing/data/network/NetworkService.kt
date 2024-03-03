package me.amitshekhar.ridesharing.data.network

import me.amitshekhar.ridesharing.simulator.WebSocket
import me.amitshekhar.ridesharing.simulator.WebSocketListener

class NetworkService {

    fun createWebSocket(webSocketListener: WebSocketListener): WebSocket {
        return WebSocket(webSocketListener)
    }

}