package com.sigma.agar.utils

import java.util.*

object Utils {
    val random = Random(System.nanoTime())

    fun lerp(startValue: Float, endValue: Float, scale: Float): Float = startValue + (endValue - startValue) * scale

    fun random(min: Float, max: Float): Float = random.nextFloat() * (max - min) + min

    fun clamp(value: Float, min: Float, max: Float): Float = when {
        value < min -> min
        value > max -> max
        else -> value
    }
}