<h1 align="center">ITMO.Widgets Core</h1>

<p align="center">
  <strong>Компонент для уникальных функций приложения <a href="https://github.com/alllexey-dev/ITMO.Widgets">ITMO.Widgets</a> </strong>
</p>

**ITMO.Widgets Core** — это ключевой компонент экосистемы ITMO.Widgets, предоставляющий унифицированный доступ к API сервисов проекта.<br>
Проект использует <a href="https://github.com/alllexey-dev/my-itmo-api">my-itmo-api</a> как основу для аутентификации в сервисах.


<a href="https://github.com/users/alllexey-dev/projects/1"><strong>Roadmap & status </strong></a>

### 🌟 Текущие возможности
* **Управление аутентификацией:** Автоматическое и прозрачное управление access и refresh токенами, включая их обновление.
* **API для сервисов ITMO.Widgets:** Полная реализация модели и методов API.
* **FCM-модель для приложения ITMO.Widgets:** Полная реализация модели данных, получаемых приложением через Google FCM.

### 🛠️ Зависимости
* `my-itmo-api`
* `OkHttp`
* `Retrofit`
* `Kotlin Coroutines Core`

### 🚀 Использование

Добавьте в pom.xml:

```xml
<dependencies>
    <dependency>
        <groupId>dev.alllexey</groupId>
        <artifactId>itmo-widgets-core</artifactId>
        <version>1.1.6</version>
    </dependency>
</dependencies>
```

## Unreleased friendship contract: 1.2.0-SNAPSHOT

The version remains 1.2.0-SNAPSHOT by explicit request. This revision changes the
unreleased social API and requires the matching Backend friendship revision:

- `sendFriendRequest`, `acceptFriendRequest`, `rejectFriendRequest`,
  `cancelFriendRequest` and `removeFriend(isu)` use ISU path parameters and return
  a fresh `UserProfile`, including viewer capabilities.
- `friends()`, `incomingFriendRequests()` and `outgoingFriendRequests()` return
  `List<UserProfile>`. The old `addFriend`, `myFriends` and body-based removal
  contract have been removed.
- `userProfile(isu)` reads a public profile; `lookupUsers(UserLookupRequest)`
  annotates up to 50 positive ISUs, never performs a name search.
- `RelationshipState` is viewer-relative: NONE, OUTGOING, INCOMING, FRIENDS;
  BLOCKED is reserved. Unknown/missing state and missing viewer capabilities fail
  decoding instead of enabling actions or access.
- `UserSportBookingsResponse.entries` contains the same free/auto queue types as
  `FriendSportBooking.entry`. Confirmed IDs remain separate; older responses that
  omit entries decode to an empty list.

The earlier dependency example describes the public release, not this snapshot.
No new artifact has been published publicly or to MavenLocal in this change.
Android is still on the previous locally resolved 1.2.0-SNAPSHOT API until its
Stage 4 repository integration. Do not overwrite that artifact just to run tests.
Coordinated Backend verification can use Gradle `--include-build ../itmo-widgets-core`
to resolve the exact source without replacing a shared snapshot.

Run `./gradlew build` with JDK 11; tests use MockWebServer and synthetic data.
