package cm.project.cmproject.models

data class Dimensions(
    val length: Double = 0.0,
    val width: Double = 0.0,
    val height: Double = 0.0
)

data class Parcel(
    val parcelId: Int = 0,
    val label: String = "",
    val isFragile: Boolean = false,
    val weight: Double = 0.0,
    val dimensions: Dimensions = Dimensions()
)
