package cm.project.cmproject.models

data class Delivery(
    val deliveryId: String = "",
    val recipientId: String? = "",
    val senderId: String? = "",
    val driverId: String? = "",
    val parcel: Parcel = Parcel(),
    val status: String = "",
    val steps: List<Step> = listOf(),
    val completedSteps: Int = 0,
    val fromAddress: String = "",
    val toAddress: String = "",
)

data class DeliveryStatus(
    val status: String = "",
    val timestamp: Long = 0L,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)