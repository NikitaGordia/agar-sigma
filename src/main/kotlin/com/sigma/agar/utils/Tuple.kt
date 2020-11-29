package com.sigma.agar.utils

import kotlin.math.sqrt

class Tuple(
    var x: Float = 0f,
    var y: Float = 0f
) {

    constructor(t: Tuple) : this(t.x, t.y)

    fun set(t: Tuple): Tuple = set(t.x, t.y)

    operator fun set(x: Float, y: Float): Tuple = also {
        this.x = x
        this.y = y
    }

    fun plus(t: Tuple): Tuple = plus(t.x, t.y)

    fun plus(x: Float, y: Float): Tuple = also {
        this.x += x
        this.y += y
    }

    fun minus(t: Tuple): Tuple = minus(t.x, t.y)

    fun minus(x: Float, y: Float): Tuple = also {
        this.x -= x
        this.y -= y
    }

    fun scalarProduct(t: Float): Tuple = also {
        x *= t
        y *= t
    }

    fun reset(): Tuple = also {
        y = 0f
        x = y
    }

    fun vectorProduct(t: Tuple): Float = t.x * x + t.y * y

    fun hypot(): Float = sqrt((x * x + y * y).toDouble()).toFloat()

    fun pythagoras(): Float = x * x + y * y

    fun normalized(): Tuple = also {
        hypot().takeIf { it != 0f }?.let {
            x /= it
            y /= it
        }
    }

    fun rotate(radians: Float): Tuple {
        val cos = Math.cos(radians.toDouble()).toFloat()
        val sin = Math.sin(radians.toDouble()).toFloat()
        val new_x = x * cos - y * sin
        val new_y = x * sin + y * cos
        x = new_x
        y = new_y
        return this
    }

    fun scalar_plus(t: Tuple, scl: Float): Tuple = this.also {
        x += t.x * scl
        y += t.y * scl
    }

    fun scalar_minus(t: Tuple, scl: Float): Tuple = this.also {
        x -= t.x * scl
        y -= t.y * scl
    }

    fun set_random(): Tuple {
        x = Utils.random(Float.MIN_VALUE, Float.MAX_VALUE)
        y = Utils.random(Float.MIN_VALUE, Float.MAX_VALUE)
        return this
    }

    override fun toString(): String = "Vec2 [x=$x, y=$y]"
}
