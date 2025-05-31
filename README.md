# Lorraine

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.dottttt.lorraine/lorraine/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.dottttt.lorraine/lorraine)

Lorraine is a work management framework for tasks.

### Setup

First add the dependency to your project:

Lastest version: https://mvnrepository.com/artifact/io.github.dottttt.lorraine/lorraine

```toml
[versions]
lorraine = "$lastestversion"

[libraries]
lorraine = { module = "io.github.dottttt.lorraine:lorraine", version.ref = "lorraine" }
```

## Using it

#### Getting started

Initialize Lorraine with workers.

> Still not satisfied with the current initialization workflow. Any input are welcome.

Shared
```kotlin
const val GET_WORKER = "GET_WORKER"

fun init(context: LorraineContext) {
    val lorraine = startLorraine(context) {
        work(GET_WORKER) { GetWorker() }
        ...
    }
}
```

Android
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        init(createLorraineContext(this))
    }
}
```

iOS
```kotlin
init(createLorraineContext())
```

Create workers by extending `WorkLorraine`

```kotlin
class GetWorker : WorkLorraine() {

    override suspend fun doWork(inputData: Data?): LorraineResult {
        ...
        return LorraineResult.success() / LorraineResult.retry() / LorraineResult.failure()
    }
    
}
```

Launch your worker

```kotlin
lorraine.enqueue(
    uniqueId = "UNIQUE_ID",
    type = ExistingLorrainePolicy.APPEND,
    request = lorraineRequest {
        identifier = GET_WORKER
        constraints { 
            requiredNetwork = true
        }
    }
)
```

## Reporting Issues / Support

Looking for application willing to use it, and have some feedback to add further improvement.

Report any issue on this GitHub repository.

## To Do

- [ ] Add support for PeriodicWork 
- [ ] Add support for JVM
- [ ] Add support for WASM
- [ ] Review BackgroundTask in iOS, to maybe use it

## Inspirations

- Koin (for dsl): https://github.com/InsertKoinIO/koin
- WorkManager (for Android): https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started
- NSOperation (for iOS): https://developer.apple.com/documentation/foundation/nsoperation

## Contributing

Feel free to open pull request
