package com.example.tdminsight.navigation

import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.TdmResult
import com.example.tdminsight.model.WorkflowType

data class TdmFlowState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val selectedWorkflow: WorkflowType? = null,
    val input: TdmInput? = null,
    val result: TdmResult? = null
) {
    fun startCase(): TdmFlowState {
        requireScreen(AppScreen.HOME)
        return copy(currentScreen = AppScreen.PATIENT_INPUT)
    }

    fun completePatientInput(): TdmFlowState {
        requireScreen(AppScreen.PATIENT_INPUT)
        return copy(currentScreen = AppScreen.WORKFLOW_SELECTION)
    }

    fun selectWorkflow(workflow: WorkflowType): TdmFlowState {
        requireScreen(AppScreen.WORKFLOW_SELECTION)
        return copy(
            currentScreen = AppScreen.CALCULATION_INPUT,
            selectedWorkflow = workflow,
            input = null,
            result = null
        )
    }

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

    fun completeCalculation(result: TdmResult): TdmFlowState {
        requireScreen(AppScreen.REVIEW)
        check(input != null) { "Calculation input must exist before a result can be stored." }
        return copy(
            currentScreen = AppScreen.RESULT,
            result = result
        )
    }

    fun openDisclaimer(): TdmFlowState = copy(currentScreen = AppScreen.DISCLAIMER)

    fun reset(): TdmFlowState = TdmFlowState()

    private fun requireScreen(expected: AppScreen) {
        check(currentScreen == expected) {
            "Expected screen $expected but current screen is $currentScreen."
        }
    }
}
