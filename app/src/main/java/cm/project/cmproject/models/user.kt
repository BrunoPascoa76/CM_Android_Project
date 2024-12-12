package cm.project.cmproject.models

import android.location.Address
import java.util.Locale

data class User(
    val uid:String="",
    val fullName:String="",
    val email: String="",
    val phoneNumber: String="",
    val address: Address= Address(Locale.getDefault()),
    val role: String="customer",

    val license: String="",
    val vehicleType: String=""
)