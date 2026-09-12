# TDM Insight App Wireframes

These wireframes document the current Compose screen designs in the repository. They are editable text diagrams, not emulator screenshots and not fabricated screenshots. The complete navigation flow has not yet been wired into `MainActivity`; the diagrams map the existing screen Composables and `TdmFlowState` contract.

## 1. Home

**Screen:** `HomeScreen`

```text
+--------------------------------------------------+
| TDM Insight                                      |
| Native Android therapeutic drug monitoring      |
| calculator prototype for Vancomycin workflows.  |
|                                                  |
| +----------------------------------------------+ |
| | Academic prototype                           | |
| | Use fictional cases only. Clinical equations| |
| | and reference values require approved       | |
| | authoritative sources.                      | |
| +----------------------------------------------+ |
|                                                  |
| [ Start New Case                              ] |
| [ View Academic Disclaimer                    ] |
+--------------------------------------------------+
```

**Primary content**

- application name and short description;
- academic-prototype notice;
- fictional-case safety reminder.

**Actions / destinations**

- `Start New Case` → Patient Input;
- `View Academic Disclaimer` → Disclaimer.

---

## 2. Patient / Case Input

**Screen:** `PatientInputScreen`

```text
+--------------------------------------------------+
| Patient / Case Details                           |
| Enter fictional case information for the        |
| academic demonstration.                          |
|                                                  |
| Case ID        [____________________________]    |
| Age            [____________________________]    |
| Weight         [____________________________]    |
|   Use the unit defined by the approved project  |
|   specification.                                 |
| Case notes     [____________________________]    |
| (optional)     [____________________________]    |
|                                                  |
| [ Back ]                         [ Next ]         |
+--------------------------------------------------+
```

**Fields/components**

- Case ID;
- Age;
- Weight;
- optional case notes.

**Actions / destinations**

- `Back` → previous screen/Home as controlled by the future navigation host;
- `Next` → Workflow Selection according to `TdmFlowState`.

---

## 3. Workflow Selection

**Screen:** `WorkflowSelectionScreen`

```text
+--------------------------------------------------+
| Select Vancomycin Workflow                       |
| Choose the workflow that matches the fictional   |
| case. The next screen shows only relevant fields.|
|                                                  |
| +----------------------------------------------+ |
| | Vancomycin Pre                              | |
| +----------------------------------------------+ |
| +----------------------------------------------+ |
| | Vancomycin Post                             | |
| +----------------------------------------------+ |
| +----------------------------------------------+ |
| | Vancomycin Pre + Post                       | |
| +----------------------------------------------+ |
|                                                  |
| [ Back                                        ] |
+--------------------------------------------------+
```

**Components**

- one reusable `WorkflowCard` for each `WorkflowType` entry;
- selected-workflow state.

**Actions / destinations**

- selecting `PRE`, `POST`, or `PRE_POST` → Calculation Input under the state contract;
- `Back` → Patient Input.

---

## 4. Calculation Input

**Screen:** `CalculationInputScreen`

The field list is dynamic. `calculationFieldsFor(workflow)` reads `WorkflowType.requirements()` so PRE / POST / PRE_POST presentation is not duplicated manually across the UI.

### Common layout

```text
+--------------------------------------------------+
| <Selected workflow display name>                 |
| Enter only the values needed for this workflow.  |
|                                                  |
| Medication dose           [_________________]    |
| Dosing interval           [_________________]    |
| <workflow-dependent fields appear here>          |
| Laboratory note (optional)[_________________]    |
|                           [_________________]    |
|                                                  |
| [ Back ]                       [ Review ]         |
+--------------------------------------------------+
```

### PRE dynamic fields

```text
Medication dose
Dosing interval
Pre-dose concentration
Laboratory note (optional)
```

POST concentration, sampling time, and additional timing information are not shown.

### POST dynamic fields

```text
Medication dose
Dosing interval
Post-dose concentration
Sampling time
Laboratory note (optional)
```

PRE concentration and additional timing information are not shown.

### PRE_POST dynamic fields

```text
Medication dose
Dosing interval
Pre-dose concentration
Post-dose concentration
Sampling time
Additional timing information
Laboratory note (optional)
```

**Actions / destinations**

- `Back` → Workflow Selection;
- `Review` → Review after input-state handling/structural validation is integrated by a later application-flow stage.

**Validation boundary**

The screen itself does not contain the structural validation rules. Raw calculation values can be represented by `TdmInputDraft` and passed to `TdmInputValidator` outside the Composable.

---

## 5. Review

**Screen:** `ReviewScreen`

```text
+--------------------------------------------------+
| Review Calculation Inputs                        |
| Check fictional case details before calculation. |
| Empty or unused workflow fields are hidden.      |
|                                                  |
| +----------------------------------------------+ |
| | Workflow                                     | |
| | <selected workflow>                          | |
| +----------------------------------------------+ |
| +----------------------------------------------+ |
| | <populated input label>                      | |
| | <value>                                      | |
| +----------------------------------------------+ |
| ... one card per populated review item ...       |
|                                                  |
| [ Back / Edit ]                [ Calculate ]     |
+--------------------------------------------------+
```

**Primary content**

- workflow;
- populated patient parameters;
- populated dose/interval/concentration values;
- populated sampling information;
- populated laboratory information.

**Actions / destinations**

- `Back / Edit` → Calculation Input;
- `Calculate` → future approved `TdmCalculator` implementation, then Result. The current repository does not contain that clinical implementation.

---

## 6. Result

**Screen:** `ResultScreen`

```text
+--------------------------------------------------+
| TDM Results                                      |
| Academic prototype output. Values must come from |
| the approved calculation engine.                 |
|                                                  |
| +----------------------------------------------+ |
| | Intermediate Values                          | |
| | <values supplied by TdmResult>               | |
| +----------------------------------------------+ |
| +----------------------------------------------+ |
| | Pharmacokinetic Parameters                   | |
| | <values supplied by TdmResult>               | |
| +----------------------------------------------+ |
| +----------------------------------------------+ |
| | Final Values                                 | |
| | <values supplied by TdmResult>               | |
| +----------------------------------------------+ |
|                                                  |
| Calculation Explanation                          |
| <ordered calculation steps if supplied>          |
|                                                  |
| [ Back to Review ]             [ New Case ]      |
| [ View Academic Disclaimer                    ]  |
+--------------------------------------------------+
```

**Primary content**

- intermediate values;
- pharmacokinetic parameters;
- final values;
- calculation explanation.

All values are rendered from `TdmResult`; the screen does not perform clinical calculations.

**Actions / destinations**

- `Back to Review` → Review;
- `New Case` → reset/start flow;
- `View Academic Disclaimer` → Disclaimer.

---

## 7. Disclaimer

**Screen:** `DisclaimerScreen`

```text
+--------------------------------------------------+
| Academic Disclaimer                              |
|                                                  |
| +----------------------------------------------+ |
| | TDM Insight is an academic prototype.        | |
| | • Not clinically validated                   | |
| | • Not prescribing software                   | |
| | • Not diagnostic software                    | |
| | • Not autonomous treatment-decision software | |
| | • Use fictional demonstration cases only     | |
| | • Clinical equations/targets/units require   | |
| |   lecturer-approved authoritative sources    | |
| +----------------------------------------------+ |
|                                                  |
| [ Back                                        ] |
+--------------------------------------------------+
```

**Actions / destinations**

- `Back` → the caller-controlled previous screen.

---

## Screen-to-Contract Mapping

| Screen | Main model/contract used |
| --- | --- |
| Home | callback-based navigation entry points |
| Patient Input | `patientParameters` map callbacks |
| Workflow Selection | `WorkflowType` |
| Calculation Input | `WorkflowType.requirements()` through `calculationFieldsFor()` |
| Review | `TdmInput` and `buildReviewItems()` |
| Result | `TdmResult` |
| Disclaimer | static academic-safety content |

The wireframes intentionally document the current repository only. They do not imply that the complete screen sequence is already connected to `MainActivity` or that a Vancomycin calculation engine has been implemented.
