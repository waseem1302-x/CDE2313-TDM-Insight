# Application Flow Diagram

This diagram reflects the screen/state flow represented by `AppScreen` and `TdmFlowState`. The full navigation host is not yet connected in `MainActivity`.

```mermaid
flowchart TD
    H[Home] -->|Start New Case| P[Patient / Case Input]
    P -->|Next| W[Workflow Selection]
    W -->|Select PRE / POST / PRE_POST| C[Calculation Input]
    C -->|Review| R[Review]
    R -->|Calculate after validation and approved engine| O[Result]

    H -.->|View Academic Disclaimer| D[Disclaimer]
    O -.->|View Academic Disclaimer| D

    P -->|Back| H
    W -->|Back| P
    C -->|Back| W
    R -->|Back / Edit| C
    O -->|Back to Review| R
    O -->|New Case| H
    D -->|Back| X[Calling screen]
```

## Notes

- The primary case sequence is `Home → Patient Input → Workflow Selection → Calculation Input → Review → Result`.
- Disclaimer access exists in the Home and Result screen designs.
- The transition from Review to Result depends on a future approved `TdmCalculator` implementation; this diagram does not claim that a clinical calculation engine currently exists.
- `MainActivity` integration is outside the current Member 3 stage.
