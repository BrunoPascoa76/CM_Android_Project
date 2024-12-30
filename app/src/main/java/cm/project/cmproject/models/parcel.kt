package cm.project.cmproject.models

data class Parcel(
    val parcelId: String = "",
    val description: String = "",
    val fromAddress: String = "",
    val toAddress: String = "",
    val toEmail: String = "",
    val toPhoneNumber: String = "",
)