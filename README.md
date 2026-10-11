# ITMO.Widgets Core (frozen)

Typed Retrofit client for the ITMO.Widgets Backend, `dev.alllexey:itmo-widgets-core` on Maven Central.

**Frozen at 1.7.0.** The app no longer uses this library: since 2.3 the Backend client lives in
[ITMO.Widgets](https://github.com/alllexey-dev/ITMO.Widgets) as the `:shared:backend-client` module. The published
jars stay on Maven Central for installed 2.1/2.2 apps and for the Backend's compatibility tests.

- Contract conventions of 1.x: [docs/contract.md](docs/contract.md)
- History: [CHANGELOG.md](CHANGELOG.md)

## A 1.7.x fix, if one is ever needed

1. Build and test with JDK 17: `./gradlew build`.
2. Bump `version` in `build.gradle.kts`, add a CHANGELOG entry, publish to Maven Central.
3. Retrofit, Gson and OkHttp come only transitively through `my-itmo-api` 1.8.2; pin them explicitly if that changes.
