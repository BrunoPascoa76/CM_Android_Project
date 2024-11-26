package cm.project.cmproject.models

data class Delivery(
    val deliveryId: Int,
    val recipientId: Int,
    val senderId: Int,
    val driverId: Int,
    val parcel: Parcel,
    val status: String,
    val steps: List<Step>,
    val completedSteps: List<Step>
)