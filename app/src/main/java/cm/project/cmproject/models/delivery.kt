package cm.project.cmproject.models

data class Delivery(
    val deliveryId: Int,
    val recipientId: String?,
    val senderId: String?,
    val driverId: String?,
    val parcel: Parcel,
    val status: String,
    val steps: List<Step>,
    val completedSteps: Int=0
)