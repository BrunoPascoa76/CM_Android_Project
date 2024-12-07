package cm.project.cmproject.models

import android.location.Address
import com.google.type.DateTime
import java.util.Locale

data class Step(
    val location: Address=Address(Locale.getDefault()),
    val description:String="",
    val isCompleted:Boolean=false,
    val completionDate: DateTime?=null
)