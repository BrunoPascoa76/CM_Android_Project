package cm.project.cmproject.models

data class User(
    val uid:String="",
    val fullName:String="",
    val email: String="",
    val phoneNumber: String="",
    val address: String="",
    val role: String="customer",
    val license: String="",
    val vehicleType: String=""
)