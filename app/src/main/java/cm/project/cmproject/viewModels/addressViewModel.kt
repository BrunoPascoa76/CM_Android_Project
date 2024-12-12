package cm.project.cmproject.viewModels

import android.location.Address
import androidx.lifecycle.ViewModel
import cm.project.cmproject.models.Delivery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AddressViewModel: ViewModel() {
    private val _state = MutableStateFlow<Address?>(null)
    val state = _state.asStateFlow()

    fun setAddress(address:Address?){
        _state.value=address
    }

    fun clearAddress(){
        _state.value=null
    }
}