package cm.project.cmproject.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.cmproject.models.Parcel
import cm.project.cmproject.models.User
import cm.project.cmproject.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import cm.project.cmproject.repositories.Result

class ParcelViewModel : ViewModel() {
    private val _state = MutableStateFlow<Parcel?>(null)
    val state = _state.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun register(
        fromAddress: String,
        toAddress: String,
        toEmail: String,
        toPhoneNumber: String,
    ) {

    }

}