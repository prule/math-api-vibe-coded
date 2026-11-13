# 3. Divide — retrospective

## Caught by review

- **`/code-review` spec:** the web test sent `"0.00"` and `-0` but never a plain `0`. Fixed: added `"b":0` to the 422 web test.
- **`/code-review` standards:** the web 422 test enforced BR-2 without citing it (STANDARDS §3). Fixed: renamed `rejectsDivisionByZeroAsUnprocessable_BR2`.
- **`/code-review` standards:** "Quotient" in a test name is not a glossary term (DOMAIN-MODEL §1). Fixed: `dividesExactlyWhenTheResultTerminates`.
- **`/code-review` spec:** changes outside "a constant, an exception and its mapping" must be flagged in the PR. Done in the PR description: the unknown-operation test's expected list gained `DIVIDE`, and the backlog marks item 2 Done.
- **Not acted on (judgement call):** each domain exception gets its own near-identical `@ExceptionHandler` in `ProblemDetailsAdvice`. With two handlers, a shared table is not worth it yet; revisit when item 6 adds more domain exceptions.
- **`/ponytail:ponytail-review`:** no findings.

## Surprises

None. Main code needed exactly the new `Operation` constant, `DivisionByZeroException` and its handler, as the backlog item predicted.

## Shortcuts taken

None.

## Lessons

- When a test covers several input forms in the domain, give the web test at least the plain form too; the HTTP path parses numbers and strings differently.
- Name tests with glossary words; reviewers check test names against DOMAIN-MODEL §1, not just production code.
