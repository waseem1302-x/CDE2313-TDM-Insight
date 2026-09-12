# TDM Insight — Case Study Analysis

## 1. Project Overview

TDM Insight is a native Android academic prototype for demonstrating the software structure of a Therapeutic Drug Monitoring (TDM) calculator. The project is implemented in Kotlin with Jetpack Compose and Material 3. Its current domain is organised around Vancomycin workflow types while deliberately separating screen presentation, workflow requirements, input state, validation, calculation contracts, and result presentation.

The repository currently contains the main domain models (`TdmInput`, `TdmResult`, and `WorkflowType`), workflow requirements, navigation/state contracts, Compose screens, reusable UI components, structural validation, and unit tests. The calculation contract exists as the `TdmCalculator` interface, but no clinical calculation implementation is provided in this stage.

The project is for university software-development work. It must not be treated as a clinically approved application.

## 2. Problem Context

Therapeutic Drug Monitoring involves using measured drug-related information as part of a wider clinical review process. From a software-engineering perspective, a TDM application must collect the correct information for the selected workflow, handle invalid or incomplete data safely, pass validated data to a calculation component, and present results clearly.

TDM Insight focuses on that software structure. It does not claim that its current screens, models, validation rules, or future calculations are sufficient for clinical use. The application is an academic prototype and should use fictional demonstration cases only.

This project therefore separates two different concerns:

- **Structural validation:** checks whether required workflow data is present and whether numeric values can be represented safely.
- **Clinical validation:** would require approved authoritative clinical sources, therapeutic targets, equations, units, assumptions, and lecturer-approved specifications. Clinical validation is not implemented in this stage.

## 3. Selected Medication / Workflow

The current project is structured around Vancomycin and defines three workflow types in `WorkflowType`:

- `PRE` — Vancomycin Pre
- `POST` — Vancomycin Post
- `PRE_POST` — Vancomycin Pre + Post

Workflow-specific field requirements are centralised in `WorkflowType.requirements()` rather than repeated in individual screens or validators.

The current requirements are:

| Workflow | Pre-dose concentration | Post-dose concentration | Sampling information | Additional timing information |
| --- | --- | --- | --- | --- |
| PRE | Required | Not required | Not required | Not required |
| POST | Not required | Required | Required | Not required |
| PRE_POST | Required | Required | Required | Required |

These are project workflow requirements, not therapeutic ranges or clinical target values. This document intentionally does not introduce Vancomycin equations, target concentrations, AUC targets, pharmacokinetic equations, or other unsupported clinical assumptions.

## 4. User Flow

The repository defines the following case flow through `AppScreen` and `TdmFlowState`:

`Home → Patient / Case Input → Workflow Selection → Calculation Input → Review → Result`

The Academic Disclaimer is also represented as a separate destination and is accessible from the Home and Result screen designs.

The screens currently present the following responsibilities:

1. **Home** — introduces TDM Insight, identifies it as an academic prototype, starts a new fictional case, and provides disclaimer access.
2. **Patient / Case Input** — captures Case ID, Age, Weight, and optional case notes.
3. **Workflow Selection** — allows selection of PRE, POST, or PRE_POST.
4. **Calculation Input** — displays medication dose, dosing interval, an optional laboratory note, and workflow-dependent concentration/timing fields.
5. **Review** — lists the populated `TdmInput` values before calculation.
6. **Result** — is designed to present intermediate values, pharmacokinetic parameters, final values, and calculation explanation supplied by a calculation engine.
7. **Disclaimer** — states the academic and non-clinical limitations of the application.

The screen and state contracts exist, but the full application flow is not yet integrated into `MainActivity`. At the current repository stage, `MainActivity` still contains the starter greeting UI. Full navigation integration is intentionally outside this Member 3 stage.

## 5. Functional Requirements

The current project supports or defines the following functions:

- represent PRE, POST, and PRE_POST workflows;
- derive workflow-specific field requirements from one shared contract;
- collect fictional patient/case information through Compose UI;
- present only the concentration/timing fields relevant to the selected workflow;
- represent calculation input through `TdmInput`;
- structurally validate workflow-required values before a future calculation stage;
- safely parse raw numeric calculation-entry values and structurally validate supplied numeric patient fields such as Age and Weight;
- distinguish blocking validation errors from non-blocking review items;
- review populated inputs before calculation;
- represent future calculation output through `TdmResult`;
- display result categories and calculation explanation when a valid result is supplied;
- provide academic disclaimer content.

The repository does **not** currently provide a clinical Vancomycin calculation implementation.

## 6. Non-Functional Requirements

### Usability

The Compose screens use clear screen titles, labelled fields, workflow-specific field presentation, Back/Next-style actions, and explanatory text. Fields that are irrelevant to the selected workflow are not displayed on the Calculation Input screen.

### Maintainability

Workflow rules are centralised in `WorkflowType.requirements()`. Validation is implemented in its own package instead of inside Composables. This reduces duplication and keeps changes to workflow requirements easier to review.

### Safe handling of invalid inputs

The structural validation layer returns validation issues instead of relying on unsafe numeric parsing. Malformed or non-finite numeric values are blocking errors, including non-blank Age and Weight values that cannot be represented as finite numbers. Missing workflow-required values are also blocking errors.

### Separation of concerns

UI presentation, input models, validation, calculation contracts, result models, and result UI are kept conceptually separate. The validation layer does not calculate clinical values and the result screen does not invent results.

### Native/offline-oriented structure

The current project is a native Android application and does not introduce backend, database, or network architecture. Current case data is represented in local in-memory models/state contracts.

### Testability

Workflow requirements, flow state, screen-presentation helpers, and structural validation are covered by local unit-test classes. The validation classes contain no Compose dependency, allowing them to be tested as ordinary Kotlin logic.

## 7. Application Architecture

The intended data path is:

`UI → Input State → Validation → TDM Calculation Engine → Result Model → Results UI`

### UI

Compose screens collect and present values. Workflow-specific field visibility is derived from the shared workflow requirements.

### Input State

Raw text entry can be represented by `TdmInputDraft` in the validation layer. Once successfully parsed and structurally validated, it can produce a typed `TdmInput`.

### Validation

`TdmInputValidator` checks input structure and numeric safety, including finite numeric representation for non-blank Age and Weight patient parameters. It does not contain clinical equations or therapeutic targets.

### TDM Calculation Engine

`TdmCalculator` currently exists only as an interface:

`calculate(input: TdmInput): TdmResult`

A clinically meaningful implementation remains pending until an approved calculation specification and authoritative source are available.

### Result Model

`TdmResult` separates intermediate values, pharmacokinetic parameters, final values, and explanatory calculation steps.

### Results UI

`ResultScreen` renders a supplied `TdmResult`. It explicitly states that the screen itself does not calculate or invent clinical results.

## 8. Validation Strategy

The Member 3 validation layer is located under:

`com.example.tdminsight.validation`

It contains a small set of focused types:

- `ValidationSeverity` — currently distinguishes `ERROR` and `REVIEW`;
- `ValidationIssue` — identifies the field, message, and severity;
- `ValidationResult` — exposes all issues, blocking errors, review items, whether processing can proceed, and a typed validated input when available;
- `TdmInputDraft` — represents raw calculation-entry strings before parsing;
- `TdmInputValidator` — performs parsing and structural checks outside Compose.

### Required-field validation

Required PRE, POST, sampling, and additional-timing values are determined by `WorkflowType.requirements()`. The validator does not maintain a second manually duplicated PRE/POST/PRE_POST rules table.

### Numeric validation

For numeric calculation-entry values supplied as text, parsing uses safe conversion. Blank optional values remain absent. Non-numeric or non-finite values produce blocking errors rather than exceptions.

The patient parameters `age` and `weight` remain strings in `TdmInput`, but when either value is non-blank the validator checks that it can be safely parsed as a finite number. This is structural numeric validation only; no age or weight ranges or clinical thresholds are imposed.

Typed numeric values supplied directly through `TdmInput` are also checked for finiteness.

### Structural workflow validation

A workflow-required value that is absent produces an `ERROR`. A workflow-specific value supplied when the selected workflow does not require it produces a `REVIEW` issue. Review issues do not block progression by themselves.

No concentration range, therapeutic target, body-weight range, dose range, AUC range, or other clinical threshold is implemented.

No division-by-zero or logarithm-domain rule is added because the current repository does not contain an approved calculation formula that establishes which values will be used in such mathematical operations. Those checks must be introduced together with the approved calculation specification rather than guessed in advance.

## 9. Safety and Academic Limitations

TDM Insight must be understood within the following limitations:

- it is an academic prototype;
- it is not clinically validated;
- it is not prescribing software;
- it is not diagnostic software;
- it is not autonomous treatment-decision software;
- fictional cases should be used for demonstrations;
- structural validation does not prove clinical correctness;
- clinical equations, therapeutic values, units, assumptions, targets, and reference ranges require approved authoritative sources before implementation;
- the current `TdmCalculator` is an interface, not a clinically complete engine;
- result presentation does not imply that valid clinical calculations currently exist.

## 10. Current Limitations / Future Work

The current repository has intentionally incomplete areas that belong to later stages:

- implement the approved Vancomycin calculation engine only after a lecturer-approved specification/source is available;
- add formula-specific mathematical domain checks only when the approved equations make those constraints explicit;
- connect structural validation to the application flow without moving validation logic into Composables;
- integrate the existing screens and state flow into `MainActivity` or the selected navigation structure in a later stage;
- add clinically sourced units, assumptions, targets, and reference values only when formally approved;
- perform broader UI/instrumented testing after screen integration;
- perform any clinical verification or validation only under the appropriate academic/clinical supervision and evidence requirements.

This stage deliberately stops at structural validation, unit tests, and repository documentation. It does not attempt to make the application clinically operational.
