# 7. Unary operations (sqrt, negate) — retrospective

## Caught by review

- **`/code-review` spec:** the OpenAPI schema typed request `b` as `number | string`, but the server accepts `"b": null` for a unary Operation, so a client that validates against the schema would reject a valid request. Fixed: `b` is now `number | string | null`, asserted in `documentsOperandsAsNumberOrString`.
- **`/code-review` spec (call out, no code change):** two binary requests behave differently now.
  - `{"operation":"NOT_AN_OPERATION","a":1}` used to return `invalid-request` (missing `b`). It now returns `unknown-operation`, because PRD §3.2 orders unknown operation before operand count. The test `reportsUnknownOperationBeforeOperandCount_BR9` shows this.
  - A binary request with no `b` keeps its 400 `invalid-request`, but the detail now names the Operation.
  - Both are listed in the PR description.
- **`/code-review` standards:**
  - The example key `invalid-request-operand-count` didn't match the glossary term Arity. Renamed to `invalid-request-arity`.
  - "incl." in the 400 description is now "including".
  - `NegativeSquareRootException` now cites BR-8.
  - Three tests now cite their BR id.
- **`/code-review` standards:** `rejectsABinaryCalculationWithoutB_BR9` listed the binary operations by hand, so a new binary Operation would be missed. Fixed: `@EnumSource` that excludes the two unary constants, so a new Operation is tested as binary by default.
- **Not acted on (judgement calls):**
  - `arity() == 1` appears in both `Calculation` and `ArityMismatchException`. An `isUnary()` method would add API to save two comparisons.
  - Unary constants ignore `b` in `apply(a, b)`. DOMAIN-MODEL §2 documents this.
  - `b` is null when absent. The domain docs choose null for this.
  - STANDARDS §3 says "nothing else should change", yet this item touched the request, response and controller. That is the one-off cost of adding arity, explained in the PR.
- **`/ponytail:ponytail-review`:** nothing to fix. It cross-checked `BigDecimal.sqrt(DECIMAL128)` against Python's `decimal` at 34 digits HALF_EVEN on 3,000 random operands: no mismatches.

## Surprises

- My first expected value for √10 was wrong in the last 18 digits; I had written it from memory. I checked it with Python's `decimal` before the first run. Work out expected digits with a tool, never from memory.
- springdoc marks no response properties as required, so my first test that the response `b` is optional failed with "path not found". Fixed with `@Schema(requiredProperties = {"operation", "a", "result"})` on `CalculationResponse`. That documents the real contract: `b` is the only field that can be absent.
- Two Python test edits stopped on their asserts, because Spotless had re-wrapped the lines they matched. The asserts stopped the script before it wrote anything, so no file was half-edited. This is the same lesson as in retro 5: match on small fragments.

## Shortcuts taken

None.

## Lessons

- When a field becomes optional, follow it through every place it appears: the request schema (`required`, and `null` in its types), the response (`@JsonInclude`, `requiredProperties`), the default detail message, and the error-order tests that used to rely on it being missing.
- When a change is "non-breaking", diff the old and new error type and detail for the requests old clients actually send, and list every difference in the PR, even the ones the docs allow.
