# Changelog

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
