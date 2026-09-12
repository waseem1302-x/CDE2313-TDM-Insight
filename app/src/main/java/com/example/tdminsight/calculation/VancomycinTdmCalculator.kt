package com.example.tdminsight.calculation

import com.example.tdminsight.model.CalculationStep
import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.TdmInputKeys
import com.example.tdminsight.model.TdmResult
import com.example.tdminsight.model.TdmResultValue
import com.example.tdminsight.model.WorkflowType
import com.example.tdminsight.validation.TdmInputValidator
import com.example.tdminsight.validation.ValidationIssue
import com.example.tdminsight.validation.ValidationSeverity
import java.util.Locale
import kotlin.math.exp
import kotlin.math.ln

class VancomycinTdmCalculator(
    private val validator: TdmInputValidator = TdmInputValidator()
) : TdmCalculator {

    override fun calculate(input: TdmInput): TdmResult {
        val validation = validator.validateForCalculation(input)
        if (validation.hasErrors) {
            throw TdmCalculationException(validation.errors)
        }

        return when (input.workflow) {
            WorkflowType.PRE -> calculatePre(input)
            WorkflowType.POST -> calculatePost(input)
            WorkflowType.PRE_POST -> calculatePrePost(input)
        }
    }

    private fun calculatePre(input: TdmInput): TdmResult {
        val ageYears = patientValue(input, TdmInputKeys.AGE_YEARS)
        val bodyWeightKg = patientValue(input, TdmInputKeys.BODY_WEIGHT_KG)
        val doseMg = requiredValue(input.medicationDose, "medicationDose")
        val intervalHours = requiredValue(input.dosingInterval, "dosingInterval")
        val cmin = requiredValue(input.preDoseConcentration, "preDoseConcentration")

        val vdLitres = adultVolumeOfDistribution(ageYears, bodyWeightKg)
        val cmax = positiveFinite(
            value = cmin + doseMg / vdLitres,
            field = "Cmax"
        )
        val ke = positiveFinite(
            value = (ln(cmax) - ln(cmin)) / intervalHours,
            field = "Ke"
        )
        val halfLifeHours = positiveFinite(
            value = HALF_LIFE_NUMERATOR / ke,
            field = "halfLife"
        )

        return TdmResult(
            intermediateValues = listOf(
                TdmResultValue("Adult Vd estimate", vdLitres, "L")
            ),
            pharmacokineticParameters = listOf(
                TdmResultValue("Ke", ke, "h⁻¹"),
                TdmResultValue("Half-life", halfLifeHours, "h"),
                TdmResultValue("Cmax", cmax, "mg/L"),
                TdmResultValue("Cmin", cmin, "mg/L")
            ),
            finalValues = emptyList(),
            explanation = listOf(
                calculationStep(
                    title = "Adult Volume of Distribution",
                    formula = "Vd = 0.17 × Age + 0.22 × TBW + 15",
                    substitution = "0.17 × ${format(ageYears)} + 0.22 × ${format(bodyWeightKg)} + 15",
                    result = vdLitres,
                    unit = "L"
                ),
                calculationStep(
                    title = "Estimated Peak Concentration",
                    formula = "Cmax = Cmin + Dose / Vd",
                    substitution = "${format(cmin)} + ${format(doseMg)} / ${format(vdLitres)}",
                    result = cmax,
                    unit = "mg/L"
                ),
                calculationStep(
                    title = "Elimination Rate Constant",
                    formula = "Ke = [ln(Cmax) - ln(Cmin)] / T",
                    substitution = "[ln(${format(cmax)}) - ln(${format(cmin)})] / ${format(intervalHours)}",
                    result = ke,
                    unit = "h⁻¹"
                ),
                calculationStep(
                    title = "Half-life",
                    formula = "t1/2 = 0.693 / Ke",
                    substitution = "0.693 / ${format(ke)}",
                    result = halfLifeHours,
                    unit = "h"
                )
            )
        )
    }

    private fun calculatePost(input: TdmInput): TdmResult {
        val ageYears = patientValue(input, TdmInputKeys.AGE_YEARS)
        val bodyWeightKg = patientValue(input, TdmInputKeys.BODY_WEIGHT_KG)
        val intervalHours = requiredValue(input.dosingInterval, "dosingInterval")
        val cpost = requiredValue(input.postDoseConcentration, "postDoseConcentration")
        val postSampleDelayHours = samplingValue(input, TdmInputKeys.POST_SAMPLE_DELAY_HOURS)
        val creatinineClearanceMlMin = requiredValue(
            input.creatinineClearanceMlMin,
            "creatinineClearanceMlMin"
        )

        val vdLitres = adultVolumeOfDistribution(ageYears, bodyWeightKg)
        val ke = positiveFinite(
            value = POPULATION_KE_INTERCEPT +
                (creatinineClearanceMlMin * POPULATION_KE_CRCL_COEFFICIENT),
            field = "Ke"
        )
        val cmax = positiveFinite(
            value = cpost * exp(ke * postSampleDelayHours),
            field = "Cmax"
        )
        val cmin = positiveFinite(
            value = cmax * exp(-ke * intervalHours),
            field = "Cmin"
        )
        val halfLifeHours = positiveFinite(
            value = HALF_LIFE_NUMERATOR / ke,
            field = "halfLife"
        )

        return TdmResult(
            intermediateValues = listOf(
                TdmResultValue("Adult Vd estimate", vdLitres, "L")
            ),
            pharmacokineticParameters = listOf(
                TdmResultValue("Ke", ke, "h⁻¹"),
                TdmResultValue("Half-life", halfLifeHours, "h"),
                TdmResultValue("Cmax", cmax, "mg/L"),
                TdmResultValue("Cmin", cmin, "mg/L")
            ),
            finalValues = emptyList(),
            explanation = listOf(
                calculationStep(
                    title = "Adult Volume of Distribution",
                    formula = "Vd = 0.17 × Age + 0.22 × TBW + 15",
                    substitution = "0.17 × ${format(ageYears)} + 0.22 × ${format(bodyWeightKg)} + 15",
                    result = vdLitres,
                    unit = "L"
                ),
                calculationStep(
                    title = "Population Elimination Rate Constant",
                    formula = "Ke = 0.0044 + (CrCl × 0.00083)",
                    substitution = "0.0044 + (${format(creatinineClearanceMlMin)} × 0.00083)",
                    result = ke,
                    unit = "h⁻¹"
                ),
                calculationStep(
                    title = "Extrapolated Peak Concentration",
                    formula = "Cmax = Cpost × e^(Ke × t')",
                    substitution = "${format(cpost)} × e^(${format(ke)} × ${format(postSampleDelayHours)})",
                    result = cmax,
                    unit = "mg/L"
                ),
                calculationStep(
                    title = "Projected Trough Concentration",
                    formula = "Cmin = Cmax × e^(-Ke × T)",
                    substitution = "${format(cmax)} × e^(-${format(ke)} × ${format(intervalHours)})",
                    result = cmin,
                    unit = "mg/L"
                ),
                calculationStep(
                    title = "Half-life",
                    formula = "t1/2 = 0.693 / Ke",
                    substitution = "0.693 / ${format(ke)}",
                    result = halfLifeHours,
                    unit = "h"
                )
            )
        )
    }

    private fun calculatePrePost(input: TdmInput): TdmResult {
        val bodyWeightKg = patientValue(input, TdmInputKeys.BODY_WEIGHT_KG)
        val doseMg = requiredValue(input.medicationDose, "medicationDose")
        val intervalHours = requiredValue(input.dosingInterval, "dosingInterval")
        val cpre = requiredValue(input.preDoseConcentration, "preDoseConcentration")
        val cpost = requiredValue(input.postDoseConcentration, "postDoseConcentration")
        val postSampleDelayHours = samplingValue(input, TdmInputKeys.POST_SAMPLE_DELAY_HOURS)
        val sampleDifferenceHours = samplingValue(
            input,
            TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS
        )
        val infusionDurationHours = requiredValue(
            input.infusionDurationHours,
            "infusionDurationHours"
        )

        val eliminationDenominatorHours = positiveFinite(
            value = intervalHours - sampleDifferenceHours,
            field = "twoPointDenominator"
        )
        val ke = positiveFinite(
            value = (ln(cpost) - ln(cpre)) / eliminationDenominatorHours,
            field = "Ke"
        )
        val halfLifeHours = positiveFinite(
            value = HALF_LIFE_NUMERATOR / ke,
            field = "halfLife"
        )
        val cmax = positiveFinite(
            value = cpost * exp(ke * postSampleDelayHours),
            field = "Cmax"
        )
        val cmin = positiveFinite(
            value = cmax * exp(-ke * intervalHours),
            field = "Cmin"
        )
        val vdFraction = positiveFinite(
            value = 1.0 - exp(-ke * intervalHours),
            field = "Vd denominator factor"
        )
        val vdDenominator = positiveFinite(
            value = cmax * vdFraction,
            field = "Vd denominator"
        )
        val vdLitres = positiveFinite(
            value = doseMg / vdDenominator,
            field = "Vd"
        )
        val vdLitresPerKg = positiveFinite(
            value = vdLitres / bodyWeightKg,
            field = "Vd/BW"
        )

        val aucTimingHours = finiteValue(
            value = infusionDurationHours + postSampleDelayHours,
            field = "t''"
        )
        val co = positiveFinite(
            value = cmax * exp(ke * aucTimingHours),
            field = "Co"
        )
        val aucInterval = positiveFinite(
            value = (co - cmin) / ke,
            field = "AUC interval"
        )
        val dosingFrequencyPerDay = positiveFinite(
            value = HOURS_PER_DAY / intervalHours,
            field = "dosing frequency per day"
        )
        val auc24 = positiveFinite(
            value = aucInterval * dosingFrequencyPerDay,
            field = "AUC24"
        )

        return TdmResult(
            intermediateValues = listOf(
                TdmResultValue(
                    "Two-point denominator",
                    eliminationDenominatorHours,
                    "h"
                ),
                TdmResultValue("t''", aucTimingHours, "h"),
                TdmResultValue("Co", co, "mg/L"),
                TdmResultValue(
                    "Dosing frequency per day",
                    dosingFrequencyPerDay,
                    "doses/day"
                ),
                TdmResultValue("AUC interval", aucInterval, "mg·h/L")
            ),
            pharmacokineticParameters = listOf(
                TdmResultValue("Ke", ke, "h⁻¹"),
                TdmResultValue("Half-life", halfLifeHours, "h"),
                TdmResultValue("Vd", vdLitres, "L"),
                TdmResultValue("Vd/BW", vdLitresPerKg, "L/kg"),
                TdmResultValue("Cmax", cmax, "mg/L"),
                TdmResultValue("Cmin", cmin, "mg/L")
            ),
            finalValues = listOf(
                TdmResultValue("AUC24", auc24, "mg·h/L")
            ),
            explanation = listOf(
                calculationStep(
                    title = "Two-point Elimination Denominator",
                    formula = "T - (t2 - t1)",
                    substitution = "${format(intervalHours)} - ${format(sampleDifferenceHours)}",
                    result = eliminationDenominatorHours,
                    unit = "h"
                ),
                calculationStep(
                    title = "Patient-specific Elimination Rate Constant",
                    formula = "Ke = [ln(Cpost) - ln(Cpre)] / [T - (t2 - t1)]",
                    substitution = "[ln(${format(cpost)}) - ln(${format(cpre)})] / ${format(eliminationDenominatorHours)}",
                    result = ke,
                    unit = "h⁻¹"
                ),
                calculationStep(
                    title = "Half-life",
                    formula = "t1/2 = 0.693 / Ke",
                    substitution = "0.693 / ${format(ke)}",
                    result = halfLifeHours,
                    unit = "h"
                ),
                calculationStep(
                    title = "Extrapolated Peak Concentration",
                    formula = "Cmax = Cpost × e^(Ke × t')",
                    substitution = "${format(cpost)} × e^(${format(ke)} × ${format(postSampleDelayHours)})",
                    result = cmax,
                    unit = "mg/L"
                ),
                calculationStep(
                    title = "Projected Trough Concentration",
                    formula = "Cmin = Cmax × e^(-Ke × T)",
                    substitution = "${format(cmax)} × e^(-${format(ke)} × ${format(intervalHours)})",
                    result = cmin,
                    unit = "mg/L"
                ),
                calculationStep(
                    title = "Volume of Distribution",
                    formula = "Vd = Dose / [Cmax × (1 - e^(-Ke × T))]",
                    substitution = "${format(doseMg)} / [${format(cmax)} × (1 - e^(-${format(ke)} × ${format(intervalHours)}))]",
                    result = vdLitres,
                    unit = "L"
                ),
                calculationStep(
                    title = "Weight-normalised Volume of Distribution",
                    formula = "Vd/BW = Vd / BW",
                    substitution = "${format(vdLitres)} / ${format(bodyWeightKg)}",
                    result = vdLitresPerKg,
                    unit = "L/kg"
                ),
                calculationStep(
                    title = "AUC Timing from Infusion Start",
                    formula = "t'' = infusionDurationHours + postSampleDelayHours",
                    substitution = "${format(infusionDurationHours)} + ${format(postSampleDelayHours)}",
                    result = aucTimingHours,
                    unit = "h"
                ),
                calculationStep(
                    title = "Start-of-infusion Concentration",
                    formula = "Co = Cmax × e^(Ke × t'')",
                    substitution = "${format(cmax)} × e^(${format(ke)} × ${format(aucTimingHours)})",
                    result = co,
                    unit = "mg/L"
                ),
                calculationStep(
                    title = "Interval AUC",
                    formula = "AUC_interval = (Co - Cmin) / Ke",
                    substitution = "(${format(co)} - ${format(cmin)}) / ${format(ke)}",
                    result = aucInterval,
                    unit = "mg·h/L"
                ),
                calculationStep(
                    title = "Dosing Frequency per Day",
                    formula = "dosingFrequencyPerDay = 24 / T",
                    substitution = "24 / ${format(intervalHours)}",
                    result = dosingFrequencyPerDay,
                    unit = "doses/day"
                ),
                calculationStep(
                    title = "24-hour AUC",
                    formula = "AUC24 = AUC_interval × dosingFrequencyPerDay",
                    substitution = "${format(aucInterval)} × ${format(dosingFrequencyPerDay)}",
                    result = auc24,
                    unit = "mg·h/L"
                )
            )
        )
    }

    private fun adultVolumeOfDistribution(ageYears: Double, bodyWeightKg: Double): Double =
        positiveFinite(
            value = (VOLUME_AGE_COEFFICIENT * ageYears) +
                (VOLUME_WEIGHT_COEFFICIENT * bodyWeightKg) +
                VOLUME_INTERCEPT_LITRES,
            field = "Vd"
        )

    private fun patientValue(input: TdmInput, key: String): Double {
        val value = input.patientParameters[key]
            ?.trim()
            ?.toDoubleOrNull()
        return requiredValue(value, key)
    }

    private fun samplingValue(input: TdmInput, key: String): Double =
        requiredValue(input.samplingInformation[key], key)

    private fun requiredValue(value: Double?, field: String): Double {
        if (value == null || !value.isFinite()) {
            fail(field, "A required validated finite value is unavailable.")
        }
        return value
    }

    private fun finiteValue(value: Double, field: String): Double {
        if (!value.isFinite()) {
            fail(field, "Calculated value must remain finite.")
        }
        return value
    }

    private fun positiveFinite(value: Double, field: String): Double {
        if (!value.isFinite() || value <= 0.0) {
            fail(field, "Calculated value must be positive and finite.")
        }
        return value
    }

    private fun calculationStep(
        title: String,
        formula: String,
        substitution: String,
        result: Double,
        unit: String
    ): CalculationStep = CalculationStep(
        title = title,
        detail = "Formula: $formula\nSubstitution: $substitution\nResult: ${format(result)} $unit"
    )

    private fun format(value: Double): String =
        String.format(Locale.US, "%.4f", value)
            .trimEnd('0')
            .trimEnd('.')

    private fun fail(field: String, message: String): Nothing {
        throw TdmCalculationException(
            listOf(
                ValidationIssue(
                    field = field,
                    message = message,
                    severity = ValidationSeverity.ERROR
                )
            )
        )
    }

    private companion object {
        const val VOLUME_AGE_COEFFICIENT = 0.17
        const val VOLUME_WEIGHT_COEFFICIENT = 0.22
        const val VOLUME_INTERCEPT_LITRES = 15.0
        const val POPULATION_KE_INTERCEPT = 0.0044
        const val POPULATION_KE_CRCL_COEFFICIENT = 0.00083
        const val HALF_LIFE_NUMERATOR = 0.693
        const val HOURS_PER_DAY = 24.0
    }
}
