# Structural Validation Flow

This diagram documents structural/input validation only. It does not represent clinical validation, therapeutic targets, or a completed Vancomycin calculation engine.

```mermaid
flowchart TD
    A[Raw calculation input and patient parameters] --> B[Safely parse supplied numeric text]
    B --> C{Malformed or non-finite?}
    C -->|Yes| E[Add ERROR issue]
    C -->|No| D[Build or continue with typed TdmInput]

    D --> P[Check non-blank Age and Weight as finite numeric text]
    P --> Q{Invalid patient numeric field?}
    Q -->|Yes| E
    Q -->|No| F[Read WorkflowType.requirements()]

    F --> G[Check required PRE / POST / sampling / additional timing fields]
    G --> H{Required value missing?}
    H -->|Yes| E
    H -->|No| I[Check supplied workflow-specific fields]

    I --> J{Value supplied although workflow does not require it?}
    J -->|Yes| K[Add REVIEW issue]
    J -->|No| L[No issue]

    E --> M{Any ERROR issues?}
    K --> M
    L --> M

    M -->|Yes| N[Return ValidationResult<br/>canProceed = false]
    M -->|No| O[Return ValidationResult<br/>canProceed = true<br/>validatedInput available]
    O --> R[Eligible for future approved calculation stage]
```

## Rules represented

- Required workflow fields come from `WorkflowType.requirements()`.
- Numeric text is parsed with safe conversion; invalid text does not throw an application exception.
- Non-blank patient `age` and `weight` values must be finite numeric text, but no clinical ranges or thresholds are applied.
- Non-finite values are blocking errors.
- Missing workflow-required values are blocking errors.
- Unexpected workflow-specific values are review items and do not block by themselves.
- No clinical concentration ranges, therapeutic targets, age ranges, body-weight ranges, dose ranges, AUC targets, or pharmacokinetic equations are checked.
- Formula-specific domain checks such as division-by-zero or logarithm constraints are deferred until an approved formula explicitly establishes where those operations are used.
