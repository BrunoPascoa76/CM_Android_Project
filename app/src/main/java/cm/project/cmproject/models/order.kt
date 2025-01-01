package cm.project.cmproject.models

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DeliveryStatus(
    val status: String = "",
    val timestamp: Long = 0L,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class OrderState(
    val trackingNumber: String = "123456789",
    val driverName: String = "John Doe",
    val driverPhone: String = "+1 234 567 890",
    val currentStatus: OrderStatus = OrderStatus.IN_TRANSIT,
    val pickupLocation: LatLng = LatLng(41.1579, -8.6291), // Porto
    val currentLocation: LatLng = LatLng(40.6405, -8.6538), // Aveiro
    val deliveryLocation: LatLng = LatLng(38.7169, -9.1399)  // Lisbon
)

enum class OrderStatus {
    DRIVER_ASSIGNED, PICKUP, IN_TRANSIT, DELIVERED
}

class OrderViewModel : ViewModel() {
    private val _orderState = MutableStateFlow(OrderState())
    val orderState: StateFlow<OrderState> = _orderState

    fun updateOrderStatus(newStatus: OrderStatus) {
        _orderState.value = _orderState.value.copy(currentStatus = newStatus)
    }

    fun updateDeliveryStatus(deliveryStatus: DeliveryStatus) {
        val newStatus = mapDeliveryStatusToOrderStatus(deliveryStatus)
        val newLocation = LatLng(deliveryStatus.latitude, deliveryStatus.longitude)
        _orderState.value = _orderState.value.copy(
            currentStatus = newStatus,
            currentLocation = if (newStatus == OrderStatus.IN_TRANSIT) newLocation else _orderState.value.currentLocation
        )
    }

    private fun mapDeliveryStatusToOrderStatus(deliveryStatus: DeliveryStatus): OrderStatus {
        return when (deliveryStatus.status) {
            "DRIVER_ASSIGNED" -> OrderStatus.DRIVER_ASSIGNED
            "PICKUP" -> OrderStatus.PICKUP
            "IN_TRANSIT" -> OrderStatus.IN_TRANSIT
            "DELIVERED" -> OrderStatus.DELIVERED
            else -> OrderStatus.IN_TRANSIT
        }
    }

    fun listenForDeliveryStatusUpdates() {
        val database =
            FirebaseDatabase.getInstance("https://cm-android-2024-default-rtdb.europe-west1.firebasedatabase.app/")
        val deliveryStatusRef = database.getReference("deliveryStatus")

        deliveryStatusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deliveryStatus = snapshot.getValue(DeliveryStatus::class.java)
                deliveryStatus?.let {
                    updateDeliveryStatus(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }
}