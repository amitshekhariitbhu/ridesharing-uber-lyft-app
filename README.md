# Ride-Sharing Uber Lyft Android App
Learn to build a ride-sharing Android Taxi Clone App like Uber, Lyft - Open-Source Project By MindOrks

[![MindOrks](https://img.shields.io/badge/mindorks-opensource-blue.svg)](https://mindorks.com/open-source-projects)
[![MindOrks Community](https://img.shields.io/badge/join-community-blue.svg)](https://mindorks.com/join-community)

<p align="center">
    <img src="https://raw.githubusercontent.com/MindorksOpenSource/ridesharing-uber-lyft-app/master/assets/banner-ridesharing-uber-lyft-app.jpg">
</p>
<br>

## About this Open Source Project
This open-source project is for you(community). Our Team at [MindOrks](https://mindorks.com) has taken this initiative to promote Android Learning in the best possible way. We are determined to provide quality content for everyone. Let's do it together by learning from this project.

## We will build and learn the following for the App like Uber and Lyft:
* Create Rider Android Clone App
* Fetch and show nearby cabs on Google Map
* Set Pickup and drop location
* Book a cab
* Fetch and show driver current location
* Show pickup and trip path on Map with Animation
* Cab Arrival for a pickup like Uber
* On-going trip UI
* Trip End
* Animation like Uber App for Moving Car
* Just to make it simple. This project uses the basic MVP Architecture for building the Uber and Lyft clone
* We have simulated the WebSocket API for you 

## We have simulated the backend environment for you to get the real-work like experience.

## Video showing the App demo with Car Animation, [check here](https://www.youtube.com/watch?v=xn3BOf8uOgc)

## Screenshots from this project

<p align="center">
  <img src="https://raw.githubusercontent.com/MindorksOpenSource/ridesharing-uber-lyft-app/master/assets/nearby-cabs.png" width="200">
  <img src="https://raw.githubusercontent.com/MindorksOpenSource/ridesharing-uber-lyft-app/master/assets/pickup-drop-location.png" width="200">
  <img src="https://raw.githubusercontent.com/MindorksOpenSource/ridesharing-uber-lyft-app/master/assets/pickup-drop-location-both-filled.png" width="200">
  <img src="https://raw.githubusercontent.com/MindorksOpenSource/ridesharing-uber-lyft-app/master/assets/request-cab-button.png" width="200">
</p>
<br>
<p align="center">
  <img src="https://raw.githubusercontent.com/MindorksOpenSource/ridesharing-uber-lyft-app/master/assets/cab-is-booked.png" width="200">
  <img src="https://raw.githubusercontent.com/MindorksOpenSource/ridesharing-uber-lyft-app/master/assets/cab-is-arriving.png" width="200">
  <img src="https://raw.githubusercontent.com/MindorksOpenSource/ridesharing-uber-lyft-app/master/assets/on-trip.png" width="200">
  <img src="https://raw.githubusercontent.com/MindorksOpenSource/ridesharing-uber-lyft-app/master/assets/trip-end.png" width="200">
</p>

## Building the project
* Every feature is done in a different branch so that it will be easy to follow.
* Clone the project, the `master` branch has the latest code.
* To learn and implement from the beginning, switch the branch to `base-project`
* This App uses the Google API Key for Maps, Directions, and Places. Get the API key from the Google Cloud Developer console after enabling the Maps, Directions and Places features for your project. Refer this [link](https://developers.google.com/maps/documentation/directions/get-api-key). And put that key in the local.properties file in your project:
Your local.properties will like below:
```
sdk.dir=PATH_TO_ANDROID_SDK_ON_YOUR_LOCAL_MACHINE    
apiKey=YOUR_API_KEY
```
* Start implementing features:
   * Start with the `base-project` branch
   * Setup project with basic MVP Architecture.
        * Resource to Learn: [Basic Android MVP Introduction](https://mindorks.com/course/android-mvp-introduction)
   * Implement Permission for fetching current location.
        * Resource to Learn: [Using Fused Location API To Fetch Current Location](https://blog.mindorks.com/using-gps-location-manager-in-android-android-tutorial)
   * Implement feature - nearby cabs.
   * Use WebSocket present in `simulator` module to fetch the nearby cabs.
   * Match your solution with `nearby-cabs` branch.
   * Implement feature - pickup and drop location.
   * Match your solution with `pickup-drop-location` branch.
   * Implement feature - book a cab.
   * Implement feature - Show pickup path on the map with Animation.
   * Implement feature - Show the current driver location during pickup.
   * Implement feature - Cab is arriving and arrived.
   * Implement feature - Car Animation like Uber.
   * Implement feature - Show trip path on the map with Animation.
   * Implement feature - Trip Starts.
   * Implement feature - Show the current driver location during the trip.
   * Implement feature - Trip on-going.
   * Implement feature - Trip Ends.
   * Implement feature - Implement Take Next Ride.
   * Match your solution with `book-complete-trip` branch.

## Explore Android Online Tutorials and Courses To Learn More by MindOrks
* [Android Tutorial](https://mindorks.com/android-tutorial) - All Free Android Tutorials by MindOrks
* [Android Online Course for Professionals](https://bootcamp.mindorks.com) - In this online course, you’ll learn the Dagger, Kotlin, RxJava, MVVM Architecture, Architecture Components, Jetpack, LiveData, ViewModel, Room Database, Database Design, Multithreading, Memory Management, Networking, Caching, How Glide works, Unit Testing, and the best practices for Android Development. By the end of this online course, you will have all the skills you need to become a professional Android Developer.
* [Android Online Course for Beginners](https://bootcamp.mindorks.com/android-training-for-beginners) - This course is for beginners for those who want to get started with Android Development.

## WebSocket API Reference for this project
A WebSocket is a persistent connection between a client and server. WebSockets provide a bidirectional, full-duplex communications channel that operates over HTTP through a single TCP/IP socket connection. At its core, the WebSocket protocol facilitates message passing between a client and server. In our case, we have simulated it for you.

* In WebSocket, we have three methods:
   * `connect()`: To connect with the server
   * `sendMessage(data: String)`: To send the data to the server
   * `disconnect()`: To disconnect from the server

* In WebSocketListener, we have four callbacks:
   * `onConnect()`: Called when it is connected with the server
   * `onMessage(data: String)`: Called when an event comes from the server
   * `fun onDisconnect()`: Called when the client is disconnected from the server
   * `fun onError(error: String)`:  Called when the error occurred on the server

* Client sending event to server using `webSocket.sendMessage(data)`:
    * Request for nearby cabs from server
    ```json
    {
      "type": "nearByCabs",
      "lat": 28.438147,
      "lng": 77.0994446
    }
    ``` 
   * Request a cab from server
    ```json
    {
      "type": "requestCab",
      "pickUpLat": 28.4369353,
      "pickUpLng": 77.1125599,
      "dropLat": -25.274398,
      "dropLng": 133.775136
    }
    ```
  
* The Server sending success event to the client received in `onMessage(data: String)`:
   * NearBy cabs 
    ```json
    {
      "type": "nearByCabs",
      "locations": [
        {
          "lat": 28.439147000000002,
          "lng": 77.0944446
        },
        {
          "lat": 28.433147,
          "lng": 77.0952446
        },
        {
          "lat": 28.440547000000002,
          "lng": 77.1026446
        }
      ]
    }
    ```
   * Cab Booked
    ```json
    {
      "type": "cabBooked"
    }
    ```  
   * PickUp Path
    ```json
    {
      "type": "pickUpPath",
      "path": [
        {
          "lat": 28.43578,
          "lng": 77.10198000000001
        },
        {
          "lat": 28.43614,
          "lng": 77.10164
        },
        {
          "lat": 28.436400000000003,
          "lng": 77.10149000000001
        }
      ]
    }
    ```   
   * Cab Current Location during pickup or trip
    ```json
    {
      "type": "location",
      "lat": 28.43578,
      "lng": 77.10198000000001
    }
    ```  
   * Cab is Arriving
    ```json
    {
      "type": "cabIsArriving"
    }
    ```    
   * Cab Arrived
    ```json
    {
      "type": "cabArrived"
    }
    ```    
   * Trip Start
    ```json
    {
      "type": "tripStart"
    }
    ```       
   * Trip Path
    ```json
    {
      "type": "tripPath",
      "path": [
        {
          "lat": 28.438370000000003,
          "lng": 77.09944
        },
        {
          "lat": 28.438450000000003,
          "lng": 77.1006
        },
        {
          "lat": 28.438480000000002,
          "lng": 77.10095000000001
        }
      ]
    }
    ``` 
   * Trip End
    ```json
    {
      "type": "tripEnd"
    }
    ```          
* The server sending the error event to the client received in `onError(error: String)`:
   * Direction API Failed
    ```json
    {
      "type": "directionApiFailed",
      "error": "Unable to resolve host \"maps.googleapis.com\": No address associated with hostname"
    }
    ```
   * Routes Not Available
    ```json
    {
      "type": "routesNotAvailable"
    }
    ```  

### Find this project useful ? :heart:

* Support it by clicking the :star: button on the upper right of this page. :v:

### License
```
   Copyright (C) 2020 MINDORKS NEXTGEN PRIVATE LIMITED

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```