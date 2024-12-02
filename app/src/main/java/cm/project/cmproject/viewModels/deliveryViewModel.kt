package cm.project.cmproject.viewModels

import androidx.lifecycle.ViewModel
import cm.project.cmproject.models.Delivery
import cm.project.cmproject.models.Parcel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.firestore.toObject

class DeliveryViewModel : ViewModel() {
    private val _state = MutableStateFlow<Delivery?>(null)
    val state = _state.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun fetchDelivery(deliveryId: Int) {
        _errorMessage.value = null

        //TODO: comment this (and uncomment below) when we no longer need mocks
        _state.value = Delivery(deliveryId, 123, 456, 789, Parcel(1, "Parcel"), "Pending", listOf(), listOf())
        /*
        Firebase.firestore.collection("deliveries").document(deliveryId.toString()).get()
            .addOnSuccessListener { snapshot->
                _state.value = snapshot.toObject<Delivery>()
            }
            .addOnFailureListener{ exception ->
                _errorMessage.value = exception.message
            }
         */
    }
}