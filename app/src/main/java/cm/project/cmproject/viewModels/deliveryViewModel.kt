package cm.project.cmproject.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.cmproject.models.Delivery
import cm.project.cmproject.models.Parcel
import cm.project.cmproject.models.User
import cm.project.cmproject.repositories.DeliveryRepository
import cm.project.cmproject.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import cm.project.cmproject.repositories.Result

class DeliveryViewModel : ViewModel() {
    private val _state = MutableStateFlow<Delivery?>(null)
    val state = _state.asStateFlow()

    private val _recipient = MutableStateFlow<User?>(null)
    val recipient = _recipient.asStateFlow()

    private val _sender = MutableStateFlow<User?>(null)
    val sender = _sender.asStateFlow()

    private val _driver = MutableStateFlow<User?>(null)
    val driver = _driver.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun fetchDelivery(deliveryId: Int) {
        _errorMessage.value = null

        //TODO: comment this (and uncomment below) when we no longer need mocks (the user details, however, are unmocked)
        _state.value = Delivery(deliveryId, "q6nH3mMR3jcKTa6nXNOrqneZZPA2", "q6nH3mMR3jcKTa6nXNOrqneZZPA2", "q6nH3mMR3jcKTa6nXNOrqneZZPA2", Parcel(1, "Parcel"), "Pending", listOf(), listOf())
        /*
        viewModelScope.launch {
            when (val result = DeliveryRepository().getDeliveryById(deliveryId)) {
                is Result.Success -> {
                    _state.value = result.data
                    _errorMessage.value = null
                }
                is Result.Error -> {
                    _state.value = null
                    _errorMessage.value = result.exception.message
                }
            }
        }
        */
        getRelatedUsers()
    }

    private fun getRelatedUsers(){
        if(_state.value==null){
            _recipient.value=null
            _sender.value=null
            _driver.value=null
        }else{
            viewModelScope.launch {
                when(val result= UserRepository().getUserById(_state.value!!.recipientId)){
                    is Result.Success -> {
                        _recipient.value = result.data
                        _errorMessage.value = null
                    }
                    is Result.Error -> {
                        _recipient.value = null
                    }
                }
                when(val result= UserRepository().getUserById(_state.value!!.senderId)) {
                    is Result.Success -> {
                        _sender.value = result.data
                        _errorMessage.value = null
                    }
                    is Result.Error -> {
                        _sender.value = null
                    }
                }
                when(val result= UserRepository().getUserById(_state.value!!.driverId)) {
                    is Result.Success -> {
                        _driver.value = result.data
                        _errorMessage.value = null
                    }
                    is Result.Error -> {
                        _driver.value = null
                    }
                }
            }
        }
    }
}