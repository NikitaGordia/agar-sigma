package com.sigma.agar.physics.entity

class Snack(color: Int) : Thing(color, INIT_MASS) {

    companion object {
        const val INIT_MASS = 1f
    }

    override fun collide_with(thing: Thing) {
        // no implementation
    }

    override val type =  SNACK
}