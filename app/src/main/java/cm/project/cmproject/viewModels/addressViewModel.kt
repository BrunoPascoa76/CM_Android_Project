package cm.project.cmproject.viewModels

import android.location.Address
import androidx.lifecycle.ViewModel
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