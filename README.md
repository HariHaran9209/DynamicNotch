----
Dynamic Notch Bar App

This lightweight android application transforms the notification area of your phone into an animated and dynamic user interface. This is the first application of its kind that does not require any Modifications to be made to the system files of the android operating system or rooting of the device itself.

The application has been created in response to the frustration I had with similar applications available from the Google Play Store, which were riddled with advertisements and frequently crashed or become inoperative during the day.
----
## Demo

To see the application in action, watch the **[SCREEN RECORDING](https://youtube.com/shorts/pfewdqGORBo?feature=share)**.
----
## Reasons for Creating the Application

I've been using a very popular app to create a Dynamic Notch Bar, only to be disappointed because:

- This app is very unreliable due to frequent crashes
- The overlay will often disappear from my screen during the daytime.
- The application has advertisements that play every 30 seconds

This is what I wanted from the Dynamic Notch Bar App:

• An overlay that will display all new notifications as soon as they arrive (in a thin banner across the top of my screen)

• An overlay that will display the current charge level as soon as I plug my phone into a power source.

• A way to see the time all the time.

• No advertisements, no crashes, no bloatware.

This app will launch itself automatically every time my phone is turned on and will run silently in the background without being visible.
----
# Features 

1. A notification bar that is shown on top of any other application in full screen width.
2. Notifications will be shown as soon as they come in with the application name and message visible.
3. Charging detection with a ⚡ displayed when charging and 🔋 displayed when not charging.
4. The time is always shown in the notification bar.
5. The bar will automatically start when the phone turns on.
6. No ads, no tracking, no permission to connect to the internet.
----
# Installation
----
## System Requirements
1. Android 10 or above(minSdk 29)
2. Enable "Install from unknown sources". Normal for sideloading, this application is not available on the Play Store

# How to enable unknown sources (Android MIUI on Redmi Brand phones)
1. Settings->Privacy->Special App Access->Install Unknown Apps
2. Select your file manager app and toggle Allow from this source to on.

# Install Instructions
1. Download 'app-debug.apk' from Download Latest Release
2. Open APK File on your phone and hit install when prompted on next screen.
3. Now open application, give both the requested permissions and press "Start Notch Bar"
----
# Permissions Required 

| Permission              | Why is it required?                                                   |
|------------------------|----------------------------------------------------------------------|
| SYSTEM_ALERT_WINDOW    | To show the notification bar over all other applications              |
| FOREGROUND_SERVICE     | To run the notification bar in foreground as long as possible          |
| BIND_NOTIFICATION_LISTENER_SERVICE |To read incoming notifications to show them on the notification bar |
| RECEIVE_BOOT_COMPLETED |To automatically open the notification bar upon rebooting of phone     |
*NO PERMISSION REQUIRES INTERNET CONNECTION
----
## 🔧 MIUI-Specific Setup (Redmi / Xiaomi)

MIUI shuts down background apps aggressively. Needs to be done once after installation:

1. **Settings → Apps → Manage Apps → Dynamic Notch → Autostart** → On
2. **Settings → Battery → Battery Saver → Dynamic Notch** → None
----
## 🏗️ Technical Stack

- **Language:** Kotlin
- **Min SDK:** 29 (Android 10)
- **Target SDK:** 34 (Android 14)
- **Primary APIs:** `WindowManager` (overlay), `NotificationListenerService`, `BroadcastReceiver` (charging), `ForegroundService`
- **Uses no third-party libraries** — only Android SDK
----
## 📁 Project Directory Structure

```
app/src/main/
├── java/com/yourname/dynamicnotch/
│   ├── MainActivity.kt              // Setup permission UI
│   ├── OverlayService.kt            // Renders and controls the overlay bar
│   ├── NotificationListener.kt      // Notification listener
│   └── BootReceiver.kt              // Boots the service upon startup
├── res/layout/
│   ├── activity_main.xml            // Setup layout UI
│   └── overlay_bar.xml              // Overlay bar UI
└── AndroidManifest.xml
```
----
## 🔨 Build from Source

```bash
git clone https://github.com/YOURUSERNAME/dynamic-notch.git
cd dynamic-notch
.\gradlew assembleDebug
# APK file: app/build/outputs/apk/debug/app-debug.apk
```

Needs JDK 17 and Android SDK (command line tools, platform 2
----
## 📋 Hackathon Notes

- Signed with the debug key (OK according to competition requirements)
- Works on Redmi 9 Prime (MIUI 12, Android 10)
- minSdk: **29**
----
