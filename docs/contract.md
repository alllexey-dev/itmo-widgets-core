# Wire conventions

Rules that every Core type follows so that Backend, Core and Android agree.

## Envelope

Every Backend response is `ApiResponse<T>`: `success`, `data` (nullable on
failure) and `error { code, message }`. Requests carry the current ITMO.ID
access token obtained through MyItmoApi; `ItmoWidgetsImpl.getValidToken()`
refreshes it transparently and an unauthenticated call fails safely instead of
sending an empty header.

## Decoding policy

`ItmoWidgetsImpl` builds one Gson instance with adapters for every type whose
Kotlin non-null contract Gson's reflective adapter could bypass:

- `UserData` and `UserCapabilities`: capabilities are required; missing or
  malformed ones fail decoding rather than granting access.
- `UserProfile` and `RelationshipState`: both fields required; an unknown
  relationship string fails.
- `UserPrivacySettings` and `SharingVisibility`: all three audiences required.
- `UserSportBookingsResponse`: `lessonIds` required; missing `entries` decode
  to an empty list for older servers.
- `SportQueueEntry` and `SportQueue`: polymorphic on `type` (`free` / `auto`).
- `FriendshipEventPayload`: event, actor and time required.
- `OffsetDateTime` through the shared adapter.

`friendsVisibility` defaults to ALL only for explicitly constructed settings;
wire responses must include it. `canViewFriends` is required on the wire and
legacy two-argument constructors default it to false. Consume these coordinated
snapshots only with Backend V3; older privacy PUTs fail closed rather than
resetting a saved audience.

A consumer never has to null-check a required field; if it decoded, it is complete.

## FCM payloads

`FcmTypedWrapper<T>(type, payload)` is what Backend serializes into the `data`
key; `FcmJsonWrapper(type, payload: JsonElement)` is what the client reads before
choosing a decoder by `type`. Payload types implement `FcmPayload.getType()` and
live in `model/fcm/impl`.

## Endpoint groups

| Group | Methods |
|---|---|
| Device | `registerDevice`, `unregisterCurrentDevice` |
| App | `latestAppVersion`, `appVersionInfo` |
| Schedule | `syncLessons`, `userLessons`, `usersByPairId` |
| Friends | `sendFriendRequest`, `acceptFriendRequest`, `rejectFriendRequest`, `cancelFriendRequest`, `removeFriend`, `friends`, `incomingFriendRequests`, `outgoingFriendRequests` |
| Users | `userFriends`, `userProfile`, `lookupUsers`, `myPrivacySettings`, `updateMyPrivacySettings`, `updateIdTokenData`, `myUserData` |
| Sport | `syncSportLessons`, `friendsSportBookings`, `userSportBookings`, free-sign and auto-sign entry, queue and limit calls |

Semantics of each route are documented in the Backend repository under
`docs/contracts/`.

## Tests

`src/test/kotlin`: contract tests per group (`DeviceApiContractTest`,
`PrivacyApiContractTest`, `SportApiContractTest`, `social/SocialApiContractTest`,
`AppVersionApiContractTest`, `FcmPayloadContractTest`, `TokenInterceptorTest`)
using MockWebServer and synthetic data.
