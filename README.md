# Arduino RC Car Project

## Description
The Arduino RC Car Project is an Android application designed to control and manage RC cars powered by Arduino microcontrollers. The app connects to the car via Wi-Fi and provides functionalities for navigation, control, and car management. Users can scan for cars on the network, add them to a list, and interact with them using an intuitive user interface. 

### Features
- Control the RC car using directional buttons (forward, backward, left, right, stop).
- Scan and discover RC cars on the local network.
- Add and manage cars from a Firebase Firestore database.
- Receive real-time notifications about car connectivity.
- Toggle between light and dark mode.
- Settings popup with options to log out and exit the app.
- Persistent user settings stored using SharedPreferences.
- Integration of content providers for efficient data querying.

## Components Used

### 1. **Foreground Services**
   - Used in `HeartbeatService` to monitor the connectivity of the car in real-time and notify users if the car is disconnected.

### 2. **Background Services**
   - Threaded network scanning to discover RC cars on the local network.

### 3. **Bound Services**
   - Not directly implemented but the architecture allows potential expansion for real-time data binding between the car and the app.

### 4. **Intents**
   - Navigate between activities (e.g., `MainActivity`, `CarControlActivity`, `CarDiscoveryActivity`, etc.).

### 5. **Activities**
   - **MainActivity**: Displays the list of cars, manages user settings, and navigation.
   - **CarControlActivity**: Allows the user to control the car using commands.
   - **CarDiscoveryActivity**: Scans the network for available RC cars.

### 6. **Broadcast Receivers**
   - Used for reacting to system-wide events (e.g., notifications).

### 7. **Shared Preferences**
   - Saves user settings such as dark mode preferences for persistent user experience.

### 8. **Content Providers**
   - Implemented in `CarContentProvider` to enable querying car data seamlessly within the app.

### 9. **Database**
   - Firebase Firestore is used for storing car details (e.g., name, model, IP address, user ID) and managing car data efficiently.

### 10. **Usage of External APIs**
   - Firebase Authentication and Firestore APIs are used for user management and database operations.

### 11. **Notifications**
   - Real-time notifications about car connectivity status, such as when the car goes out of range or reconnects.

## Technologies Used
- **Android SDK**: Kotlin-based app development.
- **Firebase Firestore**: Database for managing car details.
- **ESP8266**: Arduino-based RC car microcontroller.
- **Wi-Fi Networking**: Connect to and control RC cars.
- **Material Design**: Modern and user-friendly UI.

## Project Setup
1. Clone the repository: `git clone <repository-link>`.
2. Open the project in Android Studio.
3. Sync Gradle dependencies.
4. Configure your Firebase project credentials in the `google-services.json` file.
5. Run the app on an Android device or emulator.

## Electrical Schematics
The RC car uses the following connections:

- **ESP8266 Pins**:
  - D5 (GPIO 14) -> IN1 (L298N Motor Driver)
  - D6 (GPIO 12) -> IN2 (L298N Motor Driver)
  - D7 (GPIO 13) -> IN3 (L298N Motor Driver)
  - D8 (GPIO 15) -> IN4 (L298N Motor Driver)

- **L298N Motor Driver**:
  - IN1, IN2, IN3, IN4: Connect to ESP8266 for motor control.

- **Power Supply**:
  - 5V input to ESP8266.
  - Separate battery for motors connected to L298N.

