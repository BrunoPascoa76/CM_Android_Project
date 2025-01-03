package cm.project.cmproject.repositories

import cm.project.cmproject.models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class UserRepository{
    suspend fun getUserById(id: String): Result<User?> {
        return try{
            val snapshot= Firebase.firestore.collection("users").document(id).get().await()
            val user=snapshot.toObject<User>()
            Result.Success(user)
        }catch (e: Exception){
            Result.Error(e)
        }
    }

    suspend fun insertUser(user: User): Result<Boolean> {
        return try{
            Firebase.firestore.collection("users").document(user.uid).set(user).await()
            Result.Success(true)
        }catch (e: Exception){
            Result.Error(e)
            }
    }

    //I am aware that it's the same implementation, the only reason that I added a separate function is to help abstract the viewModel from the fact he's using firestore
    suspend fun updateUser(user: User): Result<Boolean> {
        return try{
            Firebase.firestore.collection("users").document(user.uid).set(user).await()
            Result.Success(true)
        }catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getUserByEmail(recipientEmail: String): Result<User?> {
        return try{
            val snapshot= Firebase.firestore.collection("users").whereEqualTo("email", recipientEmail).get().await()
            val user=snapshot.documents.first().toObject<User>()
            Result.Success(user)
        }catch (e: Exception){
            Result.Error(e)
        }
    }
}