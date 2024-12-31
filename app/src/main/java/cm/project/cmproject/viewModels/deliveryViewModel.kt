package cm.project.cmproject.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.cmproject.models.Delivery
import cm.project.cmproject.models.User
import cm.project.cmproject.repositories.DeliveryRepository
import cm.project.cmproject.repositories.Result
import cm.project.cmproject.repositories.UserRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun fetchDelivery(deliveryId: String) {
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


    fun fetchCurrentDelivery(user: User?) {
        if (user != null) {
            viewModelScope.launch {
                when (val result = DeliveryRepository().getAllByUserIdAndStatus(
                    user.uid,
                    listOf("Pending", "Accepted", "Pickup", "In Transit")
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
                        _state.value = null
                        _errorMessage.value = result.exception.message
                    }
                }
            }
        }
    }

    fun submitQRCode(result: String) {
        if (_state.value?.deliveryId.toString() == result) {
            completeCurrentStep()
        }
    }

    fun completeCurrentStep() {
        if (_state.value != null) {
            var status = _state.value!!.status

            val steps = _state.value!!.steps.toMutableList() //create a copy
            if (steps.isNotEmpty()) {
                steps[_state.value!!.completedSteps] = steps[_state.value!!.completedSteps].copy(
                    isCompleted = true,
                    completionDate = Timestamp.now()
                )

                //update main status based on current step
                if (_state.value!!.steps[_state.value!!.completedSteps].description in listOf(
                        "Driver Assigned",
                        "Pickup",
                        "In Transit",
                        "Delivered"
                    )
                ) { // update the "main" status (used for filtering and for users who don't want ALL the details) if completing one of the predefined steps
                    status = _state.value!!.steps[_state.value!!.completedSteps].description
                }
            }
            _state.value = _state.value!!.copy(
                steps = steps,
                status = status,
                completedSteps = _state.value!!.completedSteps + 1
            )
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
}