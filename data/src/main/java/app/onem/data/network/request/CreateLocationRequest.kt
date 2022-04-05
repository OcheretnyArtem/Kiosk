package app.onem.data.network.request

import com.google.gson.annotations.SerializedName

data class CreateLocationRequest(
    @SerializedName("display_name")
    val displayName: String,
    val address: Map<String, String>,
    val merchantUsername: String
)
