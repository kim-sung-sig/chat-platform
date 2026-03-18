Implement a focused, minimal change.

Task: $ARGUMENTS

## Instructions

1. Read `docs/conventions/CONVENTIONS.md` before making changes.
2. Change only what is described in the task — do not refactor surrounding code.
3. Identify the exact files to modify; prefer editing over creating new files.
4. After the change, run the relevant module tests to verify nothing is broken:
   `./gradlew :<module>:test --tests "<TestClass>"`
5. If the change touches a domain model or application service, add or update the corresponding unit test.
6. Summarize what was changed and why.
