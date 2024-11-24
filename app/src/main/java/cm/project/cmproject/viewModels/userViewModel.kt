package cm.project.cmproject.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import cm.project.cmproject.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
        address: String,
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
        address: String,
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
            }.addOnFailureListener() { exception ->
                _errorMessage.value = exception.message
            }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        _state.value = null
        _errorMessage.value = null
    }

    fun fetchUserTable(uid: String) {
        Firebase.firestore.collection("users").document(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.toObject<User>()
                Log.d("UserViewModel", "User created successfully")
                _state.value = user
                _errorMessage.value = null
            }.addOnFailureListener { exception ->
                _errorMessage.value = exception.message
            }
    }

    private fun createUserTable(user: User) {
        Firebase.firestore.collection("users").document(user.uid).set(user)
            .addOnSuccessListener {
                _state.value = user
                _errorMessage.value = null
                Log.d("UserViewModel", "User created successfully")
            }.addOnFailureListener { exception ->
                _errorMessage.value = exception.message
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
            Firebase.firestore.collection("users").document(user.uid).set(user)
                .addOnSuccessListener {
                    _state.value = user
                    _errorMessage.value = null
                }.addOnFailureListener { exception ->
                    _errorMessage.value = exception.message
                }
        }
    }
}