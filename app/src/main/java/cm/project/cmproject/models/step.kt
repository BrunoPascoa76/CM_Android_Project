package cm.project.cmproject.models

import android.location.Address
import com.google.type.DateTime

data class Step(
    val location: Address,
    val description:String,
    val isCompleted:Boolean,
    val completionDate: DateTime?
)