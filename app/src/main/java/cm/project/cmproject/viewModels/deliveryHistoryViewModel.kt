package cm.project.cmproject.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.cmproject.models.Delivery
import cm.project.cmproject.repositories.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import cm.project.cmproject.repositories.Result

class DeliveryHistoryViewModel: ViewModel() {
    private val _pastDeliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val pastDeliveries = _pastDeliveries.asStateFlow()

    private val _currentDeliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val currentDeliveries = _currentDeliveries.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun loadCurrentDeliveries(userId: String) {
        _errorMessage.value=null

        viewModelScope.launch {
            when(val result= DeliveryRepository().getAllByUserIdAndStatus(userId,listOf("Pending","Accepted","In Transit"))){
                is Result.Success -> {
                    _currentDeliveries.value=result.data
                }
                is Result.Error -> {
                    _currentDeliveries.value=emptyList()
                    _errorMessage.value=result.exception.message
                }
            }
        }
    }

    fun loadPastDeliveries(userId: String) {
        _errorMessage.value=null

        viewModelScope.launch {
            when(val result= DeliveryRepository().getAllByUserIdAndStatus(userId,listOf("Completed"))) {
                is Result.Success -> {
                    _pastDeliveries.value = result.data
                }
                is Result.Error -> {
                    _pastDeliveries.value = emptyList()
                    _errorMessage.value = result.exception.message
                }
            }
        }
    }
}