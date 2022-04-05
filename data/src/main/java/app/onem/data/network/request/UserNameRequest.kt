package app.onem.data.network.request

import com.google.gson.annotations.SerializedName

data class UserNameRequest(
    @SerializedName("merchantUsername")
    val merchandiseUsername: String
)