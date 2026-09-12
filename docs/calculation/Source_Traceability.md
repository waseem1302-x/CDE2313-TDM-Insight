# Vancomycin Calculation Source Traceability

This document maps each Stage 3 calculation-specification item to an authoritative source location.

The comparison repository `sbr-hkyr/MobileAppProject` is intentionally excluded from the authority column. It was used only to compare implementation choices.

## Source identifiers

- **MOH-PK-2019** — Ministry of Health Malaysia, *Clinical Pharmacokinetics Pharmacy Handbook, Second Edition (2019)*, Chapter 15: Vancomycin.
- **PHIS-TDM-9E** — Ministry of Health Malaysia PhIS & CPS, *User Manual — TDM Calculator, 9th Edition*.
- **VANCO-2020** — ASHP/PIDS/SIDP/IDSA revised Vancomycin consensus guideline (2020), official IDSA executive-summary page.

## Traceability Matrix

| Specification item | Authority | Page/location | Evidence supported |
| --- | --- | --- | --- |
| Vancomycin supports PRE, POST, PRE_POST calculator workflows | PHIS-TDM-9E | PDF p.7 | Workflow names and calculator selection |
| PRE inputs: dose, interval, pre level, serum-creatinine/Vd context | PHIS-TDM-9E | PDF p.7 | PRE workflow input structure |
| POST requires duration from infusion completion to post sampling | PHIS-TDM-9E | PDF p.8 | Post-sampling timing definition |
| PRE_POST requires post delay plus `t2-t1` timing relationship | PHIS-TDM-9E | PDF p.8 | Two timing inputs for two-point calculation |
| Population Vancomycin `Ke = 0.0044 + CrCl × 0.00083` | MOH-PK-2019 | PDF p.261 | Population elimination-rate equation |
| Average Vancomycin Vd reference `0.7 L/kg` | MOH-PK-2019 | PDF pp.261-262 | Population Vd reference |
| Adult Vd equation `Vd(L)=0.17×Age+0.22×TBW+15` | MOH-PK-2019 | PDF p.262 | Selected adult Vd equation |
| AUC24 400-600 reference context | MOH-PK-2019 | PDF pp.263-264 | MOH exposure target context |
| AUC/MIC 400-600 for serious MRSA, MIC assumed 1 mg/L | VANCO-2020 | IDSA executive summary | Current consensus target context |
| Trough sample just before next dose | MOH-PK-2019 | PDF p.269 | Sampling definition |
| Peak sample 1 hour after infusion end | MOH-PK-2019 | PDF p.269 | Sampling reference / timing context |
| Infusion at least 60 min or max 10 mg/min, whichever longer | MOH-PK-2019 | PDF p.271 | Administration timing/rate reference |
| Two-point `Ke = [ln(Cpost)-ln(Cpre)]/[T-(t2-t1)]` | MOH-PK-2019 | PDF p.272 | PRE_POST patient-specific Ke |
| Half-life `t1/2 = 0.693/Ke` | MOH-PK-2019 | PDF p.272 | Half-life equation |
| Post extrapolation `Cmax=Cpost×e^(Ke×t')` | MOH-PK-2019 | PDF p.272 | Peak extrapolation |
| Trough extrapolation `Cmin=Cmax×e^(-Ke×T)` | MOH-PK-2019 | PDF p.272 | Trough extrapolation |
| Single-trough `Cmax=Cmin+Dose/V(L)` | MOH-PK-2019 | PDF p.272 | PRE calculation |
| Single-trough `Ke=[ln(Cmax)-ln(Cmin)]/T` | MOH-PK-2019 | PDF p.272 | PRE calculation |
| Distribution-volume equation derived from displayed Chapter 15 equation and worked case | MOH-PK-2019 | PDF pp.272, 275-276 | `Vd(L)=Dose/[Cmax(1-e^(-KeT))]`, then divide by BW for L/kg |
| AUC log method `AUC=(Co-Cmin)/Ke`; `AUC24=AUC×frequency` | MOH-PK-2019 | PDF p.273 | Selected first-engine AUC method |
| `Co=Cmax×e^(Ke×t'')` and definition of `t''` | MOH-PK-2019 | PDF p.273 | Log-method start-of-infusion extrapolation |
| Worked two-point Ke ≈ 0.0643 h^-1 | MOH-PK-2019 | PDF p.275 | Regression-test reference |
| Worked half-life ≈ 10.78 h | MOH-PK-2019 | PDF p.275 | Regression-test reference |
| Worked Cmax ≈ 31.25 mg/L | MOH-PK-2019 | PDF p.275 | Regression-test reference |
| Worked Cmin ≈ 14.44 mg/L | MOH-PK-2019 | PDF p.275 | Regression-test reference |
| Worked Co ≈ 35.53 mg/L | MOH-PK-2019 | PDF p.275 | Regression-test reference |
| Worked interval AUC ≈ 328 and AUC24 ≈ 656 | MOH-PK-2019 | PDF p.276 | Regression-test reference |
| Clinical result evaluation requires patient condition, not numeric result alone | MOH-PK-2019 | PDF p.274 | Reason autonomous recommendation is excluded |

## Page-by-page Chapter 15 audit

| PDF page | Review outcome |
| --- | --- |
| 261 | Included key PK parameters; population Ke and Vd captured. |
| 262 | Included adult Vd equation and PK context. |
| 263 | Included AUC target context; no automatic dosing logic adopted. |
| 264 | Reviewed renal/indication target details; first engine remains narrower. |
| 265 | Dosing tables reviewed; excluded from first engine. |
| 266 | Cockcroft-Gault cautions and administration constraints reviewed; no hidden CrCl calculator added. |
| 267 | Renal-impairment dosing reviewed; excluded. |
| 268 | Dialysis dosing reviewed; excluded. |
| 269 | Sampling definitions adopted. |
| 270 | Monitoring parameters reviewed; excluded from automatic logic. |
| 271 | Infusion timing/rate context reviewed. |
| 272 | Core PRE/PRE_POST equations adopted. |
| 273 | Log AUC method selected for first engine; alternative AUC methods documented but not selected. |
| 274 | Result-evaluation guidance reviewed; autonomous recommendation excluded. |
| 275 | Worked-case Ke, half-life, Cmax, Cmin and Co used as future test references. |
| 276 | Worked-case AUC/AUC24 used as future test references; Vd numerical inconsistency noted in main specification. |
| 277 | Chapter references reviewed for provenance. |

## Comparison-repository classification

The public repository `sbr-hkyr/MobileAppProject` implements many similar calculations. Its implementation was classified as follows:

### Adaptable after source verification

- dedicated calculation-engine architecture;
- PRE / POST / PRE_POST branching;
- population Ke concept;
- two-point Ke concept;
- half-life and concentration extrapolation;
- explainable calculation steps.

### Do not copy directly

- silent default concentrations;
- hard-coded clinical validation ranges without source mapping;
- automated dose recommendations;
- suggested-dose rounding;
- generic target-trough classification without indication context;
- its 1000 mg/hour infusion warning (the reviewed MOH handbook states a different administration limit/context);
- equations/timing adjustments that differ from the selected MOH calculation specification.

## Source-version caveat

The current PhIS portal lists a newer 13th-edition TDM Calculator manual. The directly reviewed public Vancomycin calculator PDF for this audit was the 9th edition. The MOH 2019 pharmacokinetics handbook is the equation authority for this Stage 3 specification; PhIS is used primarily to confirm workflow/input structure.