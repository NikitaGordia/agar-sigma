package com.sigma.agar.physics.things

import com.sigma.agar.physics.World
import com.sigma.agar.utils.IdGenerator
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
    }

    abstract val type: Int

    abstract fun collide_with(thing: Thing)

    abstract val density: Float

    fun mass_to_radius(): Float {
        return Math.sqrt(mass /(density * Math.PI)).toFloat()
    }

    val id = IdGenerator.getNewId()
    var chunk: String = "null"
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
        radius = Utils.lerp(radius, mass_to_radius(), 0.5f)
    }
}