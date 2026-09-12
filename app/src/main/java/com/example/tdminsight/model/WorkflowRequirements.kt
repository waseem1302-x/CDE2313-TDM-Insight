package com.example.tdminsight.model

data class WorkflowRequirements(
    val requiresPreConcentration: Boolean,
    val requiresPostConcentration: Boolean,
    val requiresSamplingInformation: Boolean,
    val requiresAdditionalTimingInformation: Boolean
)

fun WorkflowType.requirements(): WorkflowRequirements = when (this) {
    WorkflowType.PRE -> WorkflowRequirements(
        requiresPreConcentration = true,
        requiresPostConcentration = false,
        requiresSamplingInformation = false,
        requiresAdditionalTimingInformation = false
    )

    WorkflowType.POST -> WorkflowRequirements(
        requiresPreConcentration = false,
        requiresPostConcentration = true,
        requiresSamplingInformation = true,
        requiresAdditionalTimingInformation = false
    )

    WorkflowType.PRE_POST -> WorkflowRequirements(
        requiresPreConcentration = true,
        requiresPostConcentration = true,
        requiresSamplingInformation = true,
        requiresAdditionalTimingInformation = true
    )
}
