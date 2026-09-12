package com.example.tdminsight.navigation

import com.example.tdminsight.model.CalculationStep
import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.TdmResult
import com.example.tdminsight.model.TdmResultValue
import com.example.tdminsight.model.WorkflowType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TdmFlowStateTest {

    @Test
    fun happyPathMovesFromHomeToResult() {
        val input = TdmInput(workflow = WorkflowType.PRE_POST)
        val result = TdmResult(
            intermediateValues = listOf(TdmResultValue("Intermediate", 1.0)),
            pharmacokineticParameters = emptyList(),
            finalValues = listOf(TdmResultValue("Final", 2.0)),
            explanation = listOf(CalculationStep("Step 1", "Example"))
        )

        val finalState = TdmFlowState()
            .startCase()
            .completePatientInput()
            .selectWorkflow(WorkflowType.PRE_POST)
            .submitCalculationInput(input)
            .completeCalculation(result)

        assertEquals(AppScreen.RESULT, finalState.currentScreen)
        assertEquals(WorkflowType.PRE_POST, finalState.selectedWorkflow)
        assertEquals(input, finalState.input)
        assertEquals(result, finalState.result)
        assertNotNull(finalState.result)
    }

    @Test(expected = IllegalStateException::class)
    fun patientInputCannotCompleteFromHome() {
        TdmFlowState().completePatientInput()
    }

    @Test(expected = IllegalArgumentException::class)
    fun submittedInputMustMatchSelectedWorkflow() {
        TdmFlowState()
            .startCase()
            .completePatientInput()
            .selectWorkflow(WorkflowType.PRE)
            .submitCalculationInput(TdmInput(workflow = WorkflowType.POST))
    }
}
