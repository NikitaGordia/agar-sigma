package com.sigma.agar.physics.things

class Snack(color: Int) : Thing(color, INIT_MASS) {

    companion object {
        const val INIT_MASS = 1f
    }

    override fun collide_with(thing: Thing) {
        // no implementation
    }

    override val type =  SNACK

    override val density = 0.025f
}