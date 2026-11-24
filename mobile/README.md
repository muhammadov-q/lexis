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

- **Backend:** running on port 8000, as described in the [Backend README](../backend/README.md#run-the-api).
- **Android:** Android Studio with an emulator.
- **iOS:** macOS with Xcode and an iOS simulator.

### Backend address

The app reaches the backend through `localBackendBaseUrl()`. Set it for where you run the app:

| Where | File | Address |
|---|---|---|
| Android emulator | `shared/src/androidMain/kotlin/org/example/project/core/network/platform/PlatformHttpClient.android.kt` | `http://127.0.0.1:8000` (default, needs `adb reverse` below) |
| iOS simulator | `shared/src/iosMain/kotlin/org/example/project/core/network/platform/PlatformHttpClient.ios.kt` | `http://127.0.0.1:8000` (default) |
| Phone | the file for its platform | `http://<computer-LAN-IP>:8000` |

For a phone, the phone and the computer must be on the same Wi-Fi, and the backend must be
started with `--host 0.0.0.0`.

### Android

1. Open the `mobile/` folder in Android Studio and start an emulator from **Device Manager**.
2. Forward the emulator's port 8000 to the backend on your computer (again after every
   emulator restart):

   ```bash
   ~/Library/Android/sdk/platform-tools/adb reverse tcp:8000 tcp:8000
   ```

3. Run the `androidApp` configuration.

### iOS

1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Pick a simulator and run the `iosApp` scheme.

The camera does not work in the simulator; use **Choose from photos** there.

### Tests

From the `mobile/` directory, run what CI runs (shared tests on Android and iOS, and the
Android debug build):

```bash
./check.sh
```

`./gradlew :shared:testAndroidHostTest` runs only the shared tests on the JVM, which is quicker.

## Using the App

You can use the app as a guest, or **Sign in** (top right) to save your results. Signed-in
users find their pages under **History** and their words under **Vocabulary**.

1. Choose a model from the list; its names and descriptions come from `GET /models`.
   The backend's default is selected initially. If the list cannot be loaded, use **Reload**
   to retry; recognition remains available and uses the backend default.
2. Tap **Take a photo** or **Choose from photos**. The app uploads the image with the
   selected model to `/predict` for guests or `/recognitions` for signed-in users.
3. While the page is read, the app shows **Reading the page** with the elapsed time; tap
   **Cancel** to stop, then **Read the page** to start again.
4. The page shows a box on every word and the line numbers beside it. The **Word** card shows a close-up of the selected word, its
   translation, dictionary form, definition and confidence, with arrows to step through
   the words. Under it, **Read by** names the model and how long it took, and signed-in
   users can **Open it** in their history. The **Transcript** lists the numbered lines;
   switch to **With translation** to see each line's translation, or each word's when a
   line has none. Tap a word or its box to select it, and tap it again to clear.
5. To use a different model, pick it from the list at the top. The same photo is read
   again.

Results with a `model_version` of `mock` or `eparchos-` followed by one or more characters
are placeholders: they return the same words for every page. An invalid image or model
can cause a validation error; reload the model list or choose another image.

### Vocabulary

Signed-in users can keep a vocabulary of the words they read. It uses the backend's
`/vocabulary` endpoints, so the words are the same as in the web app.

1. Select a word on a saved page, from **History** or right after reading a page. Under
   the **Word** card, write a **Note** if you like and tap **Save word**. Words are saved
   without the punctuation around them, so `ἐστί,` and `ἐστί` are the same entry.
2. Once a word is saved, the same field edits its note (**Save note**), and **In vocabulary**
   opens the word.
3. The **Vocabulary** tab lists your words, newest first, with the page and line they come
   from and their translation.
4. Tap a word to see its translation, dictionary form and definition, and to edit its note.
   The page and line under the meaning open that page with the word selected.
   **Remove word** deletes the word and its note after you confirm.

Guests see a prompt to sign in instead.

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