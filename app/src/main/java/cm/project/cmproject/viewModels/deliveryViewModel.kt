package cm.project.cmproject.viewModels

import android.accounts.NetworkErrorException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.cmproject.models.Delivery
import cm.project.cmproject.models.User
import cm.project.cmproject.repositories.DeliveryRepository
import cm.project.cmproject.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import cm.project.cmproject.repositories.Result
import timber.log.Timber

class DeliveryViewModel : ViewModel() {
    private val _accepted = MutableStateFlow<Boolean>(false)
    val accepted = _accepted.asStateFlow()

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
        getRelatedUsers()
    }

    fun fetchCurrentDelivery(user:User?){
        if(user!=null){
            viewModelScope.launch {
                when (val result = DeliveryRepository().getAllByUserIdAndStatus(
                    user.uid,
                    listOf("Pending","Accepted","In Transit")
                )) {
                    is Result.Success -> {
                        _errorMessage.value = null
                        if (result.data.isEmpty()) {
                            _state.value = null
                        } else {
                            //TODO: consider what we should do if user is involved in multiple active deliveries (more for customers than drivers)
                            _state.value = result.data[0]
                        }
                    }
                    is Result.Error -> {
                        _state.value=null
                        _errorMessage.value = result.exception.message
                    }
                }
            }
        }
    }

    fun submitQRCode(result: String) {
        if(_state.value?.deliveryId.toString() == result){
            incrementDeliveryStatus()
        }
    }

    fun incrementDeliveryStatus(){
        if(_state.value!=null) {
            _state.value= _state.value!!.copy(completedSteps = _state.value!!.completedSteps + 1)
            viewModelScope.launch {
                DeliveryRepository().updateDelivery(_state.value!!)
            }
        }
    }

    private fun getRelatedUsers() {
        viewModelScope.launch {
            if (state.value?.recipientId != null) {
                when (val result = UserRepository().getUserById(_state.value!!.recipientId!!)) {
                    is Result.Success -> {
                        _recipient.value = result.data
                        _errorMessage.value = null
                    }

                    is Result.Error -> {
                        _recipient.value = null
                    }
                }
            } else {
                _recipient.value = null
            }
            if (state.value?.senderId != null) {
                when (val result = UserRepository().getUserById(_state.value!!.senderId!!)) {
                    is Result.Success -> {
                        _sender.value = result.data
                        _errorMessage.value = null
                    }

                    is Result.Error -> {
                        _sender.value = null
                    }
                }
            } else {
                _sender.value = null
            }

            if (_state.value?.driverId != null) {
                when (val result = UserRepository().getUserById(_state.value!!.driverId!!)) {
                    is Result.Success -> {
                        _driver.value = result.data
                        _errorMessage.value = null
                    }

                    is Result.Error -> {
                        _driver.value = null
                    }
                }
            } else {
                _driver.value = null
            }
        }
    }

    fun createDelivery(userId: String, fromAddress: String, toAddress: String) {
        viewModelScope.launch {
            val newDelivery = Delivery(
                senderId = userId,
                fromAddress = fromAddress,
                toAddress = toAddress,
                status = "Pending",
                completedSteps = 0
            )
            when (val deliveryResult = DeliveryRepository().insertDelivery(newDelivery)) {
                is Result.Success -> {
                    _accepted.value = deliveryResult.data
                    _errorMessage.value = null
                }

                is Result.Error -> {
                    when (deliveryResult.exception) {
                        is NetworkErrorException -> {
                            // Handle network error, e.g., show retry option
                            _errorMessage.value =
                                "Network error: ${deliveryResult.exception.message}"
                        }

                        is com.google.firebase.database.DatabaseException -> {
                            // Handle database error, e.g., log the error and show a generic message
                            Timber.tag("DeliveryViewModel")
                                .e(deliveryResult.exception, "Database error")
                            _errorMessage.value = "Failed to save delivery."
                        }

                        else -> {
                            _errorMessage.value = deliveryResult.exception.message
                        }
                    }
                }
            }
        }
    }

    fun updateFromAddress(newAddress: String) {
        _state.value = _state.value?.copy(fromAddress = newAddress) ?: Delivery(fromAddress = newAddress)
    }

    fun updateToAddress(newAddress: String) {
        _state.value = _state.value?.copy(toAddress = newAddress) ?: Delivery(toAddress = newAddress)
    }
}