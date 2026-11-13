# 6. More binary operations (power, modulo) — retrospective

## Caught by review

- **`/ponytail:ponytail-review`:** the "rounds once" test could not fail. It also passes if the code uses `BigDecimal.pow(n, DECIMAL128)`, which the Javadoc allows to be off in the last digit (a probe found 37 wrong results in a small sample). Fixed: the test adds `0.2857142857142857142857142857142857^668`, where `pow(n, mc)` answers `…838` and the correct answer is `…839`. I swapped `pow(n, mc)` in and saw the test fail.
- **`/code-review` spec:** PRD §4 said BR-1 prevents CPU exhaustion "via huge exponents" (that is now BR-5's job). PRD §7 still said "all four operations". Neither PRD nor DOMAIN-MODEL had a change-log entry. Fixed: §4 now cites BR-5, §7 says "every operation", and both change logs have an entry.
- **`/code-review` standards:** DOMAIN-MODEL §2 says a new failure needs "a domain exception and its Problem Details handler; nothing else changes". In fact it also needs a `ProblemType` constant and a documented example (STANDARDS §4). Fixed: §2 now names both.
- **Not acted on (judgement calls):**
  - The repeated `examples.put(...)` in `OpenApiConfig` (Shotgun Surgery). Each example needs its own exception's message, so a loop would need a map from `ProblemType` to exception. That's not worth it for three lines. `ApiDocumentationTest` catches a missing example.
  - `Operation.POWER` reading `Calculation.MAX_SCALE`. BR-6 is defined as "the BR-1 bounds", so sharing one constant is correct.
  - POWER strips trailing zeros for BR-6, and `Calculation.result()` strips again for BR-4. BR-6 is defined on the normalised value; the second strip costs nothing.
  - Operation still has no arity. That is item 7.
- **`/code-review` spec (scope):** the backlog also marks 5a Done (#7). This is the process in CLAUDE.md (status updates when the PR merges), so kept.

## Surprises

- I got two expected values wrong in the first draft of the tests. `1.000…1^999` rounds to `1 + 1E-31`, not to a value with trailing zeros, because the Result is stripped (BR-4). And I had written `10^-999` in a roundabout way. I fixed both before the first run, by working the digits through by hand.
- My first "rounds once" case proved less than I thought. It shows that rounding at every step is wrong, but Java's `pow(n, mc)` uses extra working digits and gets that case right. Only a brute-force probe found a case that tells the two apart.

## Shortcuts taken

None.

## Lessons

- For a test that guards a design choice ("exact, then round once"), break the code in exactly the way the test is meant to catch, and watch it fail. A test that passes against the alternative proves nothing.
- The docs already described the finished feature, but their side claims (safety line, success criteria, the "adding an operation" rule) still drifted. Grep the docs for the old count ("four") and for the rule you are extending.
