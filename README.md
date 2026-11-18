# COMP 240 Project 3 — BankApp

Swing banking application. The GUI (`BankApp`) is provided; business rules are implemented in `BankController` and the storage classes. See `docs/CODE_TASK_INDEX.md` for where each `TODO Txx` task lives.

## Layout

```
src/bank/   Java sources (package `bank`)
test/bank/  Tests (T16), plain Java, no libraries needed
data/       Saved bank state (bank-state.csv)
docs/       Project guides, task index, lab slides
```

## Build & run

Run from the repository root (the app reads/writes `data/` relative to the working directory):

```bash
javac -d out src/bank/*.java
java -cp out bank.BankApp
```

## Tests

```bash
javac -d out src/bank/*.java test/bank/*.java
java -cp out bank.BankTests
```

Tests use temporary folders, so they never touch `data/`.

## Design notes

| Topic | Decision |
|---|---|
| Duplicate customers (T01) | The SSN identifies a person; a second signup with the same SSN is rejected. |
| Signup rules (T01) | Names: letters, spaces, `-` and `'`. SSN: 9 digits. Age 18 to 120. Phone: 10 digits. |
| Account numbers (T02) | Counter saved with the state; never reused, even after undo or restart. |
| Account rules (T03/T04) | `BankAccount` is abstract. Checking: no overdraft. Savings: no overdraft, max $1,000 per withdrawal/transfer. |
| Amounts (T05/T06) | Positive, at most 2 decimals, max $1,000,000 per operation. |
| Transfers (T07) | Only between the same customer's checking and savings, either direction. |
| History (T09) | `LinkedList` in time order; read back through a `Queue`. |
| Sorted history (T10) | Largest amount first, then time, then ID. |
| Undo (T11) | Stack of state snapshots taken before each change. Lasts for the current run only. |
| Saving (T12) | Every successful change is saved immediately. If the save fails, the change is cancelled. |
| Files (T13/T14) | Written to a `.tmp` file first, then swapped in, so a crash can't corrupt the save. |
| Damaged files (T15) | The app refuses to start and explains which record is bad, instead of overwriting data. |
