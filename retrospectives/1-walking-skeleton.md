# 1. Walking skeleton — retrospective

## Caught by review

- **`/code-review` standards:** `docs/BACKLOG.md` still said "Not started". Fixed: item 1 set to In progress.
- **`/code-review` standards:** the `shortcut:` comment sat above the layer rule only, but `allowEmptyShould` is on all three rules. Fixed: moved to the class javadoc and names both switches.
- **`/code-review` spec:** the `UsesSpring` fixture was a `@Component` inside the component-scanned package, so the smoke test's context loaded a rule-breaking bean. Fixed: the fixture now only holds an `ApplicationContext` field, enough for the ArchUnit dependency, and is not a bean.
- **`/code-review` spec:** the domain rule was a blocklist (Spring, Jakarta, Jackson); STANDARDS §2 says "plain Java only". Fixed: allowlist — domain may only depend on `java..` and `..mathapi.domain..`.
- **`/code-review` spec:** the application rule missed Boot 4's `org.springframework.boot.webmvc..`. Fixed: added.
- **Not acted on (judgement calls):** the two `noClasses`-shaped rules look alike (two rules, not worth extracting); `..mathapi.adapter..` in the application rule overlaps the layer rule (kept: it also covers a future `adapter.out`).
- **`/ponytail:ponytail-review`:** no findings.

## Surprises

- `gradle wrapper` failed in an empty folder until `settings.gradle.kts` existed. Cause: Gradle 9 refuses to run without a settings file.

## Shortcuts taken

- `src/test/java/com/paulrule/mathapi/architecture/ArchitectureTest.java`: every rule allows empty packages (`withOptionalLayers` / `allowEmptyShould`). Remove both in backlog item 2, once every layer has a class.

## Lessons

- The ponytail review missed the fixture being a scanned bean; the spec reviewer caught it. When a test fixture lives under the main base package, check what component scanning picks up.
- Set the backlog status as the first edit of an item, before writing code.
