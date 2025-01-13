package cm.project.cmproject.viewModels

import android.accounts.NetworkErrorException
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.cmproject.models.Address
import cm.project.cmproject.models.Delivery
import cm.project.cmproject.models.DeliveryStatus
import cm.project.cmproject.models.Dimensions
import cm.project.cmproject.models.Parcel
import cm.project.cmproject.models.Step
import cm.project.cmproject.models.User
import cm.project.cmproject.repositories.DeliveryRepository
import cm.project.cmproject.repositories.LocationRepository
import cm.project.cmproject.repositories.Result
import cm.project.cmproject.repositories.UserRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class DeliveryViewModel : ViewModel() {
    private val _accepted = MutableStateFlow(false)

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

    private val _fromAddress = MutableStateFlow("")
    val fromAddress = _fromAddress.asStateFlow()

    private val _toAddress = MutableStateFlow("")
    val toAddress = _toAddress.asStateFlow()

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private var _deliveryStatusRef: DatabaseReference? = null
    private var _listener: ValueEventListener? = null

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
                val currentStep = steps[_state.value!!.completedSteps]

                //update main status based on current step
                if (currentStep.description in listOf(
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

    @OptIn(ExperimentalPermissionsApi::class)
    fun createStep(
        description: String,
        isCompleted: Boolean,
        context: Context,
        permissionState: PermissionState
    ) {
        if (_state.value != null) {
            val steps = _state.value!!.steps.toMutableList() //create a copy

            var location = Address()

            //to simplify the job for both programmer and driver, I just use the current location, as they are usually created when they are completed
            viewModelScope.launch {
                when (val result =
                    LocationRepository().getCurrentLocation(context, permissionState)) {
                    is Result.Success -> {
                        location = result.data
                    }

                    is Result.Error -> {} //errors are most likely due to the permission not being granted, so let's deal with it gracefully by just using an empty address
                }

                val step = Step(location = location, description = description)

                //if empty it's the first step, else it's between the last one that was completed and the next one
                if (steps.isEmpty()) {
                    steps.add(step)
                } else {
                    steps.add(_state.value!!.completedSteps, step)
                }
                _state.value = _state.value!!.copy(steps = steps)


                if (isCompleted) {
                    completeCurrentStep()
                }
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

    fun createDelivery(
        senderId: String,
        recipientId: String,
        fromAddress: String,
        toAddress: String,

        label: String,
        isFragile: Boolean,
        weight: String,
        length: String,
        width: String,
        height: String,

        context: Context
    ) {
        viewModelScope.launch {
            var driverAssignedStep = Step(description = "Driver Assigned", isCompleted = false)
            var pickupStep = Step(description = "Pickup", isCompleted = false)
            var inTransitStep = Step(description = "In Transit", isCompleted = false)
            var deliveredStep = Step(description = "Delivered", isCompleted = false)

            when (val result =
                LocationRepository().getCoordinatesFromAddress(context, fromAddress)) {
                is Result.Success -> {
                    driverAssignedStep = driverAssignedStep.copy(location = result.data)
                    pickupStep = pickupStep.copy(location = result.data)
                    inTransitStep = inTransitStep.copy(location = result.data)
                }

                is Result.Error -> {}
            }

            when (val result = LocationRepository().getCoordinatesFromAddress(context, toAddress)) {
                is Result.Success -> {
                    deliveredStep = deliveredStep.copy(location = result.data)
                }

                is Result.Error -> {}
            }
            val newDelivery = Delivery(
                deliveryId = UUID.randomUUID().toString(),

                senderId = senderId,
                recipientId = recipientId,

                fromAddress = fromAddress,
                toAddress = toAddress,

                status = "Pending",
                completedSteps = 0,

                steps = listOf(
                    driverAssignedStep,
                    pickupStep,
                    inTransitStep,
                    deliveredStep
                ),

                parcel = Parcel(
                    label = label,
                    isFragile = isFragile,
                    weight = weight,
                    dimensions = Dimensions(
                        length = length,
                        width = width,
                        height = height
                    )
                ),
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
        _fromAddress.value = newAddress
    }

    fun updateToAddress(newAddress: String) {
        _toAddress.value = newAddress
    }

    fun listenForDeliveryStatusUpdates() {
        detachListener()

        val database =
            FirebaseDatabase.getInstance("https://cm-android-2024-default-rtdb.europe-west1.firebasedatabase.app/")
        _deliveryStatusRef = database.getReference("deliveryStatus/${_state.value?.deliveryId}")

        _listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deliveryStatus = snapshot.getValue(DeliveryStatus::class.java)
                deliveryStatus?.let {
                    updateDeliveryStatus(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        }
        _deliveryStatusRef!!.addValueEventListener(_listener!!)
    }

    fun detachListener() {
        _listener?.let { _deliveryStatusRef?.removeEventListener(it) }
    }

    fun updateDeliveryStatus(deliveryStatus: DeliveryStatus) {
//        val newStatus = mapDeliveryStatusToOrderStatus(deliveryStatus)
        val newLocation = LatLng(deliveryStatus.latitude, deliveryStatus.longitude)
        _currentLocation.value = newLocation
    }

//    private fun mapDeliveryStatusToOrderStatus(deliveryStatus: DeliveryStatus): OrderStatus {
//        return when (deliveryStatus.status) {
//            "DRIVER_ASSIGNED" -> OrderStatus.DRIVER_ASSIGNED
//            "PICKUP" -> OrderStatus.PICKUP
//            "IN_TRANSIT" -> OrderStatus.IN_TRANSIT
//            "DELIVERED" -> OrderStatus.DELIVERED
//            else -> OrderStatus.IN_TRANSIT
//        }
//    }

}