# 2. Exact operations: add, subtract, multiply — retrospective

## Caught by review

- **`/code-review` spec:** `{"operation": 123}` returned `unknown-operation` instead of `invalid-request`, because Jackson coerces scalars to `String` by default. Fixed: `config.JacksonConfig` makes `String` targets reject integer, float and boolean input; `rejectsNonStringOperation` failed before the fix.
- **`/code-review` standards:** framework errors and 500s use `type: about:blank`, which the PRD's type table did not list (STANDARDS §4). Fixed: PRD §3.2 table row and change-log entry.
- **`/code-review` standards:** BR-3 ("ADD, SUBTRACT, MULTIPLY are exact") not cited (STANDARDS §3). Fixed: cited on `Operation` and in the two exactness test names.
- **`/code-review` standards:** three javadocs restated the code (STANDARDS §5). Fixed: removed.
- **`/code-review` standards:** fully qualified `org.hamcrest.Matchers.containsString` in a test. Fixed: imported.
- **Not acted on (judgement calls):** `PerformCalculation` is a Middle Man (STANDARDS §2 requires the use case); `(operation, a, b)` travels as a data clump (DTOs may not cross the boundary); `unexpected()` sets `about:blank` by hand while `handleExceptionInternal` defaults it (the base-class hook does not run for `@ExceptionHandler` methods); the smoke test rename and the item-1 backlog Done row are small housekeeping.
- **`/ponytail:ponytail-review`:** no findings. It flagged, as not checked, that huge operands make `toPlainString()` build gigabyte strings until item 4 (operand limits).

## Surprises

- A private test helper named `post(String)` shadowed the static `MockMvcRequestBuilders.post`, so the test didn't compile. Cause: same name, more specific overload. Renamed to `calculate`.
- Spring 7 `ProblemDetail.getType()` is `null` by default and the JSON omits `type`; Spring 6 defaulted it to `about:blank`. The PRD requires `type` on every error, so `ProblemDetailsAdvice` sets it. The first attempt set it before calling `super.handleExceptionInternal`, but the base class builds the body inside that call, so the fix sets it on the returned body.

## Shortcuts taken

None.

## Lessons

- Ponytail and the spec reviewer split again: ponytail found nothing, the spec reviewer found the only real bug (scalar coercion). For any field parsed from JSON, test a wrong JSON *type* (number, boolean, object), not just a wrong value.
- When a framework major version changes (Spring 6 → 7, Jackson 2 → 3), check defaults the PRD depends on with a test rather than assuming.
