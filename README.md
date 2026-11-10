# Kotlin KTOR

![Tests](https://github.com/MarkPrins18/hbo-leerjaar2/actions/workflows/test.yml/badge.svg?branch=main)

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

* [Ktor Documentation](https://ktor.io/docs/home.html)
* [Ktor GitHub page](https://github.com/ktorio/ktor)
* [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). [Request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up).

## Features

Here's a list of features included in this project:

| Name                                                                                  | Description                                                                        |
|---------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [Content Negotiation](https://start.ktor.io/p/io.ktor/server-content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [kotlinx.serialization](https://start.ktor.io/p/io.ktor/server-kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Exposed](https://start.ktor.io/p/org.jetbrains/server-exposed)                       | Adds Exposed database to your application                                          |
| [Status Pages](https://start.ktor.io/p/io.ktor/server-status-pages)                   | Provides exception handling for routes                                             |

## Modules

| Module       | Type              | Beschrijving                                                        |
|--------------|-------------------|---------------------------------------------------------------------|
| `server`     | JVM (Ktor)        | De Web API: routes, services, repositories en database              |
| `shared`     | KMP-bibliotheek   | `@Serializable` contractklassen (models, dto, requests, responses)  |
| `composeApp` | KMP-bibliotheek   | Compose Multiplatform-UI (voorlopig alleen Android)                 |
| `androidApp` | Android-app       | Android-app (`nl.carsharing.app`) die `composeApp` start            |

## Android SDK

Voor `composeApp` en `androidApp` is een Android SDK nodig. Op Windows is geverifieerd dat Amper die zelf downloadt
bij de eerste build (command-line tools, build-tools en platform), zodat `ANDROID_HOME` niet nodig is. De eerste build
duurt daardoor langer en heeft internet nodig. Of dit ook op Linux werkt, bevestigt de CI (ubuntu) bij de eerste run.
Afspraak in het team: iedereen moet de Android-modules kunnen bouwen.

## Building & Running

To build or run the project, use one of the following tasks:

| Task             | Description       |
|------------------|-------------------|
| `./kotlin test`  | Run the tests     |
| `./kotlin build` | Build the project |
| `./kotlin run`   | Run the server    |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

De debug-APK van de Android-app staat na een build in
`build/tasks/_androidApp_buildAndroidDebug/gradle-project-debug.apk`.
