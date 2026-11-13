# math-api (vibe coded)

A rebuild of [`math-api`](https://github.com/prule/math-api) from the same PRD, domain model, standards and backlog
as `math-api-one-agent-2`, but with no OpenSpec and no agents: each backlog item is prompted directly, then
reviewed (ponytail + `/code-review`), fixed, given a retro in `retrospectives/`, and merged by PR. The point is to compare the two builds and see whether OpenSpec adds value.

A stateless REST service for exact decimal arithmetic. See [docs/PRD.md](docs/PRD.md) and
[docs/BACKLOG.md](docs/BACKLOG.md); the process is in [CLAUDE.md](CLAUDE.md).

## Comparing the builds

Once both builds have finished the same backlog items, both are scanned with SonarQube using the same quality
profile (bugs, code smells, duplication, cognitive complexity, coverage). Neither build runs Sonar during its own
process, so the scan is a neutral yardstick, not something either build was tuned against.
