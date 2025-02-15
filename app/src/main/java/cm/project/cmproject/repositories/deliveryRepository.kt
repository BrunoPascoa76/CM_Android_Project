package cm.project.cmproject.repositories

import cm.project.cmproject.models.Delivery
import com.google.firebase.Firebase
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestoreException
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
            Firebase.firestore.collection("pendingDeliveries")
                .document(delivery.deliveryId) //once they are accepted, they'll move from pendingDeliveries to regular deliveries
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

    suspend fun assignSelfToDelivery(
        deliveryId: String,
        userId: String
    ): Result<Boolean> {
        return try {
            val db = Firebase.firestore
            val deliveryRef = db.collection("deliveries").document(deliveryId)
            val pendingDeliveryRef = db.collection("pendingDeliveries").document(deliveryId)

            //to help avoid race conditions, this is run as a transaction
            db.runTransaction { transaction ->
                val deliverySnapshot = transaction.get(pendingDeliveryRef)

                if (deliverySnapshot.exists()) {
                    transaction.delete(pendingDeliveryRef)
                    var delivery = deliverySnapshot.toObject<Delivery>()

                    if (delivery != null) {
                        delivery = delivery.copy(
                            driverId = userId,
                            status = "Accepted",
                            completedSteps = 1
                        )
                        transaction.set(deliveryRef, delivery)
                    } else {
                        throw FirebaseFirestoreException(
                            "Delivery not found/already taken",
                            FirebaseFirestoreException.Code.ABORTED
                        )
                    }
                } else {
                    throw FirebaseFirestoreException(
                        "Delivery not found/already taken",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }
            }.await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}