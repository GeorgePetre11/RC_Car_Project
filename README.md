# Arduino RC Car Project

## Description
The **Arduino RC Car Project** is an Android application designed to control and manage RC cars powered by Arduino microcontrollers. The app connects to the car via Wi-Fi and provides functionalities for navigation, control, and car management. It leverages Firebase for authentication and data storage, SharedPreferences for user settings, and notifications for real-time updates on car connectivity.

---

## Github Repository link: https://github.com/GeorgePetre11/RC_Car_Project

---
## Features
- **Directional Control**: Use buttons to send commands (`/forward`, `/backward`, `/left`, `/right`, `/stop`) to the RC car.
- **Car Discovery**: Scan the local network for RC cars equipped with ESP8266 microcontrollers.
- **Car Management**: Add, view, and manage cars from a Firebase Firestore database.
- **User Authentication**: Login and manage accounts using Firebase Authentication.
- **Dark Mode**: Toggle between light and dark mode for a personalized experience.
- **Settings Menu**: Options for logging out, exiting the app, and toggling dark mode.
- **Real-Time Notifications**: Receive updates about car connectivity status.
- **Persistent Preferences**: Save user preferences (e.g., dark mode) with SharedPreferences.

---

## Component Usage by Activity/Service

1. ### MainActivity

   - Features
      * Displays a list of cars retrieved from Firebase Firestore.
      * Allows adding a test car to Firestore.
      * Provides network scanning functionality to discover RC cars on the local network.
      * Includes a settings popup for toggling dark mode, logging out, and exiting the app.
      * Navigates to other activities such as `CarControlActivity` and `CarDiscoveryActivity`.

   - Components 

      * Shared Preferences
         - Used to store and retrieve the dark mode preference.
         - Ensures a personalized user experience by persisting theme settings across sessions.

      * Database
      * * Firebase Firestore is used to store and retrieve car details (e.g., name, model, IP address).
         - Handles adding new cars and refreshing the list.

      * Intents
           Used for navigation between:
           a) `CarDiscoveryActivity` to discover new cars.
           b) `Authentication` for logging out.
           c) `CarControlActivity` for controlling a selected car.

      * Usage of External APIs
         - Firebase Authentication is used to retrieve the currently logged-in user's ID.
         - Firebase Firestore is used to manage and store car data.
   
---

2. ### Authentication

   - Features
      * Allows users to log in or register using their email and password.
      * Redirects logged-in users to `MainActivity`.
      * Displays error messages for failed login or registration attempts.
      * Validates user input for email and password before attempting login or registration.

   - Components

      * Usage of External APIs
         Firebase Authentication is used to manage user accounts and sessions.
        - Sign-in: `auth.signInWithEmailAndPassword(email, password)` verifies user credentials with Firebase servers.
        - Registration: `auth.createUserWithEmailAndPassword(email, password)` creates a new user account in Firebase.
        - Ensures secure user authentication and session management.

      * Intents
          Used for navigation between activities:
        - Redirects logged-in users to `MainActivity` automatically if they are already authenticated.
        - Navigates to `MainActivity` after successful login or registration.


---

3. ### CarDiscoveryActivity

   - Features
      * Scans the local network to discover RC cars using the `/identify` endpoint.
      * Displays a list of discovered devices in a `ListView`.
      * Allows users to select a discovered device and save it to Firebase Firestore.

   - Components 

      * Database
         - Firebase Firestore is used to store details of discovered RC cars (e.g., name, model, IP, and associated user ID).
         - Handles saving new devices to the database when selected from the list.

      * Background Services
         - The network scanning process is implemented using a background thread.
         - Ensures that the main thread remains responsive during network operations.

      * Usage of External APIs
         - Firebase Authentication is used to retrieve the currently logged-in user's ID.
         - Firebase Firestore is used to store car data for the authenticated user.
         - The ESP8266 API (`/identify` endpoint) is used to check if a discovered IP belongs to an RC car.
        
---

4. ### CarControlActivity

   - Features
      * Provides directional controls for the RC car using buttons (`/forward`, `/backward`, `/left`, `/right`, `/stop`).
      * Sends HTTP commands to the RC car via its IP address.
      * Starts the `HeartbeatService` to monitor car connectivity.
      * Sets up a notification channel to provide updates on car connection status.

   - Components 

      * Intents
         - Used to start the `HeartbeatService` to monitor the car's connectivity.

      * Usage of External APIs
         - The ESP8266 API is used to send HTTP commands to control the RC car's movements.

      * Notifications
         - A notification channel (`car_status_channel`) is created to deliver updates about the car's connectivity.


---

5. ### HeartbeatService

   - Features
      * Monitors the RC car's connection status by sending periodic "heartbeat" requests to the car's `/heartbeat` endpoint.
      * Notifies the user if the car disconnects or reconnects.
      * Runs as a foreground service to ensure persistent monitoring.
      * Broadcasts connection status changes to other components in the app.

   - Components 

      * Foreground Services
         - Runs as a foreground service to ensure it continues monitoring the car's connectivity even when the app is in the background.
         - Displays a persistent notification while active.

      * Notifications
         - Sends notifications when the RC car is disconnected or reconnected.
         - Provides real-time updates to the user about the car's connection status.

      * Usage of External APIs
         - The ESP8266 API is used to send HTTP requests to the car's `/heartbeat` endpoint to check connection status.

      * Broadcast Receivers (potential integration)
         - Sends a broadcast (`CAR_STATUS`) to inform other components, such as activities, about connection status changes.


---

6. ### CarContentProvider

   - Features
      * Provides a standardized interface for querying car data from Firebase Firestore.
      * Supports querying for all cars or a specific car by its name using URIs.
      * Allows deletion of specific cars from Firestore.

   - Components 

      * Content Providers
         - Used to share data (e.g., car details) between the app's components in a structured and consistent way.
         - Supports URI-based queries for flexibility in accessing data.

      * Database
         - Firebase Firestore is used as the backend database to store, retrieve, and delete car data.
         - Facilitates querying car details (`name`, `IP`, `model`, `userId`) for app components.

      * Usage of External APIs
         - Firestore API is used to perform read and delete operations on the car collection in the database.


---

7. ### CarCursor

   - Features
      * Acts as a custom cursor to manage car data in a structured format.
      * Supports column-based data access for car details such as `name`, `model`, `IP`, and `userId`.
      * Enables integration with Android's `ContentProvider` for querying car data.

   - Components 

      * Content Providers
         - Works in conjunction with `CarContentProvider` to handle car data efficiently and provide it to components querying the `ContentProvider`.

      * Database
         - Facilitates access to car data retrieved from Firebase Firestore through the `CarContentProvider`.

      * Usage of External APIs
         - Supports data retrieved from Firebase Firestore by providing a structured way to interact with and query that data.


---

8. ### CarListAdapter

   - Features
      * Displays a list of cars in a `ListView`, allowing users to view, control, and delete cars.
      * Provides buttons for each car to:
         - Navigate to `CarControlActivity` for controlling the car.
         - Delete the car from Firebase Firestore.

   - Components 

      * Intents
         - Used to navigate to `CarControlActivity` when the "Control" button is clicked.

      * Database
         - Firebase Firestore is used to delete car records when the "Delete" button is clicked.
         - Ensures the data is updated both locally (in the adapter) and remotely (in Firestore).

      * Usage of External APIs
         - Firestore API is used to perform deletion of car records.


---



## Technologies Used
- **Android SDK**: Developed in Kotlin.
- **Firebase Authentication**: For secure user login and session management.
- **Firebase Firestore**: To store and manage car details.
- **ESP8266**: Arduino-based microcontroller for Wi-Fi communication and car control.
- **Material Design**: Ensures a modern and user-friendly interface.
- **SharedPreferences**: Saves persistent user settings like dark mode preferences.

---

### Project Setup

### Prerequisites
1. **Install Android Studio**:  
   Download and install the latest version of [Android Studio](https://developer.android.com/studio).

2. **Set up Firebase Project**:
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Create a new Firebase project or use an existing one.
   - Add an Android app to your Firebase project by registering your app with its package name (e.g., `com.example.arduino_rc_car_project`).
   - Download the `google-services.json` file and place it in the `app/` directory of your Android project.
   - Enable **Firestore Database** and **Authentication** in the Firebase console.

3. **Set up ESP8266 RC Car**:
   - Ensure the ESP8266 microcontroller is flashed with firmware that supports the `/identify` and `/heartbeat` endpoints.
   - Connect the ESP8266 to your Wi-Fi network and configure its IP address.

---

### Steps

1. **Clone the Repository**:
   - Clone the project repository from GitHub:
     ```bash
     git clone https://github.com/GeorgePetre11/RC_Car_Project
     ```

2. **Open the Project**:
   - Launch Android Studio.
   - Select **File > Open** and navigate to the cloned project directory.

3. **Sync Gradle**:
   - Ensure all Gradle dependencies are synced. Android Studio will prompt you if any dependencies are missing.

4. **Add Firebase Configuration**:
   - Place the `google-services.json` file in the `app/` directory.
   - Ensure that the Firebase dependencies are included in your `build.gradle` files:
     - **Project-level `build.gradle`**:
       ```gradle
       dependencies {
           classpath 'com.google.gms:google-services:4.3.14'
       }
       ```
     - **App-level `build.gradle`**:
       ```gradle
       dependencies {
           implementation 'com.google.firebase:firebase-auth:22.0.0'
           implementation 'com.google.firebase:firebase-firestore:24.7.0'
       }
       apply plugin: 'com.google.gms.google-services'
       ```

5. **Run the App**:
   - Connect a physical Android device to your computer (network scanning requires a real device).
   - Select your connected device in Android Studio and click **Run** to install and launch the app.

6. **Test the Features**:
   - **Authentication**: Register a new user or log in with an existing account.
   - **Add Cars**: Use the "Add Car" feature to discover and save RC cars on the network.
   - **Control Cars**: Navigate to `CarControlActivity` to control an added car.
   - **Monitor Cars**: Verify that the `HeartbeatService` sends notifications for connection status.

---

### Notes
- Ensure the Android device and ESP8266 RC car are connected to the same Wi-Fi network and also that the Wi-Fi network is 2.4 GHz.
- If using a custom ESP8266 firmware, ensure it responds correctly to the `/identify` and `/heartbeat` endpoints.
- Test network scanning on a physical device, as emulators do not support Wi-Fi scanning.

---


