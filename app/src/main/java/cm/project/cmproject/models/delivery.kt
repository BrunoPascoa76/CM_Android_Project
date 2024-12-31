package cm.project.cmproject.models

data class Delivery(
    val parcelId: String = "",
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
    val email: String = "",
    val phoneNumber: String = "",
    val label: String = "",
    val isFragile: Boolean = false,
    val weight: String = "",
    val length: String = "",
    val width: String = "",
    val height: String = "",
)