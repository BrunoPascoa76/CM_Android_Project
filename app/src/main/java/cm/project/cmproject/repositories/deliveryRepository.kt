package cm.project.cmproject.repositories

import cm.project.cmproject.models.Delivery
import com.google.firebase.Firebase
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class DeliveryRepository {
    suspend fun getDeliveryById(id: String): Result<Delivery?> {
        return try {
            val snapshot =
                Firebase.firestore.collection("deliveries").document(id).get().await()
            val delivery = snapshot.toObject<Delivery>()
            Result.Success(delivery)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun insertDelivery(delivery: Delivery): Result<Boolean> {
        return try {
            Firebase.firestore.collection("deliveries").document(delivery.deliveryId)
                .set(delivery).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateDelivery(delivery: Delivery): Result<Boolean> {
        return try {
            Firebase.firestore.collection("deliveries").document(delivery.deliveryId)
                .set(delivery).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAllByUserIdAndStatus(userId: String, status: String): Result<List<Delivery>> {
        return try {
            val snapshot = Firebase.firestore.collection("deliveries")
                .where(
                    Filter.or(
                        Filter.equalTo("driverId", userId),
                        Filter.equalTo("senderId", userId),
                        Filter.equalTo("recipientId", userId)
                    )
                )
                .whereEqualTo("status", status)
                //TODO: add optional (or not) sorting
                .get().await()
            Result.Success(snapshot.documents.mapNotNull { it.toObject<Delivery>() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getDeliveriesByDriverId(driverId: String): Result<List<Delivery>> {
        return try {
            val snapshot = Firebase.firestore.collection("deliveries")
                .whereEqualTo("driverId", driverId)
                .get().await()
            Result.Success(snapshot.documents.mapNotNull { it.toObject<Delivery>() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAllByUserIdAndStatus(
        userId: String,
        status: List<String>
    ): Result<List<Delivery>> {
        return try {
            val snapshot = Firebase.firestore.collection("deliveries")
                .where(
                    Filter.or(
                        Filter.equalTo("driverId", userId),
                        Filter.equalTo("senderId", userId),
                        Filter.equalTo("recipientId", userId)
                    )
                )
                .whereIn("status", status)
                .get().await()
            Result.Success(snapshot.documents.mapNotNull { it.toObject<Delivery>() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}