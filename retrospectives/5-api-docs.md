# 5. OpenAPI documentation — retrospective

## Caught by review

- **`/code-review` standards (hard):** `@Schema(example = "DIVIDE")` copied a domain value into an annotation (STANDARDS §4). Fixed: the example is set from `Operation.DIVIDE` in `OpenApiConfig`.
- **`/code-review` standards + spec:** 406, 415 and 500 (all `about:blank`) were returned but not documented; §4 wants an `@ApiResponse` per status. Fixed: three more responses with examples, asserted in `documentsEveryResponseStatusWithItsProblemExamples`.
- **`/code-review` spec:** invalid-request has two real details; only the malformed-JSON one had an example. Fixed: `invalid-request-missing-field` example.
- **`/code-review` spec:** springdoc's generated `ProblemDetail` schema has a `properties` field real responses never contain. Fixed: a hand-written `Problem` schema (type, title, status, detail, instance); test asserts its exact fields and that `ProblemDetail` is gone.
- **`/code-review` spec:** examples omitted `instance`. Fixed: every example carries it.
- **`/ponytail:ponytail-review` + both `/code-review` axes:** the invalid-request detail, the titles and the `/problems/` prefix were copied between `ProblemDetailsAdvice` and `OpenApiConfig`. Fixed at the root: new `ProblemType` enum (status, slug, title, the invalid-request and 500 details) read by both; and `documentedExamplesMatchRealResponses` posts real requests and asserts each body equals its documented example.
- **`/code-review` standards:** `Arrays.stream(Operation.values()).map(Operation::name)` repeated. Fixed: `Operation.names()`.
- **`/code-review` standards:** STANDARDS §6 says one `@SpringBootTest`; `ApiDocumentationTest` is a second (springdoc isn't in the web slice). Fixed: §6 table row and change-log entry.
- **Not acted on (judgement calls):** the schema lookup chain in `OpenApiConfig` (a rename fails `ApiDocumentationTest` loudly); the repeated `@Schema` on `a` and `b` (two lines).

## Surprises

- A test run after a failed compile showed stale results from the previous run, which looked like real failures (`a.type was "number"`). Cause: I read old XML reports. Always check the build compiled before reading test results.
- A Python edit failed because Spotless had re-wrapped the line it matched; the script aborted before writing. Harmless, but edits against formatted code should match smaller fragments.
- Spring's MockMvc JsonPath doesn't support the `~` (keys) operator; read the JSON with `JsonPath` and assert in AssertJ instead.
- The 415 and 406 example details are Spring's own text, copied from a live response. If Spring rewords them the examples go stale; they're not covered by the real-response test because the exact text depends on the request.

## Shortcuts taken

None.

## Lessons

- For docs, test that examples equal real responses — that catches drift no matter where values come from.
- When a standard says "each status it returns", list the statuses the code can actually return (including framework ones) before annotating.
- Three reviewers converged on the same duplication; fixing it at the root (one catalogue) closed four findings at once.
