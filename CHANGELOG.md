# Changelog

## 1.7.0-SNAPSHOT — 2026-09-23

Paired with Backend 1.7.0-SNAPSHOT and Android 2.2-SNAPSHOT.

- 2026-09-29: AI summaries of teacher reviews. `TeacherReviewsResponse` gains
  `summary: TeacherSummary?` (default null; absent and null decode to null)
  with `reviewCount`, `description`, `pros`, `cons`, `tags` as strings, five
  `TeacherSummaryScale(kind, value, reason)`, `level`, `confidence` and
  `generatedAt`. `teacherSummaryLevels(isus)` →
  `List<TeacherSummaryLevel(teacherIsu, level)>`
  (`GET /api/teachers/summary-levels`, repeated `isu`). Strict decoding:
  `reviewCount` at least 3, exactly five distinct scales, string list entries,
  no reason for `NOT_ENOUGH_DATA`, ISO `generatedAt`, positive `teacherIsu`;
  strict `SummaryLevel`, `SummaryConfidence`, `SummaryScaleKind` and
  `SummaryScaleValue`, while unknown tag codes decode as strings. Contract and
  MockWebServer route tests.

- 2026-09-29: own teacher reviews. `saveMyTeacherReview(isu, request)`
  (`PUT /api/teachers/{isu}/reviews/mine`), `deleteMyTeacherReview(isu)`
  (`DELETE …/mine`), `voteTeacherReview(id, ResourceVoteRequest)`
  (`PUT /api/reviews/{id}/vote`) and `reportTeacherReview(id,
  ModerationReportRequest)` (`POST /api/reviews/{id}/report`), each replying
  with `TeacherReviewsResponse`. The response replaces `external` and
  `ExternalTeacherReview` with the ranked `reviews: List<TeacherReview>`
  (`kind` `COMMUNITY` or `REVIEWS`, `score`, `myVote`, `verified`,
  `reportedByMe`, optional `author`) and adds `mine: OwnTeacherReview?`,
  `canWrite`, `canVote`, `canReport` and `knownTeacher`;
  `SaveTeacherReviewRequest` is anonymous by default. `ReportReason` gains
  `OFFENSIVE` and `WRONG_TEACHER`; moderation decodes `TEACHER_REVIEW` cases as
  `TeacherReviewTarget` with `TeacherReviewRevision` and
  `ModeratedTeacherReview`. Strict decoding of the new models and enums;
  contract, MockWebServer route and moderation tests.

- 2026-09-28: `teacherReviews(isu)` → `TeacherReviewsResponse`
  (`GET /api/teachers/{isu}/reviews`) with anonymous `ExternalTeacherReview`
  copies and the provider URL. Strict required fields, UUID/date parsing,
  integer ranges, duplicate-key rejection and mutually exclusive dates;
  `LocalDateTypeAdapter().nullSafe()` preserves nullable dates. Contract,
  MockWebServer route and schedule-date regression tests.

- 2026-09-24: saving another student's link is removed. `setSubjectLinkSaved`
  (`PUT /api/links/{id}/saved`), `SetLinkSavedRequest` and
  `SubjectLink.isSaved` are gone; an `isSaved` key from an older backend is
  ignored like any unknown key.

- 2026-09-24: web sign-in approved from the app. `myRoles()` →
  `List<String>` (`GET /api/users/me/roles`, unknown roles stay plain strings);
  `webLoginPreview(code)` → `WebLoginPreview(challengeId, userAgent, createdAt,
  expiresAt)` (`GET /api/users/me/web-login/{code}`) and
  `approveWebLogin(challengeId)` → `Unit`
  (`POST /api/users/me/web-login/{challengeId}/approve`). Strict decoding:
  required ID and timestamps, optional string `userAgent`, no duplicate keys.
  Round-trip and MockWebServer route tests.

- 2026-09-24: links go to one schedule flow of any depth. `LinkVisibility` is
  PRIVATE, FLOW, ALL; `flowId` is added to `SubjectLink`,
  `SubjectLinkRevision` and `SaveSubjectLinkRequest` and decodes only together
  with FLOW. `LinkAudience` is now `flowId`, `label`, `typeId`, `depth`.

- Subject links: `subjectLinks(subjectId, period)` → `SubjectLinksResponse`
  (`mine`, `shared`, `previous`, `pinnedId`, `audiences`, `premoderation`);
  `saveSubjectLink(id, SaveSubjectLinkRequest)` with a client-generated ID,
  `deleteSubjectLink`, `setSubjectLinkSaved`, `pinSubjectLink`,
  `voteSubjectLink` and `reportSubjectLink` under `/api/links` and
  `/api/subjects/{subjectId}/links`.
- `SubjectLink` carries `category` (`LinkCategory`), `visibility`
  (`LinkVisibility`), the owner-facing `status` (`SubjectLinkStatus`),
  `audienceLabel`, `reviewNote`, votes, saved and report flags. Flow links
  publish at once; ALL may be premoderated.
- Moderation: `SubjectLinkTarget(revision, link, author, reports,
  submitterHistory)` under `targetType = SUBJECT_RESOURCE` with an immutable
  `SubjectLinkRevision` (`LinkRevisionStatus`).
- Added separate `ItmoWidgetsModerationApi` for queue decisions, restriction
  revocation and full typed policy updates; no moderator methods in the user API.
  `myRestrictions()` lists the viewer's active restrictions.
- POLICY decisions are typed separately from moderator decisions; no synthetic
  moderator identity is used for automatic approval.
- Strict link/moderation decoding: required fields, primitive shapes, integer
  ranges and duplicate keys; strict enums; unknown restriction capabilities
  conservatively decode as ALL. Round-trip and all-route MockWebServer tests.
- Nullable deleted targets preserve case audit.

## 1.2.0 — 2026-09-21

- `friendsOnLesson(pairId, date)` → `List<UserProfile>`: the viewer's accepted
  friends on one lesson occurrence (`GET /api/schedule/lessons/{pairId}/friends?date=`).
  `usersByPairId` is removed.
- `userFriends(isu)`, required `canViewFriends` and `friendsVisibility` wire fields;
  strict decoding and independent friends privacy (server default ALL).

- `FriendshipEventPayload` (`FRIENDSHIP_EVENT_PAYLOAD`) with `REQUEST_RECEIVED`
  and `REQUEST_ACCEPTED`, strict decoding.
- Friendship actions by ISU path returning `UserProfile`; `friends()`,
  `incomingFriendRequests()`, `outgoingFriendRequests()` return `List<UserProfile>`;
  `userProfile(isu)`; `lookupUsers(UserLookupRequest)`; `RelationshipState`.
  `addFriend`, `myFriends` and body-based removal removed.
- `UserSportBookingsResponse.entries` with pending free and auto queue entries;
  older responses without `entries` decode to an empty list.
- `UserPrivacySettings` with `scheduleVisibility` and `sportVisibility`
  audiences; `UserData.capabilities` (`canViewSchedule`, `canViewSport`) required.
  The boolean settings contract removed.
- `AppVersionInfo` for `GET /api/app/version-info`.
- `SportLessonDto.buildingId` nullable to carry real venues.
- Unauthenticated Backend requests handled safely; current device unregister API.

## 1.1.9 and earlier

Reciprocal friend requests, boolean privacy, sport queue contracts, device
registration, MyITMO-backed token attachment.
