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
| `shared`     | KMP-bibliotheek   | `@Serializable` contractklassen (models, requests, responses)      |
| `composeApp` | KMP-bibliotheek   | Compose Multiplatform-UI (voorlopig alleen Android)                 |
| `androidApp` | Android-app       | Android-app (`nl.carsharing.app`) die `composeApp` start            |

## Android SDK

Voor `composeApp` en `androidApp` is een Android SDK nodig. Amper downloadt ontbrekende SDK-onderdelen (build-tools,
platform) zelf bij de eerste build; die build duurt daardoor langer en heeft internet nodig. Op een machine zonder enige
Android SDK is dit nog niet geverifieerd (getest met een bestaande Android Studio-SDK met geaccepteerde licenties in
`%LOCALAPPDATA%\Android\Sdk`). Faalt de build daar: installeer de SDK via Android Studio of accepteer de licenties met
`sdkmanager --licenses`. Of dit op Linux werkt, bevestigt de CI (ubuntu) bij de eerste run.
Afspraak in het team: iedereen moet de Android-modules kunnen bouwen voordat er gemerged wordt.

## Building & Running

To build or run the project, use one of the following tasks:

| Task             | Description       |
|------------------|-------------------|
| `./kotlin test`  | Run the tests     |
| `./kotlin build` | Build the project |
| `./kotlin run`   | Run the server    |

The server needs the environment variable `JWT_SECRET` (at least 32 characters) and refuses to start without it.
The tests use their own secret, so `./kotlin test` works without it.

```
export JWT_SECRET="<random string of at least 32 characters>"   # Windows PowerShell: $env:JWT_SECRET = "..."
./kotlin run
```

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

De debug-APK van de Android-app staat na een build in
`build/tasks/_androidApp_buildAndroidDebug/gradle-project-debug.apk`.

## App en lokale server

De server luistert lokaal op `http://0.0.0.0:8080`. In de Android-emulator is de eigen machine bereikbaar via
`10.0.2.2`, dus de app praat standaard met `http://10.0.2.2:8080` (`DEFAULT_BASE_URL` in
`composeApp/src/api/ApiClient.kt`). Start eerst de server met `./kotlin run` en daarna de app in de emulator.

- Onversleuteld HTTP is alleen toegestaan voor `10.0.2.2` en `localhost`
  (`androidApp/res/xml/network_security_config.xml`).
- Op een echt toestel werkt `10.0.2.2` niet: geef dan het IP-adres van je pc in het lokale netwerk mee aan
  `createApiClient(baseUrl = ...)` en voeg dat adres toe aan de network-security-config.
