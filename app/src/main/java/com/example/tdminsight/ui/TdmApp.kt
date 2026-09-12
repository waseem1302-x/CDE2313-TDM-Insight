package com.example.tdminsight.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tdminsight.calculation.TdmCalculationException
import com.example.tdminsight.model.TdmInputKeys
import com.example.tdminsight.model.WorkflowType
import com.example.tdminsight.navigation.AppScreen
import com.example.tdminsight.navigation.TdmFlowState
import com.example.tdminsight.ui.screens.CalculationField
import com.example.tdminsight.ui.screens.CalculationInputScreen
import com.example.tdminsight.ui.screens.DisclaimerScreen
import com.example.tdminsight.ui.screens.HomeScreen
import com.example.tdminsight.ui.screens.PatientInputScreen
import com.example.tdminsight.ui.screens.ResultScreen
import com.example.tdminsight.ui.screens.ReviewScreen
import com.example.tdminsight.ui.screens.WorkflowSelectionScreen
import com.example.tdminsight.ui.screens.calculationFieldsFor
import com.example.tdminsight.validation.TdmInputDraft
import com.example.tdminsight.validation.TdmInputValidator
import com.example.tdminsight.validation.ValidationIssue

@Composable
fun TdmApp(modifier: Modifier = Modifier) {
    var flowState by remember { mutableStateOf(TdmFlowState()) }
    val validator = remember { TdmInputValidator() }

    var caseId by rememberSaveable { mutableStateOf("") }
    var ageYears by rememberSaveable { mutableStateOf("") }
    var bodyWeightKg by rememberSaveable { mutableStateOf("") }
    var caseNotes by rememberSaveable { mutableStateOf("") }

    var medicationDose by rememberSaveable { mutableStateOf("") }
    var dosingInterval by rememberSaveable { mutableStateOf("") }
    var preDoseConcentration by rememberSaveable { mutableStateOf("") }
    var postDoseConcentration by rememberSaveable { mutableStateOf("") }
    var samplingTime by rememberSaveable { mutableStateOf("") }
    var additionalTimingInformation by rememberSaveable { mutableStateOf("") }
    var creatinineClearanceMlMin by rememberSaveable { mutableStateOf("") }
    var infusionDurationHours by rememberSaveable { mutableStateOf("") }
    var laboratoryNote by rememberSaveable { mutableStateOf("") }

    var validationIssues by remember { mutableStateOf<List<ValidationIssue>>(emptyList()) }

    fun patientParameters(): Map<String, String> = mapOf(
        "caseId" to caseId,
        TdmInputKeys.AGE_YEARS to ageYears,
        TdmInputKeys.BODY_WEIGHT_KG to bodyWeightKg,
        "notes" to caseNotes
    )

    fun clearForm() {
        caseId = ""
        ageYears = ""
        bodyWeightKg = ""
        caseNotes = ""
        medicationDose = ""
        dosingInterval = ""
        preDoseConcentration = ""
        postDoseConcentration = ""
        samplingTime = ""
        additionalTimingInformation = ""
        creatinineClearanceMlMin = ""
        infusionDurationHours = ""
        laboratoryNote = ""
        validationIssues = emptyList()
    }

    fun resetToHome() {
        clearForm()
        flowState = flowState.reset()
    }

    fun beginNewCase() {
        clearForm()
        flowState = flowState.reset().startCase()
    }

    fun goBack() {
        validationIssues = emptyList()
        flowState = flowState.goBack()
    }

    fun submitCalculationInput(workflow: WorkflowType) {
        val draft = buildDraft(
            workflow = workflow,
            patientParameters = patientParameters(),
            medicationDose = medicationDose,
            dosingInterval = dosingInterval,
            preDoseConcentration = preDoseConcentration,
            postDoseConcentration = postDoseConcentration,
            samplingTime = samplingTime,
            additionalTimingInformation = additionalTimingInformation,
            creatinineClearanceMlMin = creatinineClearanceMlMin,
            infusionDurationHours = infusionDurationHours,
            laboratoryNote = laboratoryNote
        )

        val structuralValidation = validator.validate(draft)
        if (structuralValidation.hasErrors) {
            validationIssues = structuralValidation.errors
            return
        }

        val input = structuralValidation.validatedInput ?: return
        val calculationValidation = validator.validateForCalculation(input)
        if (calculationValidation.hasErrors) {
            validationIssues = calculationValidation.errors
            return
        }

        validationIssues = emptyList()
        flowState = flowState.submitCalculationInput(
            calculationValidation.validatedInput ?: input
        )
    }

    fun calculate() {
        try {
            flowState = flowState.calculate()
            validationIssues = emptyList()
        } catch (exception: TdmCalculationException) {
            validationIssues = exception.issues
        }
    }

    BackHandler(enabled = flowState.currentScreen != AppScreen.HOME) {
        goBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TdmAppBar(
                currentScreen = flowState.currentScreen,
                onBack = ::goBack
            )
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (flowState.currentScreen) {
            AppScreen.HOME -> HomeScreen(
                onStart = ::beginNewCase,
                onOpenDisclaimer = {
                    validationIssues = emptyList()
                    flowState = flowState.openDisclaimer()
                },
                modifier = screenModifier
            )

            AppScreen.PATIENT_INPUT -> PatientInputScreen(
                patientParameters = patientParameters(),
                onParameterChange = { key, value ->
                    when (key) {
                        "caseId" -> caseId = value
                        TdmInputKeys.AGE_YEARS -> ageYears = value
                        TdmInputKeys.BODY_WEIGHT_KG -> bodyWeightKg = value
                        "notes" -> caseNotes = value
                    }
                },
                onBack = ::goBack,
                onNext = {
                    validationIssues = emptyList()
                    flowState = flowState.completePatientInput()
                },
                modifier = screenModifier
            )

            AppScreen.WORKFLOW_SELECTION -> WorkflowSelectionScreen(
                selectedWorkflow = flowState.selectedWorkflow,
                onWorkflowSelected = { workflow ->
                    validationIssues = emptyList()
                    flowState = flowState.chooseWorkflow(workflow)
                },
                onContinue = {
                    validationIssues = emptyList()
                    flowState = flowState.continueWithSelectedWorkflow()
                },
                onBack = ::goBack,
                modifier = screenModifier
            )

            AppScreen.CALCULATION_INPUT -> {
                val workflow = flowState.selectedWorkflow
                if (workflow == null) {
                    FlowRecoveryScreen(
                        onReset = ::resetToHome,
                        modifier = screenModifier
                    )
                } else {
                    CalculationInputScreen(
                        workflow = workflow,
                        medicationDose = medicationDose,
                        dosingInterval = dosingInterval,
                        preDoseConcentration = preDoseConcentration,
                        postDoseConcentration = postDoseConcentration,
                        samplingTime = samplingTime,
                        additionalTimingInformation = additionalTimingInformation,
                        laboratoryNote = laboratoryNote,
                        creatinineClearanceMlMin = creatinineClearanceMlMin,
                        infusionDurationHours = infusionDurationHours,
                        validationIssues = validationIssues,
                        onMedicationDoseChange = { medicationDose = it },
                        onDosingIntervalChange = { dosingInterval = it },
                        onPreDoseConcentrationChange = { preDoseConcentration = it },
                        onPostDoseConcentrationChange = { postDoseConcentration = it },
                        onSamplingTimeChange = { samplingTime = it },
                        onAdditionalTimingInformationChange = { additionalTimingInformation = it },
                        onLaboratoryNoteChange = { laboratoryNote = it },
                        onCreatinineClearanceMlMinChange = { creatinineClearanceMlMin = it },
                        onInfusionDurationHoursChange = { infusionDurationHours = it },
                        onBack = ::goBack,
                        onNext = { submitCalculationInput(workflow) },
                        modifier = screenModifier
                    )
                }
            }

            AppScreen.REVIEW -> {
                val input = flowState.input
                if (input == null) {
                    FlowRecoveryScreen(
                        onReset = ::resetToHome,
                        modifier = screenModifier
                    )
                } else {
                    ReviewScreen(
                        input = input,
                        validationIssues = validationIssues,
                        onBack = ::goBack,
                        onCalculate = ::calculate,
                        modifier = screenModifier
                    )
                }
            }

            AppScreen.RESULT -> {
                val result = flowState.result
                if (result == null) {
                    FlowRecoveryScreen(
                        onReset = ::resetToHome,
                        modifier = screenModifier
                    )
                } else {
                    ResultScreen(
                        result = result,
                        onBackToReview = ::goBack,
                        onOpenDisclaimer = {
                            validationIssues = emptyList()
                            flowState = flowState.openDisclaimer()
                        },
                        onStartNewCase = ::resetToHome,
                        modifier = screenModifier
                    )
                }
            }

            AppScreen.DISCLAIMER -> DisclaimerScreen(
                onBack = {
                    validationIssues = emptyList()
                    flowState = flowState.returnFromDisclaimer()
                },
                modifier = screenModifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TdmAppBar(
    currentScreen: AppScreen,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = when (currentScreen) {
                    AppScreen.HOME -> "TDM Insight"
                    AppScreen.PATIENT_INPUT -> "Patient Details"
                    AppScreen.WORKFLOW_SELECTION -> "Workflow"
                    AppScreen.CALCULATION_INPUT -> "Calculation Input"
                    AppScreen.REVIEW -> "Review"
                    AppScreen.RESULT -> "Results"
                    AppScreen.DISCLAIMER -> "Disclaimer"
                }
            )
        },
        navigationIcon = {
            if (currentScreen != AppScreen.HOME) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }
            }
        }
    )
}

@Composable
private fun FlowRecoveryScreen(
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "This case cannot continue because required case data is unavailable.",
            style = MaterialTheme.typography.bodyLarge
        )
        Button(
            onClick = onReset,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Return Home")
        }
    }
}

private fun buildDraft(
    workflow: WorkflowType,
    patientParameters: Map<String, String>,
    medicationDose: String,
    dosingInterval: String,
    preDoseConcentration: String,
    postDoseConcentration: String,
    samplingTime: String,
    additionalTimingInformation: String,
    creatinineClearanceMlMin: String,
    infusionDurationHours: String,
    laboratoryNote: String
): TdmInputDraft {
    val visibleFields = calculationFieldsFor(workflow).toSet()

    fun visibleValue(field: CalculationField, value: String): String =
        if (field in visibleFields) value else ""

    return TdmInputDraft(
        workflow = workflow,
        patientParameters = patientParameters,
        medicationDose = visibleValue(CalculationField.MEDICATION_DOSE, medicationDose),
        dosingInterval = visibleValue(CalculationField.DOSING_INTERVAL, dosingInterval),
        preDoseConcentration = visibleValue(CalculationField.PRE_CONCENTRATION, preDoseConcentration),
        postDoseConcentration = visibleValue(CalculationField.POST_CONCENTRATION, postDoseConcentration),
        samplingTime = visibleValue(CalculationField.SAMPLING_TIME, samplingTime),
        additionalTimingInformation = visibleValue(
            CalculationField.ADDITIONAL_TIMING,
            additionalTimingInformation
        ),
        creatinineClearanceMlMin = visibleValue(
            CalculationField.CREATININE_CLEARANCE,
            creatinineClearanceMlMin
        ),
        infusionDurationHours = visibleValue(
            CalculationField.INFUSION_DURATION,
            infusionDurationHours
        ),
        laboratoryInformation = mapOf("notes" to laboratoryNote)
    )
}
