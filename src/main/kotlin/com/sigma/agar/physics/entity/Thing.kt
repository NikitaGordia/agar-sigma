package com.sigma.agar.physics.entity

import com.sigma.agar.physics.World
import com.sigma.agar.utils.Utils
import com.sigma.agar.utils.Tuple

abstract class Thing(val color: Int, var mass: Float) {

    companion object {
        const val RATIO = 1.3f
        const val SNACK = 0
        const val BUBBLE = 2
        const val MIN_MASS = 1.0f
        const val MAX_MASS = 22500f
        const val DENSITY = 0.025f
        private var ids: Long = 0

        fun mass_to_radius(mass: Float, density: Float): Float = Math.sqrt(mass / (Math.PI * density)).toFloat()
    }

    abstract val type: Int

    abstract fun collide_with(thing: Thing)

    val id = ids++
    var world: World? = null
	val position = Tuple()
	val velocity = Tuple()
    val force = Tuple()
	var radius = 0f
    var matched = false
    var remove = false

    open fun update() {
        mass = Utils.clamp(mass, MIN_MASS, MAX_MASS)
        position.scalar_plus(velocity, (8 * Math.pow(mass.toDouble(), -0.439)).toFloat())
        box_search.update()
    }

    fun box_search() {
        calc_radius()
        with(position) {
            min.x = x - radius
            min.y = y - radius
            max.x = x + radius
            max.y = y + radius
        }
    }

    fun calc_radius() {
        radius = Utils.lerp(radius, mass_to_radius(mass, DENSITY), 0.5f)
    }
}