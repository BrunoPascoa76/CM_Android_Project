package cm.project.cmproject.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.cmproject.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel: ViewModel(){
    private val _state= MutableStateFlow<User?>(null)
    val state=_state.asStateFlow()
    private val _errorMessage=MutableStateFlow<String?>(null)
    val errorMessage=_errorMessage.asStateFlow()


    fun login(email:String, password:String){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnSuccessListener { task ->
                fetchUserTable(task.user!!.uid)
            }.addOnFailureListener { exception ->
                _errorMessage.value=exception.message
            }
    }

    private fun fetchUserTable(uid:String){
        Firebase.firestore.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
            val user = snapshot.toObject<User>()
            _state.value = user
            _errorMessage.value = null
        }.addOnFailureListener { exception ->
            _errorMessage.value = exception.message
        }
    }
}