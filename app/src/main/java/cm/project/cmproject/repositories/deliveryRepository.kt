package cm.project.cmproject.repositories

import cm.project.cmproject.models.Delivery
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class DeliveryRepository{
    suspend fun getDeliveryById(id: Int): Result<Delivery?> {
        return try {
            val snapshot =
                Firebase.firestore.collection("deliveries").document(id.toString()).get().await()
            val delivery = snapshot.toObject<Delivery>()
            Result.Success(delivery)
        }catch (e: Exception){
            Result.Error(e)
        }
    }

    suspend fun insertDelivery(delivery: Delivery): Result<Boolean> {
        return try {
            Firebase.firestore.collection("deliveries").document(delivery.deliveryId.toString()).set(delivery).await()
            Result.Success(true)
        }catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateDelivery(delivery: Delivery): Result<Boolean> {
        return try {
            Firebase.firestore.collection("deliveries").document(delivery.deliveryId.toString()).set(delivery).await()
            Result.Success(true)
        }catch (e: Exception) {
            Result.Error(e)
        }
    }
}