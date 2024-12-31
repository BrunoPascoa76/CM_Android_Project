package cm.project.cmproject.models

import android.location.Address
import java.time.LocalDateTime
import java.util.Locale

data class Step(
    val location: Address = Address(Locale.getDefault()),
    val description: String = "",
    val isCompleted: Boolean = false,
    val completionDate: LocalDateTime? = null
)