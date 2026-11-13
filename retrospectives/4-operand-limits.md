# 4. Operand limits — retrospective

## Caught by review

- **`/code-review` spec:** a field name over 1,000 characters returned 200; PRD §3.2 caps every JSON string. Cause: Jackson's `maxNameLength` (default 50,000) is separate from `maxStringLength`. Fixed: `.maxNameLength(1_000)`, test `rejectsFieldNameOverOneThousandCharactersAsInvalid`.
- **`/code-review` spec:** `maxDocumentLength` counts bytes, not characters, so a non-ASCII body under 16,384 characters can be rejected. Fixed by amending PRD §3.2 to "16,384 bytes (UTF-8)" with a change-log entry, plus test `limitsBodyInUtf8BytesNotCharacters`. Counting characters would mean buffering and decoding the whole body ourselves.
- **`/code-review` standards + spec:** `OperandOutOfRangeException` repeated "100" and "±1000" as literals. Fixed: message built from `Calculation.MAX_PRECISION` / `MAX_SCALE`.
- **`/code-review` standards:** the exception's constructor parameter `operand` held the name, not the Operand. Fixed: `operandName`.
- **`/code-review` standards:** the 1,000 cap appears in both `JacksonConfig` and `OperandDeserializer`. Fixed partly: a comment says the deserializer check is for numbers (strings are capped by the parser). Not merged into one constant: `config` and the adapter are different packages and the deserializer is package-private.
- **Not acted on (judgement calls):** operand names as bare `"a"`/`"b"` strings (revisit with unary operations, item 7); `reportsUnknownOperationBeforeOperandOutOfRange` cites no BR (the order is PRD §3.2, not a BR); the deleted `CalculationRequest` javadoc (now said by `OperandDeserializer`).
- **`/ponytail:ponytail-review`:** one nice-to-have, not acted on: an exponent too large for a Java `int` (`"1e3000000000"`) is reported as `invalid-request`, not `operand-out-of-range`. Both are 400; telling overflow from garbage needs our own number parsing. Noted in the PR.

## Surprises

- Jackson's `maxNumberLength` counts digits, not characters: a 1,001-character `0.000…1` has 1,000 digits and passed. Fixed with `OperandDeserializer`, which checks the token's text length before `new BigDecimal`. The first test lumped string and number together; splitting it showed which one leaked.
- I got the byte arithmetic of the UTF-8 test wrong twice (`"é",` is 4 characters, 5 bytes). A direct jackson-core probe settled it.

## Shortcuts taken

None.

## Lessons

- Don't trust a library limit's name: check what unit it counts (digits vs characters, bytes vs characters) with a boundary test at limit+1 for every input shape (string, number, field name).
- Split multi-case "rejects X" tests per input shape from the start, so a failure names the shape.
- Ponytail missed both contract gaps again; the spec reviewer, running the real server with curl, found them. Spec review against the PRD keeps paying for itself.
