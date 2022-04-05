package app.onem.data.network.request

import com.google.gson.annotations.SerializedName

data class RegisterReaderRequest(
    val label: String,
    @SerializedName("registration_code")
    val registrationCode: String,
    val location: String,
    val merchantUsername: String
)
