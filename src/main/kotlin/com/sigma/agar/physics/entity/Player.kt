package com.sigma.agar.physics.entity

import com.sigma.agar.physics.World
import com.sigma.agar.utils.Tuple
import org.web.httpserver.Session
import java.util.*

class Player(x: Float, y: Float, color: Int, world: World) {

    companion object {
        const val UPDATE_TIME: Long = 100
        const val SLIT_LIMIT = 16
    }

    val children = LinkedList<Bubble>()
    var maxMass = 0f
    var mass = 0f
    var can_eat_spikes = false
    var session: Session? = null
    var screen_point = Tuple()
        private set

    init {
        Bubble(1).run {
            parent = this@Player
            children.add(this)
            position[x] = y
            world.add(this)
        }
    }

    fun update() {
        when {
            children.size > 1 -> {
                min[Float.POSITIVE_INFINITY] = Float.POSITIVE_INFINITY
                max[Float.NEGATIVE_INFINITY] = Float.NEGATIVE_INFINITY
                mass = 0f
                var x: Float
                var y: Float
                for (bubble in children) {
                    x = bubble.position.x
                    y = bubble.position.y
                    when {
                        x < min.x -> min.x = x
                        x > max.x -> max.x = x
                    }
                    when {
                        y < min.y -> min.y = y
                        y > max.y -> max.y = y
                    }
                    mass += bubble.mass
                }
                if (mass > maxMass) {
                    maxMass = mass
                }
            }
            children.size > 0 -> {
                children.first().let {
                    min[it.position.x - it.radius] = it.position.y - it.radius
                    max[it.position.x + it.radius] = it.position.y + it.radius
                    mass = it.mass
                    if (mass > maxMass) {
                        maxMass = mass
                    }
                }
            }
        }
    }

    fun share(tuple: Tuple) {
        children.forEach {
            it.share(tuple.x, tuple.y)
        }
    }

    fun follow(worldPoint: Tuple?) {
        children.forEach {
            it.follow(worldPoint!!)
        }
    }

    fun split() {
        if (children.size < SLIT_LIMIT) {
            val tmp = Tuple(children.first.velocity).normalized()
            children.forEach {
                it.split(tmp.x, tmp.y)
            }
            if (children.size >= SLIT_LIMIT) {
                can_eat_spikes = true
            }
        }
    }
}