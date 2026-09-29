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
  constructor defaults. `ModerationCaseTarget` is polymorphic on `targetType`:
  SUBJECT_RESOURCE is `SubjectLinkTarget`, TEACHER_REVIEW is
  `TeacherReviewTarget` (`revision`, `review`, `author`, `reports`,
  `submitterHistory`, all required). The target is nullable because a deleted
  link or review leaves its case for audit; other case fields and decisions
  remain required. `ReportReason` holds the link reasons (`BROKEN`,
  `WRONG_SUBJECT`, `SPAM`, `OTHER`) and the review reasons (`OFFENSIVE`,
  `WRONG_TEACHER`, `SPAM`, `OTHER`) in one strict enum; Backend refuses a reason
  of the other kind.
- `WebLoginPreview`: `challengeId`, `createdAt` and `expiresAt` are required
  strings that must parse as a UUID and ISO offset timestamps; `userAgent` is
  optional but, when present, a string (`WebLoginPreviewTypeAdapterFactory`).
- Teacher review models (`TeacherReviewModelsTypeAdapterFactory`): required
  fields, primitive shapes, integer ranges and duplicate keys are checked before
  reflection, in nested objects too.
  - `TeacherReviewsResponse` requires a positive integer `teacherIsu`, string
    `providerUrl`, a non-null `reviews` array without null entries and the
    booleans `canWrite`, `canVote`, `canReport`, `knownTeacher`; `mine` is
    optional. A response in the replaced shape with `external` fails.
    `summary` is optional: absent and null both decode to null, any other
    non-object value fails.
  - `TeacherReview` requires a UUID-parsable `id`, `kind`, `text`, integer
    `score`, `myVote` of -1, 0 or 1 and the booleans `verified` and
    `reportedByMe`. Optional strings reject other shapes; `author` is absent,
    null or an object. `writtenOn` must parse as an ISO `LocalDate`,
    `writtenBeforeYear` must be a JSON integer in the `Int` range, and they
    cannot both be set. A `COMMUNITY` review has no source and no before-year;
    a `REVIEWS` copy has no author and is neither verified nor reported.
  - `OwnTeacherReview` requires `id`, `text`, `anonymous`, `status`, `score`,
    `verified` and an ISO `writtenOn`.
  - `SaveTeacherReviewRequest` requires `text`, `anonymous` and `flowIds`
    (JSON integers in the `Long` range) when read: Gson does not apply Kotlin
    defaults, and a missing `anonymous` must never reveal a name.
  - `TeacherReviewRevision` requires UUIDs, a positive `number`, `text`,
    `status` and `submittedAt`; `ModeratedTeacherReview` a positive
    `teacherIsu`, `anonymous`, `status`, `score`, `hidden` and `verification`,
    with an integer `verifiedFlowId` when present.
  - `TeacherSummary` requires an integer `reviewCount` of at least 3, a string
    `description`, the arrays `pros`, `cons`, `tags` and `scales`, `level`,
    `confidence` and `generatedAt`. Entries of `pros`, `cons` and `tags` must be
    strings; null entries fail. `scales` holds exactly five objects with string
    `kind` values, each kind once. `generatedAt` must parse as an ISO `Instant`
    (a local date-time, a bare date or a number fails). `tags` are not an enum:
    they stay plain strings, so a tag code added by a newer server decodes and
    consumers skip codes they do not know.
  - `TeacherSummaryScale` requires `kind` and `value`; `reason` is an optional
    string and must be absent or null when `value` is `NOT_ENOUGH_DATA`. With
    another value a null `reason` is accepted.
  - `TeacherSummaryLevel` requires a positive integer `teacherIsu` and `level`;
    the ISU range and the 1–50 id limit are checked by Backend.
  - `TeacherReviewKind`, `TeacherReviewStatus`, `ReviewRevisionStatus`,
    `ReviewVerification`, `SummaryLevel`, `SummaryConfidence`,
    `SummaryScaleKind` and `SummaryScaleValue` are strict: unknown names, null
    and numbers fail.

  The shared `LocalDateTypeAdapter` is registered with `nullSafe()` for nullable
  review dates; schedule request dates keep the same wire representation.
- `myRoles()` returns plain strings so a role added by a newer server never
  breaks decoding; consumers compare against the names they know.

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
| Users | `userFriends`, `userProfile`, `lookupUsers`, `myPrivacySettings`, `updateMyPrivacySettings`, `updateIdTokenData`, `myUserData`, `myRoles` |
| Web sign-in | `webLoginPreview`, `approveWebLogin` |
| Teacher reviews | `teacherReviews`, `saveMyTeacherReview`, `deleteMyTeacherReview`, `voteTeacherReview`, `reportTeacherReview`, `teacherSummaryLevels` |
| Subject links | `subjectLinks`, `saveSubjectLink`, `deleteSubjectLink`, `pinSubjectLink`, `voteSubjectLink`, `reportSubjectLink`, `myRestrictions` |
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
`resources/SubjectLinkApiTest`. Roles and web sign-in are covered by
`weblogin/WebLoginContractTest` and `weblogin/WebLoginApiTest`. Teacher review
and summary models and all six routes are covered by
`reviews/TeacherReviewContractTest` and `reviews/TeacherReviewApiTest` (strict
decoding, exact keys, the anonymous default, the rejected `external` shape, an
absent or null `summary`, an unknown tag code kept as a string, rejected
summaries and levels, the repeated `isu` parameter and schedule-date
serialization regression checks); `reviews/TeacherReviewModerationContractTest` covers
TEACHER_REVIEW cases and the review report reasons. Shared fixtures are in
`reviews/TeacherReviewContractFixtures`.

## Teacher reviews

| Method | Route | Body | Reply |
|---|---|---|---|
| `teacherReviews(isu)` | `GET /api/teachers/{isu}/reviews` | — | `TeacherReviewsResponse` |
| `saveMyTeacherReview(isu, request)` | `PUT /api/teachers/{isu}/reviews/mine` | `SaveTeacherReviewRequest` | `TeacherReviewsResponse` |
| `deleteMyTeacherReview(isu)` | `DELETE /api/teachers/{isu}/reviews/mine` | — | `TeacherReviewsResponse` |
| `voteTeacherReview(id, request)` | `PUT /api/reviews/{id}/vote` | `ResourceVoteRequest` | `TeacherReviewsResponse` |
| `reportTeacherReview(id, request)` | `POST /api/reviews/{id}/report` | `ModerationReportRequest` | `TeacherReviewsResponse` |
| `teacherSummaryLevels(isus)` | `GET /api/teachers/summary-levels?isu=…` (repeated `isu`) | — | `List<TeacherSummaryLevel>` |

Every method except `teacherSummaryLevels` replies with the viewer's reviews
of the teacher: `teacherIsu`, the Reviews teacher page `providerUrl`, `reviews`
in Backend's ranked order, the viewer's own `mine` (or null), the flags
`canWrite`, `canVote`, `canReport` and `knownTeacher`, and the shown AI
`summary` (or null). A `TeacherReview` is `COMMUNITY` (an own review
of a user: `verified`, `reportedByMe`, `author` only when written under the
name) or `REVIEWS` (a copy: optional source title and link, a date or a
before-year). `OwnTeacherReview` carries the author's current content,
`anonymous`, the status `PENDING`, `PUBLISHED`, `REJECTED` or `HIDDEN` with
`reviewNote`, `score`, `verified` and `writtenOn`.
`SaveTeacherReviewRequest(subjectTitle, text, anonymous = true, flowIds =
emptyList())` creates or edits the review; `flowIds` are candidate ISU flows
that Backend checks itself. `ResourceVoteRequest.value` is -1, 0 or 1; review
reports use `OFFENSIVE`, `WRONG_TEACHER`, `SPAM` or `OTHER`. The moderation API
decodes TEACHER_REVIEW cases as `TeacherReviewTarget` with
`TeacherReviewRevision` and `ModeratedTeacherReview` (`teacherName`,
`verification`, `verifiedFlowId`).

`summary` is null when the teacher has no shown summary or an admin hides it.
A `TeacherSummary` carries `reviewCount` (the reviews the shown summary was
built from, at least 3; it may lag behind the current reviews until a new
summary is built), `description`, `pros`, `cons`, `tags` (codes of Backend's
fixed list as strings), five `TeacherSummaryScale(kind, value, reason)` in
`SummaryScaleKind` order (`EXPLAINS`, `ATTITUDE`, `FAIRNESS`, `STRICTNESS`,
`WORKLOAD`; value `LOW`, `MEDIUM`, `HIGH` or `NOT_ENOUGH_DATA` without a
reason), the tone `level` (`VERY_NEGATIVE`, `NEGATIVE`, `MIXED`, `POSITIVE`,
`VERY_POSITIVE`), `confidence` (`LOW`, `MEDIUM`, `HIGH`) and `generatedAt`.
`teacherSummaryLevels(isus)` sends 1–50 distinct ISU ids as repeated `isu`
parameters and returns `TeacherSummaryLevel(teacherIsu, level)` only for
teachers whose shown summary has `MEDIUM` or `HIGH` confidence; others are
absent from the list. The
[Backend contract](../../itmo-widgets-backend/docs/contracts/teacher-reviews.md)
defines ordering, limits, authentication and errors.

## Subject links

| Method | Route | Body | Reply |
|---|---|---|---|
| `subjectLinks` | `GET /api/subjects/{subjectId}/links?period=` | — | `SubjectLinksResponse` |
| `saveSubjectLink` | `PUT /api/links/{id}` | `SaveSubjectLinkRequest` | `SubjectLink` |
| `deleteSubjectLink` | `DELETE /api/links/{id}` | — | `Unit` |
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

## Web sign-in and roles

| Method | Route | Body | Reply |
|---|---|---|---|
| `myRoles` | `GET /api/users/me/roles` | — | `List<String>` (e.g. `MODERATOR`, `ADMIN`) |
| `webLoginPreview` | `GET /api/users/me/web-login/{code}` | — | `WebLoginPreview` |
| `approveWebLogin` | `POST /api/users/me/web-login/{challengeId}/approve` | — | `Unit` (`data: {}`) |

The site shows a short code (also inside its QR link); the app looks it up with
`webLoginPreview` and shows `WebLoginPreview(challengeId, userAgent, createdAt,
expiresAt)` before the user approves it by `challengeId`. Backend accepts both
calls only with an app ITMO.ID token, not a web session. An unknown, used or
expired code or challenge is HTTP 404 `not_found`, which Retrofit raises as
`HttpException`.
