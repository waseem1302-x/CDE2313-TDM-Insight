# Calculation Specification Gate

## 1. Specification Status

**READY FOR IMPLEMENTATION — scoped academic calculation engine**

Stage 3 is unblocked for a deliberately limited implementation scope:

- medication: **Vancomycin**;
- route/context: **intermittent intravenous dosing**;
- population for the first implementation: **adult patients**;
- supported workflows: `PRE`, `POST`, `PRE_POST`;
- purpose: academic pharmacokinetic calculation and explainable results only;
- excluded from the first implementation: pediatric-specific equations, continuous infusion, dialysis-specific dosing, unstable renal-function dosing, automated prescribing recommendations, and autonomous dose changes.

The implementation must remain an academic prototype and must not be represented as clinically validated prescribing or treatment-decision software.

The calculation specification below is based on official Ministry of Health Malaysia material and current authoritative Vancomycin monitoring guidance. The public comparison repository `sbr-hkyr/MobileAppProject` was inspected only as an implementation reference; it is **not** treated as a clinical authority.

---

## 2. Approved / Authoritative Sources Used

### Source A — Ministry of Health Malaysia

**Clinical Pharmacokinetics Pharmacy Handbook, Second Edition (2019)**

Prepared by the Clinical Pharmacy Working Committee (Clinical Pharmacokinetics Subspecialty), Pharmacy Practice & Development Division, Ministry of Health Malaysia.

Official publication page:
`https://pharmacy.moh.gov.my/en/documents/clinical-pharmacokinetics-pharmacy-handbook-second-edition.html`

Official PDF reviewed:
`clinical-pharmacokinetics-pharmacy-handbook-ccph-2nd-edition-rev-2.0.pdf`

Primary Vancomycin material reviewed page-by-page in Chapter 15:

| PDF page | Printed chapter page/content | Use in this specification |
| --- | --- | --- |
| 261 | Chapter opening / key parameters | Population `Ke`, population `Vd`, clearance reference, half-life reference |
| 262 | Pharmacokinetic section | Adult `Vd` equation and Vancomycin PK context |
| 263-264 | Indication / therapeutic targets | AUC24 target context and limitations |
| 265-268 | Dosing and renal/dialysis material | Reviewed but excluded from first engine scope |
| 269 | Sampling | Pre/post sampling definitions and timing |
| 270 | Monitoring parameters | Reviewed; no autonomous clinical monitoring logic added |
| 271 | Administration | Infusion timing context |
| 272 | Calculation | PRE and PRE+POST equations, variable definitions |
| 273 | AUC24 calculation | Log and trapezoidal AUC methods and AUC24 definitions |
| 274 | Result evaluation | Reviewed; autonomous recommendations intentionally excluded |
| 275-276 | Worked Vancomycin case | Equation verification / example outputs |
| 277 | References | Source lineage for Chapter 15 |

### Source B — Ministry of Health Malaysia PhIS

**Pharmacy Information System (PhIS) & Clinic Pharmacy System (CPS), User Manual — TDM Calculator, 9th Edition**

Official public PDF reviewed:
`PB_U. MANUAL_TDM CALCULATOR-v2.0.pdf`

Relevant pages:

| PDF page | Manual page/content | Use in this specification |
| --- | --- | --- |
| 6 | Latest Vancomycin calculator enhancement | PRE workflow expected-value behaviour |
| 7 | Vancomycin calculator overview | PRE workflow input set |
| 8 | Vancomycin Post / Pre+Post requirements | POST and PRE_POST timing/input mapping |
| 9 | Pre+Post calculator figure | Workflow confirmation |

The current PhIS portal also lists a newer 13th-edition TDM Calculator manual. The accessible 9th-edition PDF was used here for explicit workflow-field mapping because its Vancomycin pages were directly reviewable during this Stage 3 audit.

### Source C — 2020 Vancomycin Consensus Guideline

**ASHP/PIDS/SIDP/IDSA Revised Consensus Guideline for Therapeutic Monitoring of Vancomycin for Serious MRSA Infections (2020)**

Official IDSA page:
`https://www.idsociety.org/practice-guideline/vancomycin/`

Used only for the current AUC exposure target context: **AUC/MIC 400-600 mg·h/L when MIC is assumed to be 1 mg/L for serious MRSA infections**. The first engine must not convert this into an autonomous treatment recommendation.

---

## 3. Implementation Scope and Assumptions

### Supported in the first calculation engine

1. Adult intermittent-IV Vancomycin.
2. `PRE` workflow using one trough/pre-dose concentration.
3. `POST` workflow using one post-dose concentration plus sampling delay and a population elimination-rate estimate.
4. `PRE_POST` workflow using both measured concentrations and timing for patient-specific elimination-rate estimation.
5. Explainable calculation steps.
6. Numeric pharmacokinetic outputs only.
7. AUC24 calculation for the `PRE_POST` workflow where all required timing data are present.

### Explicitly excluded from the first calculation engine

- pediatric-specific Vancomycin calculation paths;
- dialysis dosing;
- continuous-infusion dosing;
- unstable renal-function dose selection;
- automatic loading-dose selection;
- automatic dose adjustment;
- recommended new dose generation;
- automated treatment advice;
- clinical interpretation of toxicity/efficacy beyond displaying a source-backed reference target;
- unsourced age, weight, concentration, dose, or interval ranges.

### Important source interpretation rule

Where the public comparison project differs from the MOH equations, **the MOH source wins**.

No default/fallback clinical concentration (for example `10`, `12`, `25`, or `28 mg/L`) may be silently inserted when a required measured value is missing.

---

## 4. Workflow Support Matrix

| Workflow | Status | Source-backed calculation approach | Required measured/derived information |
| --- | --- | --- | --- |
| `PRE` | Ready | Single-trough equations from MOH Chapter 15 | Dose, interval, pre concentration, adult Vd inputs |
| `POST` | Ready with population-Ke path | PhIS POST workflow + MOH population `Ke` + post-sample extrapolation | Dose, interval, post concentration, post-sample delay, CrCl, adult Vd inputs |
| `PRE_POST` | Ready | Patient-specific two-point equations from MOH Chapter 15 | Dose, interval, pre concentration, post concentration, post delay, pre-to-post sample-time difference; infusion duration for AUC |

---

## 5. Canonical Input Specification

All numeric values must be finite. Units below are fixed for the first engine and should be shown explicitly in the UI.

| Canonical input | Meaning | Type | Unit | Workflow | Required? | Current project mapping |
| --- | --- | --- | --- | --- | --- | --- |
| `ageYears` | Patient age used for adult Vd equation | Double | years | PRE, POST | Yes for adult population-Vd path | currently `patientParameters["age"]` |
| `bodyWeightKg` | Total body weight | Double | kg | PRE, POST, PRE_POST | Yes | currently `patientParameters["weight"]` |
| `doseMg` | Vancomycin dose | Double | mg | all | Yes | `medicationDose` |
| `dosingIntervalHours` | Nominal dosing interval `T` | Double | h | all | Yes | `dosingInterval` |
| `preDoseConcentrationMgL` | Measured pre-dose / trough concentration | Double | mg/L | PRE, PRE_POST | Required by workflow | `preDoseConcentration` |
| `postDoseConcentrationMgL` | Measured post-dose concentration | Double | mg/L | POST, PRE_POST | Required by workflow | `postDoseConcentration` |
| `postSampleDelayHours` | Time from end of infusion to post sample (`t'`) | Double | h | POST, PRE_POST | Yes | currently generic `samplingTime` |
| `prePostSampleDifferenceHours` | `t2 - t1`, elapsed clock time between the pre and post sample times used in the two-point denominator | Double | h | PRE_POST | Yes | currently generic `additionalTimingInformation` |
| `creatinineClearanceMlMin` | Creatinine clearance used by the MOH population Vancomycin `Ke` equation | Double | mL/min | POST | Yes for population-Ke POST path | **model change required** |
| `infusionDurationHours` | Infusion duration used to determine time from infusion start to post sample for log-method AUC | Double | h | PRE_POST AUC | Yes for AUC24 | **model change required** |

### Why CrCl is a direct calculation input in this specification

The Vancomycin chapter directly provides the population relationship:

`Ke (h^-1) = 0.0044 + (CrCl × 0.00083)`

The first engine should accept a validated `CrCl` value rather than silently choosing a Cockcroft-Gault weight convention or inventing a renal-function rule. If the project later computes CrCl internally from serum creatinine, that should be a separately specified and tested calculation.

---

## 6. Variable Dictionary

| Symbol | Definition | Unit |
| --- | --- | --- |
| `Dose` | Vancomycin dose | mg |
| `T` | Dosing interval | h |
| `Cpre` | Measured pre-dose concentration | mg/L |
| `Cpost` | Measured post-dose concentration | mg/L |
| `Cmin` | Estimated minimum / trough concentration | mg/L |
| `Cmax` | Estimated maximum concentration | mg/L |
| `Ke` | Elimination rate constant | h^-1 |
| `t1/2` | Elimination half-life | h |
| `t'` | Time from end of infusion to post sampling | h |
| `t1` | Pre-sample clock time reference | h or timestamp-derived hour value |
| `t2` | Post-sample clock time reference | h or timestamp-derived hour value |
| `Δsample` | `t2 - t1` | h |
| `BW` | Body weight | kg |
| `Vd` | Volume of distribution | L |
| `Vd/BW` | Weight-normalised volume of distribution | L/kg |
| `CrCl` | Creatinine clearance | mL/min |
| `Co` | Concentration extrapolated to infusion start for the handbook log-AUC method | mg/L |
| `t''` | Time from infusion start to post sampling | h |
| `AUC_interval` | AUC for one dosing interval using the selected method | mg·h/L |
| `AUC24` | 24-hour area under the concentration-time curve | mg·h/L |

---

## 7. Equations

### 7.1 Adult population volume of distribution

MOH Chapter 15 provides an adult equation:

`Vd (L) = 0.17 × Age(years) + 0.22 × TBW(kg) + 15`

Use this equation for the first adult PRE and POST implementation.

The handbook also reports an average Vancomycin Vd of approximately `0.7 L/kg`; this may be displayed as reference information but must not silently replace the selected adult equation.

**Source:** MOH handbook, PDF page 262 (Chapter 15 pharmacokinetic section).

### 7.2 Population elimination-rate constant for POST workflow

`Ke = 0.0044 + (CrCl × 0.00083)`

Output unit: `h^-1`.

**Source:** MOH handbook, PDF page 261, Chapter 15 key parameters.

### 7.3 PRE workflow — only trough/pre level available

For the first adult PRE implementation:

1. `Vd = 0.17 × Age + 0.22 × TBW + 15`
2. `Cmax = Cmin + Dose / Vd`
3. `Ke = [ln(Cmax) - ln(Cmin)] / T`
4. `t1/2 = 0.693 / Ke`

`Cmin` is the measured pre-dose concentration for this workflow.

**Source:** MOH handbook, PDF page 272, "Only trough level available"; adult Vd from PDF page 262. PhIS PDF page 7 confirms the Vancomycin PRE workflow uses dose, interval, pre-level concentration and Vd-related information.

### 7.4 POST workflow — population-Ke extrapolation

The POST workflow is confirmed by the PhIS manual, which requires the post concentration and the duration between infusion completion and post sampling.

For the first implementation:

1. `Vd = 0.17 × Age + 0.22 × TBW + 15`
2. `Ke = 0.0044 + (CrCl × 0.00083)`
3. `Cmax = Cpost × e^(Ke × t')`
4. `Cmin = Cmax × e^(-Ke × T)`
5. `t1/2 = 0.693 / Ke`

This uses the MOH population `Ke` relationship where only one measured post concentration is available and applies the handbook's post-sample extrapolation relationship.

**Sources:** MOH handbook PDF pages 261, 262 and 272; PhIS manual PDF page 8.

### 7.5 PRE_POST workflow — patient-specific two-point elimination

1. `Ke = [ln(Cpost) - ln(Cpre)] / [T - (t2 - t1)]`
2. `t1/2 = 0.693 / Ke`
3. `Cmax = Cpost × e^(Ke × t')`
4. `Cmin = Cmax × e^(-Ke × T)`

For total distribution volume in litres, use the Chapter 15 equation in the same form used by the worked example:

5. `Vd(L) = Dose / [Cmax × (1 - e^(-Ke × T))]`
6. `Vd(L/kg) = Vd(L) / BW`

**Source:** MOH handbook PDF page 272; worked-case verification on PDF pages 275-276.

### 7.6 PRE_POST AUC24 — log method

The handbook lists four AUC24 methods. To keep the first engine deterministic, use the **log method** demonstrated in the worked case.

1. `t'' = infusionDurationHours + postSampleDelayHours`
2. `Co = Cmax × e^(Ke × t'')`
3. `AUC_interval = (Co - Cmin) / Ke`
4. `dosingFrequencyPerDay = 24 / T`
5. `AUC24 = AUC_interval × dosingFrequencyPerDay`

Output unit: `mg·h/L`.

**Source:** MOH handbook PDF page 273; worked case on PDF pages 275-276.

### 7.7 AUC target context

A numeric AUC24 result may be displayed alongside the reference interval **400-600 mg·h/L** where applicable to serious MRSA / S. aureus exposure monitoring.

This is reference information only. The application must not automatically generate a dose recommendation from it in the first calculation-engine stage.

**Sources:** MOH handbook PDF pages 263-264; 2020 ASHP/PIDS/SIDP/IDSA consensus guideline.

---

## 8. Mathematical Preconditions / Formula-Domain Validation

These are mathematical requirements, not invented therapeutic ranges.

### All workflows

- `Dose > 0`
- `T > 0`
- all required values must be finite;
- required concentrations must be `> 0` before using `ln()`;
- `Vd > 0` before any division by Vd;
- `Ke > 0` before calculating half-life.

### PRE

- `Cmin > 0`;
- computed `Cmax > 0`;
- `ln(Cmax) - ln(Cmin)` must produce a positive finite `Ke`.

### POST

- `Cpost > 0`;
- `CrCl` must be finite;
- `t' >= 0`;
- computed `Ke`, `Cmax`, and `Cmin` must be positive and finite.

### PRE_POST

- `Cpre > 0`;
- `Cpost > 0`;
- `Cpost > Cpre` is required for a positive two-point `Ke` under this model;
- denominator `T - (t2 - t1) > 0`;
- `t' >= 0`;
- `1 - e^(-Ke × T) > 0` before Vd division;
- `BW > 0` before calculating `L/kg`;
- AUC log method additionally requires `infusionDurationHours > 0` and a finite `t''`.

No arbitrary clinical age, dose, concentration, weight, or interval ranges should be introduced unless separately approved and sourced.

---

## 9. Output Specification

The existing `TdmResult` model is flexible enough to hold the first engine's outputs.

### Intermediate values

- adult `Vd` estimate where applicable;
- two-point denominator / elapsed elimination interval;
- `t''` for AUC when applicable;
- `Co` for AUC when applicable;
- dosing frequency per day.

### Pharmacokinetic parameters

- `Ke` — `h^-1`;
- `t1/2` — `h`;
- `Vd` — `L`;
- `Vd/BW` — `L/kg` where weight is available;
- `Cmax` — `mg/L`;
- `Cmin` — `mg/L`.

### Final values

- `AUC24` — `mg·h/L` for PRE_POST when required timing data are available;
- optional reference-target display `400-600 mg·h/L` without autonomous dose recommendation.

### Explanation

Each equation must add a `CalculationStep` containing:

1. equation name;
2. formula;
3. substituted input values;
4. result and unit;
5. source identifier/page reference where practical.

---

## 10. Worked-Case Verification Target

The MOH Chapter 15 worked case (PDF pages 275-276) uses:

- dose `750 mg`;
- interval `12 h`;
- pre sample `15.9 mg/L`;
- post sample `29.3 mg/L`;
- pre clock time `05:30`;
- dose start `06:00` with 1-hour infusion;
- post clock time `08:00`.

The source reports approximately:

- `Ke = 0.0643 h^-1`;
- `t1/2 = 10.78 h`;
- `Cmax = 31.25 mg/L`;
- `Cmin = 14.44 mg/L`;
- `Co = 35.53 mg/L`;
- interval AUC about `328 mg·h/L`;
- `AUC24 = 656 mg·h/L`.

These are appropriate future regression-test targets for Member 3.

**Important:** the handbook's worked Vd example contains a numerical denominator value that is not fully consistent with the preceding displayed Cmax value. The implementation should follow the stated equation, not hard-code the worked Vd number as a golden value.

---

## 11. Repository Compatibility Assessment

### `WorkflowRequirements`

Current workflow visibility is compatible with the source structure:

- PRE requires pre concentration and no sampling field;
- POST requires post concentration plus sampling information;
- PRE_POST requires both concentrations, sampling information and additional timing.

No workflow-enum redesign is required.

### `TdmInput`

Current typed fields already cover:

- workflow;
- dose;
- dosing interval;
- pre concentration;
- post concentration;
- generic sampling information.

Stage 4 should add typed fields for:

- `creatinineClearanceMlMin: Double?` for the POST population-Ke path;
- `infusionDurationHours: Double?` for PRE_POST AUC;

and should standardise sampling-map keys if the map is retained.

`age` and `weight` currently live inside `patientParameters`. Stage 4 may either keep them there with safe typed conversion before calculation or migrate them into typed fields. Do not duplicate them in two competing sources of truth.

### `CalculationInputScreen`

Current generic labels must become source-specific:

- Medication dose **(mg)**;
- Dosing interval **(hours)**;
- Pre-dose concentration **(mg/L)**;
- Post-dose concentration **(mg/L)**;
- POST/PRE_POST sampling time: **hours after infusion end**;
- PRE_POST additional timing: **elapsed time between pre and post sample clock times (t2 - t1), hours**.

The UI also needs a source-backed way to collect `CrCl` for POST and infusion duration for PRE_POST AUC.

### `TdmResult`

No structural change is required for the initial engine. Its intermediate, pharmacokinetic, final, and explanation collections can represent all specified outputs.

### Structural validator

Existing numeric/presence validation remains valid. Stage 4 should add formula-domain checks listed in Section 8 when the new calculation inputs are added.

---

## 12. Comparison with `sbr-hkyr/MobileAppProject`

The comparison repository was useful for architecture and feature discovery, but the following rules apply.

### Supported concepts that may be adapted

- dedicated calculation engine outside Compose;
- separate PRE, POST and PRE_POST branches;
- `Ke`, half-life, Cmax/Cmin, Vd and AUC result categories;
- step-by-step explanation output;
- use of the population Vancomycin `Ke` relationship when only one concentration is available.

### Must be modified before use

- use the MOH equations and timing definitions exactly rather than copying that repository's infusion-time adjustments;
- use explicit source-backed units;
- do not use silent default concentrations when required input is absent;
- do not copy unsourced fallback values.

### Must not be copied into the first engine

- hard-coded broad clinical validation ranges such as age/height/weight/dose ranges without a project-approved source;
- automatic dose recommendation text;
- suggested-dose rounding algorithms;
- generic trough status classification detached from indication/context;
- the repository's `1000 mg/hour` infusion warning, because the reviewed MOH handbook instead states infusion over at least 60 minutes or a maximum rate of 10 mg/min, whichever is longer;
- any dosing simulation or treatment recommendation logic not separately specified.

---

## 13. Assumptions and Unresolved Questions

### Source-stated / adopted assumptions

- first implementation is adult intermittent-IV Vancomycin;
- PRE concentration represents the trough/pre-dose level;
- POST sampling delay is measured from **end of infusion**;
- PRE_POST `t2 - t1` is the elapsed clock time between the pre and post sample timestamps used by the MOH equation;
- AUC log method is used as the single deterministic AUC24 method for the first engine;
- the application remains academic and non-clinical.

### Resolved implementation choices

- POST uses direct `CrCl` input for the population-Ke equation to avoid silently embedding an unspecific CrCl calculation policy;
- PRE and POST use the adult MOH Vd equation rather than an unsourced obesity/body-weight selection algorithm;
- automated dose recommendation remains out of scope.

### Remaining non-blocking clarification

If the lecturer specifically requires the app itself to calculate CrCl from serum creatinine, Stage 4 must first document the exact Cockcroft-Gault implementation and weight-selection policy before coding that extra calculation. This does **not** block the current calculation engine because direct `CrCl` input is sufficient for the specified POST pathway.

---

## 14. Implementation Readiness Checklist

- [x] authoritative sources identified
- [x] source pages recorded
- [x] supported implementation scope defined
- [x] PRE workflow specified
- [x] POST workflow specified
- [x] PRE_POST workflow specified
- [x] required inputs identified
- [x] input units identified
- [x] equations identified
- [x] variables defined
- [x] outputs identified
- [x] output units identified
- [x] calculation sequence documented
- [x] mathematical preconditions documented
- [x] assumptions documented
- [x] workflow applicability documented
- [x] current repository compatibility assessed
- [x] friend-repository formulas reviewed against authoritative sources
- [x] no autonomous dosing recommendation included
- [x] no unresolved ambiguity that blocks the scoped first implementation

**Final Stage 3 gate status: READY FOR IMPLEMENTATION.**

The next clinical-code stage may implement this specification, but calculation regression tests should be contributed/reviewed separately by Member 3 to preserve team contribution history.