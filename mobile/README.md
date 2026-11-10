# Mobile App

The Lexis mobile app is built with **Kotlin Multiplatform** and Compose Multiplatform. It allows users to take a photo or select an existing image, upload it to the backend, and view recognition results.

## Architecture

The mobile app uses **Compose Multiplatform** to share UI and business logic between Android and iOS, with platform-specific implementations where needed.

The code is organized into presentation, domain, and data layers:

- **Presentation:** Compose screens, UI state, and navigation.
- **Domain:** Use cases and repository interfaces.
- **Data:** Repository implementations and API communication.
- **Dependency injection:** Koin wires the app's components and dependencies.
- **Platform-specific code:** Android and iOS provide their own HTTP clients and image-handling implementations.

## Running the App

### Prerequisites

- **Android:** Android Studio, Android SDK, and an emulator or connected device.
- **iOS:** macOS, Xcode, and an iOS simulator or connected device.

The backend must be running before testing image uploads. See the [Backend README](../backend/README.md) for setup instructions.

### Android

1. Open `shared/src/androidMain/kotlin/org/example/project/core/network/platform/PlatformHttpClient.android.kt`.

2. Configure `localBackendBaseUrl()` for your target:

   | Target | Backend URL |
      |---|---|
   | Emulator with ADB reverse | `http://127.0.0.1:8000` |
   | Standard Android emulator | `http://10.0.2.2:8000` |
   | Physical device | `http://<computer-LAN-IP>:8000` |

3. For the recommended emulator setup, forward the backend port:

   ```bash
   adb reverse tcp:8000 tcp:8000
   ```

   Run this command again after restarting the emulator.

   For a physical device, connect the phone and computer to the same network. The backend must listen on `0.0.0.0`, and the firewall must allow connections on port `8000`.

4. From the `mobile/` directory, build the debug APK:

   ```bash
   ./gradlew :androidApp:assembleDebug
   ```

   Alternatively, build and run the Android app using Android Studio or Gradle.

### iOS

1. Open `shared/src/iosMain/kotlin/org/example/project/core/network/platform/PlatformHttpClient.ios.kt`.

2. Configure `localBackendBaseUrl()`:

   - **Simulator:** `http://127.0.0.1:8000`
   - **Physical device:** `http://<computer-LAN-IP>:8000`

3. Open the iOS project in Xcode and run the app on a simulator or connected device.

For a physical device, ensure the backend is reachable over the local network. Camera functionality may be limited in simulators, so a physical device is recommended for testing photo capture.

**Note for physical devices:** The backend must listen on `0.0.0.0` and both devices must be connected to the same Wi-Fi network.

Start the backend using:

```bash
ML_PREDICTOR="eparchos" uv run uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```


## Using the App

1. Choose a model from the list; its names and descriptions come from `GET /models`.
   The backend's default is selected initially. If the list cannot be loaded, use **Reload**
   to retry; recognition remains available and uses the backend default.
2. Tap **Take a photo** or **Choose from photos**. The app uploads the image with the
   selected model to `/predict` for guests or `/recognitions` for signed-in users.
3. View the result. To use a different model, tap **Read with another model**, select a
   model, and tap **Recognize page**. The image preview and photo are retained.

Results with a `model_version` of `mock` or `eparchos-` followed by one or more characters
are placeholders: they return the same words for every page. An invalid image or model
can cause a validation error; reload the model list or choose another image.

## Project Structure

```text
mobile/
├── androidApp/     # Android application
├── iosApp/         # iOS application and Xcode project
└── shared/
    └── src/
        ├── commonMain/   # Shared UI, networking and business logic
        ├── androidMain/  # Android-specific implementations
        ├── iosMain/      # iOS-specific implementations
        └── commonTest/   # Shared unit tests
```

For backend setup and API details, see the [Backend README](../backend/README.md).