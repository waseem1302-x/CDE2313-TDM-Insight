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
import org.junit.Assert.assertNull
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

    @Test
    fun workflowChoiceCanBeSelectedBeforeContinuing() {
        val selectedState = TdmFlowState()
            .startCase()
            .completePatientInput()
            .chooseWorkflow(WorkflowType.POST)

        assertEquals(AppScreen.WORKFLOW_SELECTION, selectedState.currentScreen)
        assertEquals(WorkflowType.POST, selectedState.selectedWorkflow)

        val calculationState = selectedState.continueWithSelectedWorkflow()

        assertEquals(AppScreen.CALCULATION_INPUT, calculationState.currentScreen)
        assertEquals(WorkflowType.POST, calculationState.selectedWorkflow)
    }

    @Test
    fun backNavigationUsesPredictableStageSixTransitions() {
        val input = TdmInput(workflow = WorkflowType.PRE)
        val result = sampleResult()
        val resultState = TdmFlowState()
            .startCase()
            .completePatientInput()
            .selectWorkflow(WorkflowType.PRE)
            .submitCalculationInput(input)
            .completeCalculation(result)

        val reviewState = resultState.goBack()
        assertEquals(AppScreen.REVIEW, reviewState.currentScreen)
        assertEquals(result, reviewState.result)

        val calculationState = reviewState.goBack()
        assertEquals(AppScreen.CALCULATION_INPUT, calculationState.currentScreen)
        assertNull(calculationState.result)

        val workflowState = calculationState.goBack()
        assertEquals(AppScreen.WORKFLOW_SELECTION, workflowState.currentScreen)

        val patientState = workflowState.goBack()
        assertEquals(AppScreen.PATIENT_INPUT, patientState.currentScreen)

        val homeState = patientState.goBack()
        assertEquals(AppScreen.HOME, homeState.currentScreen)
    }

    @Test
    fun changingWorkflowClearsSubmittedInputAndResult() {
        val resultState = TdmFlowState()
            .startCase()
            .completePatientInput()
            .selectWorkflow(WorkflowType.PRE)
            .submitCalculationInput(TdmInput(workflow = WorkflowType.PRE))
            .completeCalculation(sampleResult())

        val changedState = resultState
            .goBack()
            .goBack()
            .goBack()
            .chooseWorkflow(WorkflowType.POST)

        assertEquals(AppScreen.WORKFLOW_SELECTION, changedState.currentScreen)
        assertEquals(WorkflowType.POST, changedState.selectedWorkflow)
        assertNull(changedState.input)
        assertNull(changedState.result)
    }

    @Test
    fun disclaimerReturnsToOriginatingScreen() {
        val reviewState = TdmFlowState()
            .startCase()
            .completePatientInput()
            .selectWorkflow(WorkflowType.PRE)
            .submitCalculationInput(TdmInput(workflow = WorkflowType.PRE))

        val disclaimerState = reviewState.openDisclaimer()
        assertEquals(AppScreen.DISCLAIMER, disclaimerState.currentScreen)

        val returnedState = disclaimerState.returnFromDisclaimer()
        assertEquals(AppScreen.REVIEW, returnedState.currentScreen)
        assertEquals(reviewState.input, returnedState.input)
    }

    @Test
    fun resetClearsCaseStateAndReturnsHome() {
        val populatedState = TdmFlowState()
            .startCase()
            .completePatientInput()
            .selectWorkflow(WorkflowType.PRE)
            .submitCalculationInput(TdmInput(workflow = WorkflowType.PRE))
            .completeCalculation(sampleResult())

        val resetState = populatedState.reset()

        assertEquals(AppScreen.HOME, resetState.currentScreen)
        assertNull(resetState.selectedWorkflow)
        assertNull(resetState.input)
        assertNull(resetState.result)
    }

    private fun sampleResult(): TdmResult = TdmResult(
        intermediateValues = listOf(TdmResultValue("Intermediate", 1.0)),
        pharmacokineticParameters = emptyList(),
        finalValues = listOf(TdmResultValue("Final", 2.0)),
        explanation = listOf(CalculationStep("Step 1", "Example"))
    )
}
