package cm.project.cmproject.viewModels

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class RealtimeLocationViewModel : ViewModel() {
    private var ref: DatabaseReference? = null
    private var listener: ValueEventListener? = null

    private val _driverLocation: MutableStateFlow<LatLng?> = MutableStateFlow(null)
    val driverLocation: StateFlow<LatLng?> = _driverLocation.asStateFlow()


    fun attachListener(deliveryId: String) {
        detachListener()
        ref =
            FirebaseDatabase.getInstance("https://cm-android-2024-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("deliveryStatus").child(deliveryId)
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitude = snapshot.child("latitude").getValue(Double::class.java)
                val longitude = snapshot.child("longitude").getValue(Double::class.java)
                if (latitude != null && longitude != null) {
                    _driverLocation.value = LatLng(latitude, longitude)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Timber.tag("Realtime DB Error").d(error.toString())
                _driverLocation.value = null
            }
        }
        ref?.addValueEventListener(listener!!)
    }

    fun detachListener() {
        listener?.let { ref?.removeEventListener(it) }
    }
}