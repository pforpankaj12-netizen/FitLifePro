# FitLife Pro (Starter Android Project)

This is a starter native Android project using Kotlin, Jetpack Compose, Material 3, and Navigation Compose.

## Current starter features
- Dashboard with calorie target and progress bar
- Manual food calorie logging
- Water quick-add buttons
- Workout duration logging
- Manual weight entry
- Bottom navigation between Home, Food, Activity, and Progress

## Important limitations
This starter is not yet the complete product specification. Entries currently live in UI memory and can reset when the app process restarts. It does not yet include Room persistence, a verified food database, weekly historical charts, reminders, Health Connect, or release signing.

## Build an APK
1. Install the latest stable Android Studio.
2. Open this folder as an existing project.
3. Allow Gradle sync to finish. Use JDK 17 if Android Studio asks.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Android Studio will show the APK location, usually `app/build/outputs/apk/debug/app-debug.apk`.
6. Copy the APK to your Android phone and install it. You may need to permit installation from that file manager.

## Next development steps
1. Add Room entities/DAOs and repositories for food, water, workouts, and weight.
2. Add date-based history and Monday-to-Sunday aggregation.
3. Add validated nutrition data and macro tracking.
4. Add weekly charts and CSV export.
5. Add notification scheduling and optional Health Connect integration.
6. Add automated tests and prepare a signed release build.
## Build using only your Android phone (GitHub Actions)

This project includes `.github/workflows/build-apk.yml` to build a debug APK in GitHub's cloud runners.

1. In your phone browser, sign in to GitHub and create a new repository named `FitLifePro`.
2. Upload the contents of this extracted project to the repository (upload the files and folders inside `FitLifePro`, not the parent folder).
3. Ensure `.github/workflows/build-apk.yml` is included. If GitHub's web uploader makes hidden folders difficult, create the workflow file in the repository using **Add file > Create new file** and name it `.github/workflows/build-apk.yml`, then paste in the workflow content from this project.
4. Open the repository's **Actions** tab. Select **Build FitLife Pro APK** and tap **Run workflow** if it has not started automatically.
5. Wait for the workflow to finish. Open the successful run and download the `FitLifePro-debug-APK` artifact.
6. Extract the downloaded artifact ZIP on your phone; it contains `app-debug.apk`. Tap the APK to install it, allowing installation from your browser or file manager if Android asks.

GitHub may ask you to verify your account or enable Actions. Cloud runner availability and usage limits depend on your GitHub account. Never upload signing keys or passwords to the repository.

## Note
The workflow builds the current starter app only. It does not add the planned persistent database or full weekly analytics yet.
