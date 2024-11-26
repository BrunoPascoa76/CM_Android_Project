import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.android.gms.maps.model.LatLng

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
}