package com.example.tdminsight.navigation

import com.example.tdminsight.calculation.TdmCalculator
import com.example.tdminsight.calculation.VancomycinTdmCalculator
import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.TdmResult
import com.example.tdminsight.model.WorkflowType

data class TdmFlowState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val selectedWorkflow: WorkflowType? = null,
    val input: TdmInput? = null,
    val result: TdmResult? = null,
    val disclaimerReturnScreen: AppScreen? = null
) {
    fun startCase(): TdmFlowState {
        requireScreen(AppScreen.HOME)
        return copy(currentScreen = AppScreen.PATIENT_INPUT)
    }

    fun completePatientInput(): TdmFlowState {
        requireScreen(AppScreen.PATIENT_INPUT)
        return copy(currentScreen = AppScreen.WORKFLOW_SELECTION)
    }

    fun chooseWorkflow(workflow: WorkflowType): TdmFlowState {
        requireScreen(AppScreen.WORKFLOW_SELECTION)
        return copy(
            selectedWorkflow = workflow,
            input = null,
            result = null
        )
    }

    fun continueWithSelectedWorkflow(): TdmFlowState {
        requireScreen(AppScreen.WORKFLOW_SELECTION)
        check(selectedWorkflow != null) { "A workflow must be selected before continuing." }
        return copy(currentScreen = AppScreen.CALCULATION_INPUT)
    }

    fun selectWorkflow(workflow: WorkflowType): TdmFlowState =
        chooseWorkflow(workflow).continueWithSelectedWorkflow()

    fun submitCalculationInput(input: TdmInput): TdmFlowState {
        requireScreen(AppScreen.CALCULATION_INPUT)
        require(input.workflow == selectedWorkflow) {
            "Input workflow must match the selected workflow."
        }
        return copy(
            currentScreen = AppScreen.REVIEW,
            input = input,
            result = null
        )
    }

    fun calculate(
        calculator: TdmCalculator = VancomycinTdmCalculator()
    ): TdmFlowState {
        requireScreen(AppScreen.REVIEW)
        val calculationInput = input
            ?: throw IllegalStateException("Calculation input must exist before calculation can run.")
        return completeCalculation(calculator.calculate(calculationInput))
    }

    fun completeCalculation(result: TdmResult): TdmFlowState {
        requireScreen(AppScreen.REVIEW)
        check(input != null) { "Calculation input must exist before a result can be stored." }
        return copy(
            currentScreen = AppScreen.RESULT,
            result = result
        )
    }

    fun openDisclaimer(): TdmFlowState {
        check(currentScreen != AppScreen.DISCLAIMER) { "Disclaimer is already open." }
        return copy(
            currentScreen = AppScreen.DISCLAIMER,
            disclaimerReturnScreen = currentScreen
        )
    }

    fun returnFromDisclaimer(): TdmFlowState {
        requireScreen(AppScreen.DISCLAIMER)
        return copy(
            currentScreen = disclaimerReturnScreen ?: AppScreen.HOME,
            disclaimerReturnScreen = null
        )
    }

    fun goBack(): TdmFlowState = when (currentScreen) {
        AppScreen.HOME -> this
        AppScreen.PATIENT_INPUT -> copy(currentScreen = AppScreen.HOME)
        AppScreen.WORKFLOW_SELECTION -> copy(currentScreen = AppScreen.PATIENT_INPUT)
        AppScreen.CALCULATION_INPUT -> copy(
            currentScreen = AppScreen.WORKFLOW_SELECTION,
            result = null
        )
        AppScreen.REVIEW -> copy(
            currentScreen = AppScreen.CALCULATION_INPUT,
            result = null
        )
        AppScreen.RESULT -> copy(currentScreen = AppScreen.REVIEW)
        AppScreen.DISCLAIMER -> returnFromDisclaimer()
    }

    fun reset(): TdmFlowState = TdmFlowState()

    private fun requireScreen(expected: AppScreen) {
        check(currentScreen == expected) {
            "Expected screen $expected but current screen is $currentScreen."
        }
    }
}
