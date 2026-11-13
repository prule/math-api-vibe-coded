# 5a. API docs toggle per environment — retrospective

## Caught by review

- **`/code-review` standards (judgement call):** the STANDARDS §6 testing table didn't list profile tests, though §8 requires one per profile file. This is a third `@SpringBootTest`, with its own context. Fixed: a "Profile" row in §6 plus a change-log entry.
- **`/code-review` standards (judgement call):** `stillServesTheApi` has the same shape as `SmokeTest.calculatesEndToEnd`. Kept, because it is the only check that `add-mappings: false` leaves the API working under `prod`. Renamed to `stillServesTheApiWithStaticResourcesOff` so its name says why it exists.
- **`/code-review` spec (judgement call):** asserting `type: about:blank` on the 404s is stricter than "not served". Kept on purpose: PRD §3.2 says every error, framework 404s included, is Problem Details with a `type`.
- **`/code-review` spec (note, no change):** `add-mappings: false` turns off every static resource under `prod`, not only the webjar. That is harmless today (there is no `static/` folder), and STANDARDS §8 records it as intended.
- **`/ponytail:ponytail-review`:** nothing to fix. It noted that `/actuator/health` under `prod` had no check; the spec reviewer then booted the app with `SPRING_PROFILES_ACTIVE=prod` and saw health return 200.

## Surprises

None. The build passed on the first run. To prove the test is meaningful, I deleted `application-prod.yaml` (all 5 path cases failed). Then I re-enabled only `add-mappings` (only the webjar case failed). So each of the three settings is needed.

## Shortcuts taken

None.

## Lessons

- When a test passes on the first run, break the code on purpose to watch it fail. Do it once per setting, so the test shows each line of config is needed.
- PRD and STANDARDS already described this item in full. That made the build quick, but cross-references such as the §6 testing table can still drift. Search the docs for every table that lists the kind of thing you are adding.
