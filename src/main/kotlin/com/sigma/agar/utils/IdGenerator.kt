package com.sigma.agar.utils

object IdGenerator {
    private var count = 0

    @Synchronized
    fun getNewId(): Int {
        return count++
    }
}