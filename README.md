# Lorraine

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.dottttt.lorraine/lorraine/badge.svg)](https://maven-central.sonatype.com/search?q=g:io.github.dottttt.lorraine%20a:lorraine)

**Lorraine** is a lightweight, persistent work management framework for Kotlin Multiplatform. Inspired by Android's WorkManager and NSOperation, it provides a unified API for managing background tasks across Android and iOS, ensuring they run even if the app is restarted.

## ✨ Features

- 📱 **Kotlin Multiplatform**: Shared logic for Android and iOS.
- 💾 **Persistence**: Tasks are stored in a local SQLite database (via Room) and resumed after app restarts.
- 🔗 **Work Chaining**: Easily chain multiple tasks together with `then` operations.
- ⚙️ **Constraints**: Define requirements like `requiredNetwork`, `requireBatteryNotLow` and `requireCharging` for your tasks.
- 🛠️ **DSL-based API**: Clean and intuitive DSL for initialization and task definition.
- 📊 **Monitoring**: Observe task status using Kotlin Flows.

## 🚀 Setup

Add the dependency to your project using Version Catalogs:

```toml
[versions]
lorraine = "0.3.0" # Use the latest version

[libraries]
lorraine = { module = "io.github.dottttt.lorraine:lorraine", version.ref = "lorraine" }
```

In your module's `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.lorraine)
        }
    }
}
```

## 🛠️ Usage

### 1. Define a Worker

Create your background logic by extending `WorkLorraine`. Use `LorraineResult` to communicate the outcome.

```kotlin
class SyncDataWorker : WorkLorraine() {
    override suspend fun doWork(inputData: LorraineData?): LorraineResult {
        return try {
            // Your heavy work here
            // val userId = inputData?.getString("userId")
            LorraineResult.success()
        } catch (e: Exception) {
            LorraineResult.retry() // Or LorraineResult.failure()
        }
    }
}
```

### 2. Initialize Lorraine

Register your workers during app startup.

#### Shared Code
```kotlin
const val SYNC_USER = "SYNC_USER"

fun initLorraine(context: LorraineContext): Lorraine {
    return startLorraine(context) {
        work(SYNC_USER) { SyncDataWorker() }
        
        logger {
            enable = true // Enable internal logging for debugging
        }
    }
}
```

#### Android
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initLorraine(createLorraineContext(this))
    }
}
```

#### iOS
```kotlin
// In your iOS Application delegate or SwiftUI App
initLorraine(createLorraineContext())
```

### 3. Enqueue Tasks

#### Single Task
```kotlin
lorraine.enqueue(
    queueId = "single_sync",
    type = ExistingLorrainePolicy.REPLACE,
    request = lorraineRequest {
        identifier = SYNC_USER
        constraints { 
            requiredNetwork = true 
        }
    }
)
```

#### Work Chaining (Operations)
Combine multiple requests into a single operation.

```kotlin
val operation = lorraineOperation {
    existingPolicy = ExistingLorrainePolicy.APPEND
    
    startWith {
        identifier = "REFRESH_TOKEN"
    }
    then {
        identifier = SYNC_USER
    }
}

lorraine.enqueue("user_refresh_chain", operation)
```

## 🔍 Observing Work

You can monitor the status of your tasks in real-time:

```kotlin
lorraine.listenLorrainesInfo().collect { infoList ->
    infoList.forEach { info ->
        println("Task ${info.id}: ${info.state}")
    }
}
```

## 🚧 Roadmap

- [ ] Add support for `PeriodicWork`
- [ ] JVM Support
- [ ] WASM Support
- [ ] Advanced iOS BackgroundTask integration

## 🤝 Contributing

Contributions are welcome! Please feel free to open a pull request or report issues.

## ❤️ Inspirations

- [Koin](https://github.com/InsertKoinIO/koin) - For the elegant DSL structure.
- [WorkManager](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started) - For the core concepts of persistent background work.
- [NSOperation](https://developer.apple.com/documentation/foundation/nsoperation) - For task queueing logic.