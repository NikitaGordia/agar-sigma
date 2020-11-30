package com.sigma.agar.physics

import com.sigma.agar.physics.things.Player
import com.sigma.agar.physics.things.Thing
import kotlin.math.max
import kotlin.math.min

class ChunkController(
        pov_width: Int,
        pov_height: Int,
        private val world_width: Int,
        private val world_height: Int,
) {
    private val chunk_search_radius = 1

    private val chunk_size_width: Int = pov_width / 2
    private val chunk_size_height: Int = pov_height / 2

    private val chunks = mutableMapOf<String, MutableSet<Int>>()
    private val tags = mutableMapOf<Int, Thing>()

    fun update(thing: Thing) {
        val id = thing.id
        val chunk = get_chunk(thing.position.x, thing.position.y)
        tags[id]?.let { pt ->
            pt.position.set(thing.position)
            pt.velocity.set(thing.velocity)
            pt.force.set(thing.force)
            pt.radius = thing.radius
            pt.matched = thing.matched
            pt.remove = thing.remove
            if (pt.chunk != chunk) {
                chunks[pt.chunk]?.remove(id)
                update_chunk(chunk, thing)
            }
        } ?: run {
            tags[id] = thing
            update_chunk(chunk, thing)
        }
    }

    fun remove(thing: Thing) {
        val id = thing.id
        tags[id]?.let {
            chunks[it.chunk]?.remove(id)
        }
        tags.remove(id)
    }

    fun get_neighbours(player: Player): List<Thing> {
        val thing = tags[player.bubble.id] ?: return player.bubble.position.run { get_neighbours(x, y) }

        return thing.position.run {
            get_neighbours(x, y)
        }
    }

    fun get_neighbours(x: Float, y: Float): List<Thing> {
        val res = mutableListOf<Thing>()

        val chunk_x = ((x + world_width / 2) / chunk_size_width).toInt()
        val chunk_y = ((y + world_height / 2) / chunk_size_height).toInt()

        for (i in max(0, chunk_x - chunk_search_radius) .. min(chunk_x + chunk_search_radius, world_width / chunk_size_width))
            for (j in max(0, chunk_y - chunk_search_radius) .. min(chunk_y + chunk_search_radius, world_height / chunk_size_height)) {
                chunks[get_chunk(i, j)]?.let {
                    res.addAll(it.mapNotNull { id -> tags[id] })
                }
            }

        return res
    }

    private fun update_chunk(chunk: String, thing: Thing) {
        if (chunk != thing.chunk) {

            chunks[chunk]?.remove(thing.id)

            thing.chunk = chunk

            chunks[chunk]?.add(thing.id) ?: run {
                chunks[chunk] = mutableSetOf(thing.id)
            }
        }
    }

    private fun get_chunk(x: Float, y: Float): String {
        val chunk_x = ((x + world_width / 2) / chunk_size_width).toInt()
        val chunk_y = ((y + world_height / 2) / chunk_size_height).toInt()
        return get_chunk(chunk_x, chunk_y)
    }

    private fun get_chunk(i: Int, j: Int): String {
        return "${i}x${j}"
    }
}