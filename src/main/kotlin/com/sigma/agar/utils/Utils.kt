package com.sigma.agar.utils

import com.sigma.agar.physics.things.Thing
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
    
    fun overlap(thing1: Thing, thing2: Thing): Boolean {
        val radius1 = thing1.radius
        val position1 = thing1.position
        
        val minx1 = position1.x - radius1
        val maxx1 = position1.x + radius1
        val miny1 = position1.y - radius1
        val maxy1 = position1.y + radius1

        val radius2 = thing2.radius
        val position2 = thing2.position

        val minx2 = position2.x - radius2
        val maxx2 = position2.x + radius2
        val miny2 = position2.y - radius2
        val maxy2 = position2.y + radius2

        return minx1 < maxx2 &&
                miny1 < maxy2 &&
                maxx1 > minx2 &&
                maxy1 > miny2
    }
}