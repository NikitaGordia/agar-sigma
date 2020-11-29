package com.sigma.agar.physics.entity

import com.sigma.agar.physics.World
import com.sigma.agar.utils.Utils
import com.sigma.agar.utils.Tuple

class Bubble(color: Int) : Thing(color, INIT_MASS) {

    var split_time = 0f
    var parent: Player? = null

    private var tick = 0
    private var thinness = 0f

    companion object {
        const val FORCE = 5f
        const val INIT_MASS = 10f
        const val COMMON_MASS = 10f
        const val LEAST_SPLITTABLE_MASS = 35f
        const val THINNESS = 0.003f
        const val INIT_TIME = 30f

        private val tmp = Tuple()

        fun get_split_time(mass: Float): Float = get_split_time(INIT_TIME, mass)

        fun get_split_time(split_time: Float, mass: Float): Float = split_time + mass * 0.02333f
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
        if (!remove) {
            val bubble = thing as Bubble
            when {
                bubble.parent === parent -> {
                    mass += thing.mass
                    split_time = get_split_time(split_time, thing.mass)
                    parent!!.children.remove(thing)
                    parent!!.can_eat_spikes = false
                    thing.remove = true
                }
                parent != null -> {
                    val ratio = mass / thing.mass
                    if (ratio > RATIO) {
                        mass += thing.mass
                        split_time = get_split_time(split_time, thing.mass)
                        if (bubble.parent != null) {
                            bubble.parent!!.children.remove(thing)
                        }
                        thing.remove = true
                    }
                }
            }
        }
    }

    fun split(nx: Float, ny: Float, new_mass: Float = mass * 0.5f): Boolean {
        if (!remove) {
            val diff = mass - new_mass
            if (new_mass >= INIT_MASS && diff >= INIT_MASS) {
                val bubble = Bubble(1)
                mass = diff
                split_time = get_split_time(mass)
                bubble.mass = new_mass
                bubble.split_time = get_split_time(bubble.mass)
                bubble.force.set(nx, ny).scalarProduct((1.0 / (8 * Math.pow(bubble.mass.toDouble(), -0.439)) * 380).toFloat())
                bubble.position.x = position.x + nx
                bubble.position.y = position.y + ny
                parent!!.children.add(bubble)
                bubble.parent = parent
                world!!.add(bubble)
                return true
            }
        }
        return false
    }

    fun share(x: Float, y: Float) {
        if (mass - COMMON_MASS >= LEAST_SPLITTABLE_MASS) {
            val food = Bubble(1)
            mass -= COMMON_MASS.also { food.mass = it }
            val rnd = Utils.random(-0.2f, 0.2f)
            val dx = x - position.x
            val dy = y - position.y
            tmp.set(dx, dy).normalized()
            food.force.set(tmp).rotate(rnd).scalarProduct((1.0 / (8.0 * Math.pow(food.mass.toDouble(), -0.439)) * 380).toFloat())
            tmp.scalarProduct(mass_to_radius(mass, DENSITY) + mass_to_radius(food.mass, DENSITY))
            food.position.set(position).plus(tmp)
            world!!.add(food)
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
}