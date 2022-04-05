package app.onem.domain.utils

import kotlin.random.Random

//TODO Move to entities package after release
data class Shop(
    val id: String? = null,
    val value: String? = null,
    val code: String? = null
) {

    override fun hashCode(): Int {
        // Fix for correct Coroutines Flow work
        return Random.nextInt(Int.MAX_VALUE)
    }
}

fun Shop?.isTestShop(): Boolean =
    this?.code == Shops.testShop.code

class Shops {
    companion object {
        val testShop
            get() = Shop(
                id = "test",
                value = "TeaShop",
                code = "test"
            )
    }
}
