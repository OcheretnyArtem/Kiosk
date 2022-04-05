package app.onem.domain.entities

import kotlin.math.roundToInt

private const val CENTS_PER_DOLLAR = 100

// handling each price as a number of cents
typealias Price = Int

fun Price.toDollars() = this.toDouble() / CENTS_PER_DOLLAR
fun doubleToPrice(dollars: Double): Price = (dollars * CENTS_PER_DOLLAR).roundToInt()
