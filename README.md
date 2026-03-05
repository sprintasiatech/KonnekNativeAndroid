# Konnek Chats — Android Integration Guide

v.1.0.0

---

## Overview

This guide covers adding the Konnek Chats SDK to an existing native Android app using the AAR mechanism. Two libraries are required:

| Library | Package | Repository |
|---|---|---|
| Konnek Flutter Library | `konnek_native_core` | [GitHub](https://github.com/sprintasiatech/konnek_native_core) |
| Konnek Native Bridge Library | `KonnekNativeAndroid` | [GitHub](https://github.com/sprintasiatech/KonnekNativeAndroid) |

---

## Minimum Requirements

- Flutter **3.27.0** or later
- JDK **11**
- Android Studio **Iguana (2023.2.1)** or later
- Konnek Flutter Library (`konnek_native_core`)
- Konnek Native Bridge Library — Android (`KonnekNativeAndroid`)

---

## Step 1 — Installation & Configuration

### 1.1 Clone Repositories

Clone both repositories into the **same parent directory** as your existing app project:

```bash
git clone https://github.com/sprintasiatech/konnek_native_core
git clone https://github.com/sprintasiatech/KonnekNativeAndroid
```

### 1.2 Install Flutter SDK

Install Flutter SDK (minimum v3.27.0): https://docs.flutter.dev/install

### 1.3 Build the Flutter Library

Open `konnek_native_core` in VS Code or any editor, then run:

```bash
flutter pub get
flutter build aar --release
```

### 1.4 Build the Native Bridge Library

Open `KonnekNativeAndroid` in Android Studio, then run:

```bash
./gradlew clean
# Then sync Gradle, followed by:
./gradlew assembleRelease
./gradlew publishReleasePublicationToMavenRepository
```

### 1.5 Configure Your App Project

**Add the dependency to `app/build.gradle`:**

```groovy
// Groovy
implementation 'com.konneknative:konnek-android:1.0.0'
```

```kotlin
// KTS
implementation("com.konneknative:konnek-android:1.0.0")
```

**Add Maven repositories to `settings.gradle` or project-level `build.gradle`:**

```groovy
// Groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add these lines:
        maven { url "https://storage.googleapis.com/download.flutter.io" }
        maven { url "../konnek_native_core/build/host/outputs/repo" }
        maven { url "../KonnekNativeAndroid/app/build" }
    }
}
```

```kotlin
// KTS
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add these lines:
        maven("https://storage.googleapis.com/download.flutter.io")
        maven("../konnek_native_core/build/host/outputs/repo")
        maven("../KonnekNativeAndroid/app/build")
    }
}
```

---

## Step 2 — Initialization

Add the SDK initialization to your **Main Activity**:

```java
// Java
KonnekNative.initializeSDK(
    this,
    "your-client-id",
    "your-client-secret"
);
```

```kotlin
// Kotlin
KonnekNative.initializeSDK(
    this,
    id = "your-client-id",
    secret = "your-client-secret",
)
```

---

## Step 3 — Entry Points

Choose one or both entry point methods:

### Option A — Floating Button

**Java:**
```java
ViewGroup root = findViewById(R.id.main);
View fab = KonnekNative.getFloatingButton(this);
root.addView(fab);

// If using ConstraintLayout:
ConstraintLayout.LayoutParams layoutParams =
    (ConstraintLayout.LayoutParams) fab.getLayoutParams();
layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
```

**Kotlin:**
```kotlin
val root = findViewById<ViewGroup>(R.id.main)
val fab = KonnekNative.getFloatingButton(this)
root.addView(fab)

// If using ConstraintLayout:
val layoutParams = fab.layoutParams as ConstraintLayout.LayoutParams
layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
```

**Kotlin Compose:**
```kotlin
@Composable
fun KonnekFloatingButton(fontResId: Int? = null) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val container = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
            }
            val fab = KonnekNative.getFloatingButton(context, fontResId)
            fab.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                70.toPx(context),
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                marginEnd = 10.toPx(context)
                bottomMargin = 10.toPx(context)
            }
            container.addView(fab)
            container
        },
        update = {},
    )
}
```

#### Optional — Custom Font with Floating Button

Add your font file to `res/font/`, then:

```java
// Java
View fab = KonnekNative.getFloatingButton(this, R.font.<your-font>);
```

```kotlin
// Kotlin
val fab = KonnekNative.getFloatingButton(this, R.font.<your-font>)
```

---

### Option B — Direct Function

```java
// Java
Button entryPoint = findViewById(R.id.entry_point);
entryPoint.setOnClickListener(v -> {
    KonnekNative.openChat(this);
});
```

```kotlin
// Kotlin
val entryPoint = findViewById<Button>(R.id.entry_point)
entryPoint.setOnClickListener {
    KonnekNative.openChat(this)
}
```

---

## Step 4 — Run Your Application

Build and run your app. The Konnek Chats UI should appear via your configured entry point.