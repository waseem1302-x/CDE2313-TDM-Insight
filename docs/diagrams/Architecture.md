# Architecture / Data-Flow Diagram

The architecture keeps presentation, structural validation, calculation contracts, and results separate. The calculator is deliberately shown as an interface/pending implementation because no approved clinical engine exists in the current repository.

```mermaid
flowchart LR
    UI[Compose UI Screens]
    STATE[Input State / TdmInputDraft]
    VALIDATION[Structural Validation<br/>TdmInputValidator]
    INPUT[TdmInput]
    CALC[TdmCalculator Interface<br/>Approved implementation pending]
    RESULT[TdmResult]
    RESULTUI[ResultScreen]

    UI --> STATE
    STATE --> VALIDATION
    VALIDATION -->|No blocking errors| INPUT
    VALIDATION -->|Errors / review issues| UI
    INPUT --> CALC
    CALC --> RESULT
    RESULT --> RESULTUI

    REQ[WorkflowType.requirements()] --> UI
    REQ --> VALIDATION
```

## Separation of concerns

- **Compose UI** displays fields and collects text; clinical/structural rules are not embedded in Composables.
- **Input state** preserves raw values before safe parsing.
- **Structural validation** checks workflow-required presence, numeric parsing, and finiteness.
- **`TdmInput`** is the typed input model eligible to be passed forward when no blocking validation errors exist.
- **`TdmCalculator`** is currently only an interface. An approved clinical implementation is future work.
- **`TdmResult`** is the result contract consumed by `ResultScreen`.

No backend, database, network service, dependency-injection framework, or clinical formula layer is introduced by this stage.
