package cm.project.cmproject.models

import com.google.firebase.Timestamp

data class Step(
    val location: Address = Address(),
    val description: String = "",
    val isCompleted: Boolean = false,
    val completionDate: Timestamp? = null
)