package cm.project.cmproject.viewModels

import androidx.lifecycle.ViewModel
import cm.project.cmproject.models.Delivery
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PendingDeliveriesViewModel : ViewModel() {
    private var _listener: ListenerRegistration? = null
    private val _firestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow<List<Delivery>>(emptyList())
    val state = _state.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()


    //Note: doing this directly in the viewModel because I couldn't "fit" this properly with the repository stuff neatly
    fun subscribeToPendingDeliveries() {
        _listener = _firestore.collection("pendingDeliveries")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _state.value = snapshot.toObjects(Delivery::class.java)
                }
            }
    }

    fun unsubscribeFromPendingDeliveries() {
        _listener?.remove()
        _listener = null
    }
}