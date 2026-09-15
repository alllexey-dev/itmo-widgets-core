# ITMO.Widgets Core agent guide

The ecosystem-wide rules are in the Android repository's `AGENTS.md`
(`/Users/alllexey/proj/ITMO.Widgets.copy/AGENTS.md`). This file adds what is
specific to Core.

## Responsibilities

Core is the typed Kotlin contract and Retrofit client for ITMO.Widgets Backend:
DTOs, `ItmoWidgetsApi`, the Gson configuration with strict adapters, token
attachment through MyItmoApi, and the FCM payload types. It holds no Android UI
or application state.

## Hard rules

- Backend implements a change first; Core mirrors it exactly: `@Path` for path
  variables, `@Query` for query parameters, serialized names identical to the
  wire contract. Android consumes last.
- Required fields decode strictly. A missing or unknown enum value is a
  `JsonParseException`, never a default that widens access or enables an action.
  Optional fields are nullable only when the wire field is genuinely optional.
- Every public API change ships with a JSON round-trip test and a MockWebServer
  contract test.
- Binary and source compatibility are preserved unless a coordinated version
  bump allows a break. The version stays `1.2.0-SNAPSHOT` until Android 2.1.
- `publishToMavenLocal` is coordinated development, not a release; Maven Central
  publication happens only on explicit request.

## Build

The artifact targets JVM 11; this machine builds it with JDK 17:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew build publishToMavenLocal
```

Do not commit `.idea/` changes.
