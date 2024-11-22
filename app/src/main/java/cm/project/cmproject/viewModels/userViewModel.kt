package cm.project.cmproject.viewModels

import androidx.lifecycle.ViewModel
import cm.project.cmproject.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel: ViewModel(){
    private val _state= MutableStateFlow<User?>(null)
    private val state=_state.asStateFlow()
}