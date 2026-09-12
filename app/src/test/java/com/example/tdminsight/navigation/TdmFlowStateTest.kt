package com.example.tdminsight.navigation

import com.example.tdminsight.calculation.TdmCalculator
import com.example.tdminsight.calculation.VancomycinTdmCalculator
import com.example.tdminsight.model.CalculationStep
import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.TdmInputKeys
import com.example.tdminsight.model.TdmResult
import com.example.tdminsight.model.TdmResultValue
import com.example.tdminsight.model.WorkflowType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TdmFlowStateTest {

    @Test
    fun happyPathMovesFromHomeToResult() {
        val input = TdmInput(workflow = WorkflowType.PRE_POST)
        val result = sampleResult()

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

    @Test
    fun calculationTransitionsReviewToResultAndPassesSubmittedInput() {
        val input = TdmInput(workflow = WorkflowType.POST)
        val expectedResult = sampleResult()
        var receivedInput: TdmInput? = null
        val fakeCalculator = object : TdmCalculator {
            override fun calculate(input: TdmInput): TdmResult {
                receivedInput = input
                return expectedResult
            }
        }

        val reviewState = TdmFlowState()
            .startCase()
            .completePatientInput()
            .selectWorkflow(WorkflowType.POST)
            .submitCalculationInput(input)

        val finalState = reviewState.calculate(fakeCalculator)

        assertEquals(AppScreen.RESULT, finalState.currentScreen)
        assertSame(input, receivedInput)
        assertSame(expectedResult, finalState.result)
    }

    @Test(expected = IllegalStateException::class)
    fun calculateCannotRunFromHome() {
        TdmFlowState().calculate(
            object : TdmCalculator {
                override fun calculate(input: TdmInput): TdmResult = sampleResult()
            }
        )
    }

    @Test
    fun validInputCanReachResultUsingRealVancomycinCalculator() {
        val input = TdmInput(
            workflow = WorkflowType.PRE,
            patientParameters = mapOf(
                TdmInputKeys.AGE_YEARS to "50",
                TdmInputKeys.BODY_WEIGHT_KG to "70"
            ),
            medicationDose = 1000.0,
            dosingInterval = 12.0,
            preDoseConcentration = 15.0
        )

        val finalState = TdmFlowState()
            .startCase()
            .completePatientInput()
            .selectWorkflow(WorkflowType.PRE)
            .submitCalculationInput(input)
            .calculate(VancomycinTdmCalculator())

        assertEquals(AppScreen.RESULT, finalState.currentScreen)
        assertNotNull(finalState.result)
        assertTrue(finalState.result?.pharmacokineticParameters?.isNotEmpty() == true)
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

    private fun sampleResult(): TdmResult = TdmResult(
        intermediateValues = listOf(TdmResultValue("Intermediate", 1.0)),
        pharmacokineticParameters = emptyList(),
        finalValues = listOf(TdmResultValue("Final", 2.0)),
        explanation = listOf(CalculationStep("Step 1", "Example"))
    )
}
