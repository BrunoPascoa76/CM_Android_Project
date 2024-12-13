package cm.project.cmproject.models

data class Delivery(
    val deliveryId: Int=0,
    val recipientId: String?="",
    val senderId: String?="",
    val driverId: String?="",
    val parcel: Parcel=Parcel(),
    val status: String="",
    val steps: List<Step> = listOf(),
    val completedSteps: Int=0,
    val fromAddress: String="",
    val toAddress: String="",
    val toEmail: String="",
    val toPhoneNumber: String="",
)