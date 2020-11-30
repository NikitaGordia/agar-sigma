package com.sigma.agar.physics

import com.sigma.agar.Game
import com.sigma.agar.physics.things.Bubble
import com.sigma.agar.physics.things.Thing
import com.sigma.agar.utils.Tuple
import com.sigma.agar.utils.Utils.overlap

open class World(
        val game: Game,
        width: Float,
        height: Float
) {
    companion object {
        const val TIME_UNIT = 1f / Game.FPS
        const val BOUNCINESS = 0.15f
        const val RESISTANCE = 0.95f
    }

    val hw = width / 2
    val hh = height / 2

    private val cache = Tuple()
    private val things = mutableListOf<Thing>()

    fun step() {
        var thing: Thing
        var n = things.size

        for (i in 0 until n) {
            thing = things[i]
            if (thing.type == Thing.BUBBLE) {
                thing.matched = true
                val thing1 = thing
                things.forEachIndexed { index, thing2 ->
                    if (index != i && overlap(thing1, thing2)) {
                        if (!process_overlapping(thing1, thing2)) {
                            return@forEachIndexed
                        }
                    }
                }
            }
            val x = thing.position.x + thing.velocity.x
            var diff = x - thing.radius
            if (diff < -hw) {
                thing.position.x += -hw - diff
                thing.velocity.x *= -BOUNCINESS
            }
            diff = x + thing.radius
            if (diff > hw) {
                thing.position.x -= diff - hw
                thing.velocity.x *= -BOUNCINESS
            }
            val y = thing.position.y + thing.velocity.y
            diff = y - thing.radius
            if (diff < -hh) {
                thing.position.y += -hh - diff
                thing.velocity.y *= -BOUNCINESS
            }
            diff = y + thing.radius
            if (diff > hh) {
                thing.position.y -= diff - hh
                thing.velocity.y *= -BOUNCINESS
            }
        }

        var i = 0
        while (i < n) {
            thing = things[i]
            if (thing.remove) {
                if (thing is Bubble) {
                    game.rejoin(thing.player.session)
                }
                remove(thing)
                i--
                n--
            } else {
                thing.run {
                    velocity.scalarProduct(RESISTANCE)
                    velocity.scalar_plus(force, TIME_UNIT)
                    force.reset()
                    update()
                    Game.chunks.update(this)
                    matched = false
                }
            }
            i++
        }
    }

    fun add(thing: Thing) {
        thing.world = this
        things.add(thing)
        Game.chunks.update(thing)
    }

    open fun remove(thing: Thing) {
        things.remove(thing)
        Game.chunks.remove(thing)
    }

    fun process_overlapping(thing1: Thing, thing2: Thing): Boolean {
        if (thing1.remove) return false
        if (!thing2.matched && !thing2.remove) {
            val d = cache.set(thing2.position).minus(thing1.position)
            val len2: Float = d.pythagoras()
            val radius: Float = thing1.radius + thing2.radius
            if (len2 < radius * radius) { // Collision test
                val distance = StrictMath.sqrt(len2.toDouble()).toFloat()
                val penetration = radius - distance
                if (thing2.type == Thing.SNACK) {
                    thing1.mass += thing2.mass
                    thing2.remove = true
                } else if (thing2.type == Thing.BUBBLE) {
                    if (penetration > Math.min(thing1.radius, thing2.radius)) {
                        thing1.collide_with(thing2)
                        thing2.collide_with(thing1)
                    }
                }
            }
        }
        return true
    }
}