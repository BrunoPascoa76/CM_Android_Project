package cm.project.cmproject.models

data class User(
    val uid:String,
    val givenName:String,
    val email: String,
    val phoneNumber: String,
    val role: String
)