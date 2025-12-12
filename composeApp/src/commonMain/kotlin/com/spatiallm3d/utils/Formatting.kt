package com.spatiallm3d.utils

import kotlin.math.pow
import kotlin.math.round

fun Float.format(decimals: Int): String {
    val multiplier = 10.0.pow(decimals).toFloat()
    val rounded = round(this * multiplier) / multiplier
    return rounded.toString()
}

fun Double.format(decimals: Int): String {
    val multiplier = 10.0.pow(decimals)
    val rounded = round(this * multiplier) / multiplier
    return rounded.toString()
}
