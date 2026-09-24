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
- Subject link, restriction and moderation models: required fields, primitive
  shapes, integer ranges and duplicate keys are checked before reflection
  (`SubjectLinkModelsTypeAdapterFactory`, `ModerationModelsTypeAdapterFactory`).
  `LinkCategory`, `LinkVisibility`, `SubjectLinkStatus`, `LinkRevisionStatus`,
  report reason and moderation enums are strict: unknown names, null and
  numbers fail. `SubjectLink.myVote` must be -1, 0 or 1; a revision `number`
  must be positive.
- Unknown `RestrictionCapability` strings conservatively decode as ALL so a
  newer server cannot accidentally enable an action on an older client. Missing,
  null or non-string capabilities still fail. This is the only enum fallback.
- `ModerationPolicy`: all five fields are required on the wire despite local
  constructor defaults. `ModerationCaseTarget` is polymorphic on `targetType`;
  only SUBJECT_RESOURCE (`SubjectLinkTarget`) is implemented. TEACHER_REVIEW is
  reserved in the enum. The target is nullable because a deleted link leaves its
  case for audit; other case fields and decisions remain required.

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
| Schedule | `syncLessons`, `userLessons`, `friendsOnLesson` |
| Friends | `sendFriendRequest`, `acceptFriendRequest`, `rejectFriendRequest`, `cancelFriendRequest`, `removeFriend`, `friends`, `incomingFriendRequests`, `outgoingFriendRequests` |
| Users | `userFriends`, `userProfile`, `lookupUsers`, `myPrivacySettings`, `updateMyPrivacySettings`, `updateIdTokenData`, `myUserData` |
| Subject links | `subjectLinks`, `saveSubjectLink`, `deleteSubjectLink`, `setSubjectLinkSaved`, `pinSubjectLink`, `voteSubjectLink`, `reportSubjectLink`, `myRestrictions` |
| Moderation (separate `ItmoWidgetsModerationApi`) | `moderationCases`, `decide`, `userRestrictions`, `revokeRestriction`, `moderationSettings`, `updateModerationSettings` |
| Sport | `syncSportLessons`, `friendsSportBookings`, `userSportBookings`, free-sign and auto-sign entry, queue and limit calls |

Semantics of each route are documented in the Backend repository under
`docs/contracts/`.

## Tests

`src/test/kotlin`: contract tests per group (`DeviceApiContractTest`,
`PrivacyApiContractTest`, `SportApiContractTest`, `social/SocialApiContractTest`,
`AppVersionApiContractTest`, `FcmPayloadContractTest`, `TokenInterceptorTest`)
using MockWebServer and synthetic data. Link and moderation models and all
user/moderator routes are covered by `resources/SubjectLinkContractTest` and
`resources/SubjectLinkApiTest`.

## Subject links

| Method | Route | Body | Reply |
|---|---|---|---|
| `subjectLinks` | `GET /api/subjects/{subjectId}/links?period=` | — | `SubjectLinksResponse` |
| `saveSubjectLink` | `PUT /api/links/{id}` | `SaveSubjectLinkRequest` | `SubjectLink` |
| `deleteSubjectLink` | `DELETE /api/links/{id}` | — | `Unit` |
| `setSubjectLinkSaved` | `PUT /api/links/{id}/saved` | `SetLinkSavedRequest` | `SubjectLink` |
| `pinSubjectLink` | `PUT /api/subjects/{subjectId}/links/pin` | `PinSubjectLinkRequest` | `SubjectLinksResponse` |
| `voteSubjectLink` | `PUT /api/links/{id}/vote` | `ResourceVoteRequest` | `SubjectLink` |
| `reportSubjectLink` | `POST /api/links/{id}/report` | `ModerationReportRequest` | `SubjectLink` |

A link has one of `LinkCategory` (`SCORES, QUEUE, MATERIALS, TASKS, RECORDINGS,
NOTES, EXAM, CHAT, OTHER`) and a `LinkVisibility` (`PRIVATE, FLOW, ALL`).
FLOW publishes at once to one schedule flow of the author named by `flowId`
(lectures `ФИЗ ПИИКТ 3`, practice `3.2` or labs `3.2.1`); ALL goes through
premoderation when `premoderation` is true. `flowId` is present exactly with
FLOW in `SubjectLink`, `SubjectLinkRevision` and `SaveSubjectLinkRequest`;
decoding rejects FLOW without it, another visibility with it, and non-integer
values. `audienceLabel` is the flow's schedule name. `status` is the owner's view
(`PRIVATE, PENDING, PUBLISHED, REJECTED, HIDDEN`); other viewers always get
PUBLISHED. The period key is `YYYY-S`.

`saveSubjectLink` creates a link under a client-generated UUID or replaces the
viewer's own one. `mine` holds the viewer's links, `shared` the visible links of
others, `previous` approved ALL links of past periods, `audiences` the viewer's
flows as `LinkAudience(flowId, label, typeId, depth)` sorted by depth, then
label (`depth` is at least 1). `PinSubjectLinkRequest.linkId = null`
removes the pin. Optional fields (`title`, `flowId`, `audienceLabel`, `reviewNote`,
`author`, `pinnedId`, `linkId`) are omitted from Core requests when null and
accepted both absent and null in responses.

Automatic decisions have required `actor=POLICY`, null moderatorId and APPROVE;
MODERATOR decisions require moderatorId. Link moderation cases carry the
reviewed immutable `SubjectLinkRevision` and the link itself.
