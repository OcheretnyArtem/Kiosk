package app.onem.kioskandroid.utils

import app.onem.domain.entities.Price
import app.onem.domain.entities.toDollars

fun Price.format() = "\$ ${(toDollars()).format(decimalPlaces = 2)}"
