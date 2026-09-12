# Calculation Specification Gate

## Specification Status

**BLOCKED**

Stage 3 cannot produce an implementation-ready clinical calculation specification because no lecturer/project-approved clinical calculation source was provided in this chat and no approved clinical source file is present in the current repository baseline.

Per the Stage 3 source rule, no clinical equation, variable definition, clinical unit, constant, therapeutic target, dosing recommendation, or other clinical meaning may be reconstructed from memory or substituted from general internet material.

## Repository Baseline Reviewed

Baseline: `main` at `48e48b61fdd919b916dc6bdc6287c8684828a108`.

The existing repository already provides the software contracts needed to receive a future approved specification:

- `WorkflowType` defines `PRE`, `POST`, and `PRE_POST`.
- `WorkflowRequirements` defines current project-level field presence requirements for those workflows.
- `TdmInput` can hold patient parameters, medication dose, dosing interval, pre/post concentrations, sampling information, and laboratory information, but it does not establish authoritative clinical units or meanings for those values.
- `TdmResult` can represent intermediate values, pharmacokinetic parameters, final values, and explanatory calculation steps.
- `CalculationInputScreen` deliberately tells the user to use units and timing definitions from the approved project specification.
- `TdmInputValidator` currently performs structural validation only: safe numeric parsing, finite-value checks, and workflow-required field presence/review checks.
- `TdmCalculator` remains an interface only; no clinical calculation implementation is present.

These existing project structures do **not** constitute an approved clinical calculation source.

## Blocked Specification Items

Until an approved source is supplied and reviewed page-by-page, the following remain unresolved and must not be guessed:

- which of `PRE`, `POST`, and `PRE_POST` are clinically calculable;
- exact required inputs and their units;
- exact equations and variable definitions;
- timing definitions and sampling relationships;
- constants and source-stated assumptions;
- mathematical preconditions implied by approved equations;
- calculation dependency order;
- source-supported outputs and output units;
- mapping of those outputs to `TdmResult`;
- any model/UI changes required by source-backed variables;
- any formula-domain or lecturer-approved clinical validation rules.

## Required Evidence to Unblock Stage 3

Provide the lecturer/project-approved calculation source (for example, an approved PDF, guideline, paper, clinical reference, or course material). The source must contain enough information to identify the required equations, variables, units, assumptions, workflow applicability, and source locations/pages without inference.

After that source is provided, Stage 3 can be reopened to create the full calculation specification, source traceability matrix, calculation flow, units matrix, compatibility assessment, assumptions register, and implementation-readiness checklist.

No clinical formula or calculation implementation is added by this blocked-gate document.
