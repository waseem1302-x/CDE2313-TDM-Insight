package com.example.tdminsight.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkflowRequirementsTest {

    @Test
    fun preRequiresOnlyPreConcentrationAmongConcentrationFields() {
        val requirements = WorkflowType.PRE.requirements()

        assertTrue(requirements.requiresPreConcentration)
        assertFalse(requirements.requiresPostConcentration)
        assertFalse(requirements.requiresSamplingInformation)
        assertFalse(requirements.requiresAdditionalTimingInformation)
        assertFalse(requirements.requiresCreatinineClearance)
        assertFalse(requirements.requiresInfusionDuration)
    }

    @Test
    fun postRequiresPostConcentrationAndSamplingInformation() {
        val requirements = WorkflowType.POST.requirements()

        assertFalse(requirements.requiresPreConcentration)
        assertTrue(requirements.requiresPostConcentration)
        assertTrue(requirements.requiresSamplingInformation)
        assertFalse(requirements.requiresAdditionalTimingInformation)
        assertTrue(requirements.requiresCreatinineClearance)
        assertFalse(requirements.requiresInfusionDuration)
    }

    @Test
    fun prePostRequiresBothConcentrationsAndTimingInformation() {
        val requirements = WorkflowType.PRE_POST.requirements()

        assertTrue(requirements.requiresPreConcentration)
        assertTrue(requirements.requiresPostConcentration)
        assertTrue(requirements.requiresSamplingInformation)
        assertTrue(requirements.requiresAdditionalTimingInformation)
        assertFalse(requirements.requiresCreatinineClearance)
        assertTrue(requirements.requiresInfusionDuration)
    }
}
