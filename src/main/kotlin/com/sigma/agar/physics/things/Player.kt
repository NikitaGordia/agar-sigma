package com.sigma.agar.physics.things

import com.sigma.agar.physics.World
import com.sigma.agar.utils.Tuple
import org.webutil.httpserver.Session

class Player(val session: Session, x: Float, y: Float, color: Int, world: World) {

    var maxMass = 0f
    var mass = 0f
    var screen_point = Tuple()
        private set

    val bubble = Bubble(this, color).apply {
        position[x] = y
        world.add(this)
    }

    fun update() {
        bubble.let {
            mass = it.mass
            if (mass > maxMass) {
                maxMass = mass
            }
        }
    }

    fun follow(worldPoint: Tuple?) {
        bubble.follow(worldPoint!!)
    }
}