package app.onem.data.model

import app.onem.domain.utils.Shop
import com.google.gson.annotations.SerializedName

data class MerchantModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("officialName")
    val officialName: String,
    @SerializedName("username")
    val username: String
)

internal fun MerchantModel.toShop(): Shop =
    Shop(
        id = id,
        value = officialName,
        code = username
    )