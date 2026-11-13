# math-api (vibe coded)

Stateless Spring Boot 4 / Java 25 REST API for exact decimal arithmetic, rebuilt from the same docs as
`../mathi-api-one-agent-2`, but with no OpenSpec and no agent pipeline, so the two builds can be compared.

Read before working:
- `docs/PRD.md` — scope, API contract, non-goals, roadmap
- `docs/DOMAIN-MODEL.md` — glossary and business rules (BR-n)
- `docs/STANDARDS.md` — architecture, conventions, testing
- `docs/BACKLOG.md` — ordered items; update an item's status when you start it and when its PR merges

Any change that alters scope, the API contract, domain terms or business rules, or standards MUST update the
matching doc in `docs/` in the same change, including its change log. Never renumber or reuse a BR id.

## Process

One backlog item at a time, on a branch `feat/<item>` from an up-to-date `main`:

1. **Build**: the user prompts with the item's text from `docs/BACKLOG.md`. Implement it; done when `./gradlew clean build` passes.
2. **Review**: run both on the change since `main`:
   - `/ponytail:ponytail-review`: is it correct, safe, tested and minimal?
   - `/code-review`: does it follow `docs/STANDARDS.md` and meet the backlog item?
3. **Fix**: fix every finding from either review, rerun `./gradlew build`.
4. **Retro**: run `/ponytail:ponytail-debt`, then write `retrospectives/<n>-<item>.md` with four sections:
   - **Caught by review**: each finding from both reviews and how it was fixed.
   - **Surprises**: each failed check or bug found while building, with its cause, or "cause unknown".
   - **Shortcuts taken**: each `shortcut:` comment this item added, with its file, its limit and when to upgrade.
   - **Lessons**: what to do differently next time.

   Write "None" under a section with nothing to report.
5. **PR**: commit (Conventional Commits), push, open a PR filling in `.github/pull_request_template.md`, squash merge once CI is green.
