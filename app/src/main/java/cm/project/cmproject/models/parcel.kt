package cm.project.cmproject.models

data class Dimensions(
    val length: String = "",
    val width: String = "",
    val height: String = ""
)

data class Parcel(
    val parcelId: String = "",
    val label: String = "",
    val isFragile: Boolean = false,
    val weight: String = "",
    val dimensions: Dimensions = Dimensions()
)