package com.sigma.agar.physics

import com.sigma.agar.Game
import com.sigma.agar.physics.entity.Bubble
import com.sigma.agar.physics.entity.Thing
import com.sigma.agar.utils.Tuple

open class World(
        width: Float,
        height: Float
) {
    companion object {
        const val TIME_UNIT = 1f / Game.FPS
        const val OVERSPILL = 0.05f
        const val FRACTION = 0.5f
        const val BOUNCINESS = 0.15f
        const val RESISTANCE = 0.95f
    }
//    -width / 2f,
//    -height / 2f,
//    width /2f,
//    height / 2f

    private val tmp_1 = Tuple()
    private val tmp_2 = Tuple()
    private val entities = mutableListOf<Thing>()
    private val iterator = MyIterator()

    fun step() {
        var thing: Thing
        var n = entities.size
        for (i in 0 until n) {
            thing = entities[i]
            if (thing.type == Thing.BUBBLE) {
                thing.matched = true
                iterator.thing_a = thing
                boxsearch.iterate(thing, iterator)
            }
            val x = thing.position.x + thing.velocity.x
            var diff = x - thing.radius
            if (diff < min.x) {
                thing.position.x += min.x - diff
                thing.velocity.x *= -BOUNCINESS
            }
            diff = x + thing.radius
            if (diff > max.x) {
                thing.position.x -= diff - max.x
                thing.velocity.x *= -BOUNCINESS
            }
            val y = thing.position.y + thing.velocity.y
            diff = y - thing.radius
            if (diff < min.y) {
                thing.position.y += min.y - diff
                thing.velocity.y *= -BOUNCINESS
            }
            diff = y + thing.radius
            if (diff > max.y) {
                thing.position.y -= diff - max.y
                thing.velocity.y *= -BOUNCINESS
            }
        }

        var i = 0
        while (i < n) {
            thing = entities[i]
            if (thing.remove) {
                remove(thing)
                i--
                n--
            } else {
                thing.run {
                    velocity.scalarProduct(RESISTANCE)
                    velocity.scalar_plus(force, TIME_UNIT)
                    force.reset()
                    update()
                    box_search.update(this)
                    matched = false
                }
            }
            i++
        }
    }

    fun add(thing: Thing) {
        thing.world = this
        entities.add(thing)
        box_search.update()
        box_search.insert(thing)
    }

    open fun remove(thing: Thing) {
        entities.remove(thing)
        box_search.remove(thing)
    }

    inner class MyIterator {
        var thing_a: Thing? = null

        override fun next(thing_b: Thing): Boolean {
            if (thing_a!!.remove) return false
            if (!thing_b.matched && !thing_b.remove) {
                if (thing_b.overlap(thing_a!!)) {
                    val d = tmp_1.set(thing_b.position).minus(thing_a!!.position)
                    val len_2 = d.pythagoras()
                    val radius = thing_a!!.radius + thing_b.radius
                    if (len_2 < radius * radius) {
                        val distance = StrictMath.sqrt(len_2.toDouble()).toFloat()
                        val penetration = radius - distance
                        val bubble_a = thing_a as Bubble?
                        when {
                            thing_b.type == Thing.SNACK -> {
                                thing_a!!.mass += thing_b.mass
                                thing_b.remove = true
                            }
                            thing_b.type == Thing.BUBBLE -> {
                                val bubble_b = thing_b as Bubble
                                if (bubble_b.parent === bubble_a!!.parent && (bubble_b.parent == null || bubble_b.split_time + bubble_a!!.split_time > 0f)) {
                                    val combined_mass = thing_a!!.mass + thing_b.mass
                                    if (distance > 0) {
                                        d.scalarProduct(1f / distance)
                                    } else {
                                        d[1f] = 0f
                                    }
                                    val rv = tmp_2.set(thing_b.velocity).minus(thing_a!!.velocity)
                                    val vn = rv.vectorProduct(d)
                                    if (vn < 0f) {
                                        val j = vn / combined_mass
                                        thing_a!!.velocity.scalar_plus(d, j * thing_b.mass)
                                        thing_b.velocity.scalar_minus(d, j * thing_a!!.mass)
                                    }
                                    val correction = Math.max(penetration - OVERSPILL, 0.0f) / combined_mass * FRACTION
                                    thing_a!!.position.scalar_minus(d, correction * thing_b.mass)
                                    thing_b.position.scalar_plus(d, correction * thing_a!!.mass)
                                } else if (penetration > Math.min(thing_a!!.radius, thing_b.radius)) {
                                    thing_a!!.collide_with(thing_b)
                                    thing_b.collide_with(thing_a!!)
                                }
                            }
                            penetration >= Math.min(thing_a!!.radius, thing_b.radius) * 1.25f -> {
                                thing_b.collide_with(thing_a!!)
                            }
                        }
                    }
                } else {
                    return false
                }
            }
            return true
        }
    }
}