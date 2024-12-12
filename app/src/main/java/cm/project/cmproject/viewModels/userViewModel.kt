package cm.project.cmproject.viewModels

import android.location.Address
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.cmproject.models.User
import cm.project.cmproject.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import cm.project.cmproject.repositories.Result

class UserViewModel : ViewModel() {
    private val _state = MutableStateFlow<User?>(null)
    val state = _state.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()


    fun login(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { task ->
                fetchUserTable(task.user!!.uid)
            }.addOnFailureListener { exception ->
                _errorMessage.value = exception.message
            }
    }

    fun register(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String,
        address: Address,
        role: String
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { task ->
                val user = User(task.user!!.uid, fullName, email, phoneNumber, address, role)
                createUserTable(user)
            }.addOnFailureListener { exception ->
                _errorMessage.value = exception.message
            }
    }

    fun register(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String,
        address: Address,
        role: String,
        license: String,
        vehicleType: String
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { task ->
                val user = User(
                    task.user!!.uid,
                    fullName,
                    email,
                    phoneNumber,
                    address,
                    role,
                    license,
                    vehicleType
                )
                createUserTable(user)
            }.addOnFailureListener { exception ->
                _errorMessage.value = exception.message
            }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        _state.value = null
        _errorMessage.value = null
    }

    fun fetchUserTable(uid: String){
        viewModelScope.launch{
            when(val result= UserRepository().getUserById(uid)){
                is Result.Success -> {
                    _state.value = result.data
                    _errorMessage.value = null
                }
                is Result.Error -> {
                    _errorMessage.value = result.exception.message
                }
            }
        }
    }

    private fun createUserTable(user: User) {
        viewModelScope.launch {
            when (val result = UserRepository().insertUser(user)){
                is Result.Success -> {
                    _state.value = user
                    _errorMessage.value = null
                }
                is Result.Error -> {
                    _errorMessage.value = result.exception.message
                }
            }
        }
    }

    fun update(user: User) {
        if( _state.value==user ){
            return
        }

        if(_state.value!=null && _state.value?.email!=user.email){
            FirebaseAuth.getInstance().currentUser!!.verifyBeforeUpdateEmail(user.email)
        }

        if (user.uid.isNotEmpty()) {
            viewModelScope.launch {
                when (val result = UserRepository().updateUser(user)){
                    is Result.Success -> {
                        _state.value = user
                        _errorMessage.value = null
                    }
                    is Result.Error -> {
                        _errorMessage.value = result.exception.message
                    }
                }
            }
        }
    }
}