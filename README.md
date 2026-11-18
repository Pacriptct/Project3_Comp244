# COMP 240 Project 3 — BankApp

Swing banking application. The GUI (`BankApp`) is provided; business rules are implemented in `BankController` and the storage classes. See `docs/CODE_TASK_INDEX.md` for where each `TODO Txx` task lives.

## Layout

```
src/bank/   Java sources (package `bank`)
data/       Saved bank state (bank-state.csv)
docs/       Project guides, task index, lab slides
```

## Build & run

Run from the repository root (the app reads/writes `data/` relative to the working directory):

```bash
javac -d out src/bank/*.java
java -cp out bank.BankApp
```
