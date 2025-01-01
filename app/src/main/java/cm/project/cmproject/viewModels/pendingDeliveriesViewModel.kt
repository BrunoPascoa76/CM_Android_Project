package cm.project.cmproject.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.cmproject.models.Delivery
import cm.project.cmproject.models.User
import cm.project.cmproject.repositories.DeliveryRepository
import cm.project.cmproject.repositories.Result
import cm.project.cmproject.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PendingDeliveriesViewModel : ViewModel() {
    private var _listener: ListenerRegistration? = null
    private val _firestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow<List<Delivery>>(emptyList())
    val state = _state.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _acceptedDelivery = MutableStateFlow(false)
    val acceptedDelivery = _acceptedDelivery.asStateFlow()

    private val _recipient = MutableStateFlow<User?>(null)
    val selectedRecipient = _recipient.asStateFlow()

    private val _sender = MutableStateFlow<User?>(null)
    val selectedSender = _sender.asStateFlow()




    //Note: doing this directly in the viewModel because I couldn't "fit" this properly with the repository stuff neatly
    fun subscribeToPendingDeliveries() {
        _errorMessage.value = null
        _acceptedDelivery.value = false
        _listener = _firestore.collection("pendingDeliveries")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _state.value=snapshot.toObjects(Delivery::class.java)
                }
            }
    }

    fun assignSelfToDelivery(delivery: Delivery, driverId: String) {
        _errorMessage.value = null
        viewModelScope.launch {
            when (val result =
                DeliveryRepository().assignSelfToDelivery(delivery.deliveryId, driverId)) {
                is Result.Success -> {
                    _acceptedDelivery.value = true
                }

                is Result.Error -> _errorMessage.value = result.exception.message
            }
        }
    }

    fun fetchRecipientAndSender(delivery: Delivery) {
        _errorMessage.value = null
        viewModelScope.launch {
            delivery.recipientId?.let {
                when (val result = UserRepository().getUserById(it)) {
                    is Result.Success -> {
                        _recipient.value = result.data
                    }
                    is Result.Error -> _errorMessage.value = result.exception.message
                }
            }
            delivery.senderId?.let {
                when (val result = UserRepository().getUserById(it)) {
                    is Result.Success -> {
                        _sender.value = result.data
                    }

                    is Result.Error -> _errorMessage.value = result.exception.message
                }
            }
        }
    }

    fun unsubscribeFromPendingDeliveries() {
        _listener?.remove()
        _listener = null
    }
}