package com.sigma.agar.physics.things

import com.sigma.agar.physics.World
import com.sigma.agar.utils.Tuple

class Bubble(val player: Player, color: Int) : Thing(color, INIT_MASS) {

    var split_time = 0f

    private var tick = 0
    private var thinness = 0f

    companion object {
        const val FORCE = 5f
        const val INIT_MASS = 50f
        const val THINNESS = 0.003f
    }

    override val type = BUBBLE

    override fun update() {
        super.update()
        if (mass > INIT_MASS) {
            tick++
            if (tick > 60) {
                tick = 0
                thinness += mass * THINNESS
            }
            if (thinness - 1f > 0f) {
                thinness--
                mass--
            }
        }
        if (split_time > 0f) {
            split_time -= World.TIME_UNIT
        }
    }

    override fun collide_with(thing: Thing) {
        thing as Bubble
        if (!remove) {
            val ratio = mass / thing.mass
            if (ratio > RATIO) {
                mass += thing.mass
                thing.remove = true
            }
        }
    }

    fun follow(world_point: Tuple) {
        var dx = world_point.x - position.x
        var dy = world_point.y - position.y
        val len2 = dx * dx + dy * dy
        if (len2 != 0f) {
            val distance = (1f / StrictMath.sqrt(len2.toDouble())).toFloat()
            dx *= distance
            dy *= distance
        }
        force.plus(dx * FORCE, dy * FORCE)
    }

    override val density = 0.25f
}