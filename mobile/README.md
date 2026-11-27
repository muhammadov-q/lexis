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

Each feature (`auth`, `camera`, `history`, `historydetail`, `source`, `vocabulary`) lives in its own
folder under `shared/src/commonMain/kotlin/org/example/project/feature/`, with `data`,
`domain`, `presentation` and `di` packages where it needs them. Composables shared by several features, the
theme and the buttons are in `common/presentation/ui/`.

## Tech Stack

| Part         | Library                                                                         |
| ------------ | ------------------------------------------------------------------------------- |
| Language     | Kotlin Multiplatform: one codebase for Android and iOS                          |
| UI           | Compose Multiplatform with Material 3, and Compose resources for text and icons |
| Navigation   | Navigation Compose                                                              |
| State        | AndroidX Lifecycle `ViewModel` with Kotlin coroutines and `StateFlow` (MVI)     |
| Dependencies | Koin                                                                            |
| Network      | Ktor client: CIO engine on Android, Darwin on iOS                               |
| JSON         | kotlinx.serialization                                                           |
| Dates        | kotlinx-datetime                                                                |
| Photos       | ImagePickerKMP for the camera and the photo library                             |
| Tests        | kotlin.test, kotlinx-coroutines-test and Ktor `MockEngine`                      |
| Build        | Gradle with the Android Gradle Plugin on JDK 21, and Xcode for iOS              |

The versions are in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Running the App

### Prerequisites

- **Backend:** running on port 8000, as described in the [Backend README](../backend/README.md#run-the-api).
- **Android:** Android Studio with an emulator. Gradle runs on JDK 21; the JDK bundled with
  Android Studio works.
- **iOS:** macOS with Xcode and an iOS simulator.

### 1. Start the backend

Start it as the [Backend README](../backend/README.md#run-the-api) describes, and add
`--host 0.0.0.0` so that a phone on your Wi-Fi can reach it too. For example, with the demo
model, which needs no extra install:

```bash
cd backend
ML_PREDICTOR="eparchos" uv run uvicorn app.main:app --reload --host 0.0.0.0
```

To read your own pages, use `ML_PREDICTOR="kraken"` after installing the `ml` extra, as the
Backend README explains.

### 2. Tell the app where the backend is

Find your case in the table:

| Where the app runs | Backend address         | What you do                                                   |
| ------------------ | ----------------------- | ------------------------------------------------------------- |
| iOS simulator      | `http://127.0.0.1:8000` | Nothing, it is the default.                                   |
| Android emulator   | `http://127.0.0.1:8000` | Run `adb reverse` (step 3), nothing else.                     |
| iPhone             | Your computer's IP      | Add `lexis.backendUrl.ios` to `local.properties` (below).     |
| Android phone      | Your computer's IP      | Add `lexis.backendUrl.android` to `local.properties` (below). |

For a phone:

1. Find your computer's IP on the Wi-Fi. On a Mac:

   ```bash
   ipconfig getifaddr en0
   ```

2. Put it in `mobile/local.properties`. Android Studio creates this file with an `sdk.dir`
   line; if you only use Xcode, create it yourself. Git ignores it, so your IP stays on your
   computer.

   ```properties
   # Both platforms
   lexis.backendUrl=http://192.168.1.20:8000
   # Or one platform only
   lexis.backendUrl.ios=http://192.168.1.20:8000
   lexis.backendUrl.android=http://192.168.1.20:8000
   ```

3. Rebuild the app. The address is read when the app is built, not when it starts.

4. Check that the phone reaches the backend: open `http://<your IP>:8000/models` in the
   phone's browser. It should show a list of models.

If the phone can't reach it: the phone and the computer must be on the same Wi-Fi, and the
backend must run with `--host 0.0.0.0`. Some networks, such as university Wi-Fi, block
devices from reaching each other; use a hotspot or home Wi-Fi then. Your IP changes when you
change network, so update `local.properties` and rebuild.

### 3. Run on Android

1. Open the `mobile/` folder in Android Studio.
2. Pick where to run it:
   - **Emulator:** start one from **Device Manager**, then forward its port 8000 to your
     computer (again after every emulator restart):

     ```bash
     ~/Library/Android/sdk/platform-tools/adb reverse tcp:8000 tcp:8000
     ```

   - **Phone:** turn on **Developer options** and **USB debugging** on the phone, connect it
     with a cable and accept the prompt on the phone.
3. Select the device at the top and run the `androidApp` configuration.

### 4. Run on iOS

1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Pick where to run it:
   - **Simulator:** choose one at the top. The camera does not work there; use
     **Choose from photos**.
   - **iPhone:** connect it with a cable. In Xcode, select the `iosApp` target, open
     **Signing & Capabilities** and choose your Apple ID as **Team**; if the bundle identifier
     is taken, change it to something of your own. On the phone, turn on **Developer Mode**
     (Settings → Privacy & Security), and after the first install trust your Apple ID under
     Settings → General → VPN & Device Management.
3. Run the `iosApp` scheme.

### Tests

From the `mobile/` directory, run what CI runs (shared tests on Android and iOS, and the
Android debug build):

```bash
./check.sh
```

`./gradlew :shared:testAndroidHostTest` runs only the shared tests on the JVM, which is quicker.
To build an APK without Android Studio, run `./gradlew :androidApp:assembleDebug`; it is
written to `androidApp/build/outputs/apk/debug/`.

## Development

- **Screens:** each screen has a `ViewModel` that exposes a `StateFlow` of its `XContract`
  state and takes all input as `XEvent`s through `onEvent`, with no other public functions. One-off actions, such as leaving a screen,
  go through an `XEffect` channel. The state, events and effects are in the screen's
  `XContract.kt`.
- **Composables:** put a new composable in its own file, with a `@Preview`. A screen's stateful
  composable gets its view model from Koin and passes plain state to a stateless content
  composable.
- **Text:** all user-facing text is in `shared/src/commonMain/composeResources/values/strings.xml`.
  Use the same words as the web app.
- **Dependencies:** register new APIs, repositories and view models in the feature's `di`
  module, and add new modules to `di/AppModule.kt`.
- **Network:** APIs take the shared `ApiConfig` and build URLs with `config.url(...)`. They
  decode with `apiJson` and throw `ApiException(status, detail)`, with status 0 when the
  backend could not be reached. All three are in `core/network`.
- **Sign-out:** a view model that shows a user's data calls `resetOnSignOut(sessionStore) { ... }`
  in `init` to clear its state. A repository that caches a user's data clears it itself when
  the session becomes Guest, like `VocabularyRepositoryImpl`.
- **Tests:** shared tests are in `shared/src/commonTest/`, in the same package as the code
  they test. Run `./check.sh` before opening a pull request; CI runs the same script.

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
