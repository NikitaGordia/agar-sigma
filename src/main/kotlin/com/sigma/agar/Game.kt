package com.sigma.agar

import com.sigma.agar.physics.ChunkController
import com.sigma.agar.physics.World
import com.sigma.agar.physics.things.Player
import com.sigma.agar.physics.things.Snack
import com.sigma.agar.physics.things.Thing
import com.sigma.agar.utils.GameLoop
import com.sigma.agar.utils.GameLoop.GameLoopListener
import com.sigma.agar.utils.Utils
import com.sigma.agar.utils.Tuple
import com.sigma.agar.utils.protocol.Protocol
import org.webutil.httpserver.Session
import java.util.*

class Game : GameLoop(), GameLoopListener {

    companion object {
        const val FPS = 60
        const val WORLD_WIDTH = 1500
        const val WORLD_HEIGHT = 1500
        const val TOTAL_COLORS = 10
        const val INIT_FOOD_COUNT = 300
        const val POV_WIDTH = 500
        const val POV_HEIGHT = 500
        val chunks = ChunkController(
                pov_width = POV_WIDTH,
                pov_height = POV_HEIGHT,
                world_height = WORLD_HEIGHT,
                world_width = WORLD_WIDTH
        )
    }

    init {
        fps = FPS
        listener = this
        create()
    }

    var protocol_data: Protocol? = null

    private val sessions = mutableSetOf<Session>()

    private val cache2 = Tuple()
    private var time_accumulator = 0f
    private var world: World? = null
    private val post_requests: MutableList<Runnable> = ArrayList()
    private val game_map: MutableMap<Long, Player> = HashMap()

    private fun create() {
        val random = Utils.random
        val half_width = WORLD_WIDTH / 2
        val half_height = WORLD_HEIGHT / 2
        val width = WORLD_WIDTH
        val height = WORLD_HEIGHT
        world = object : World(this@Game, WORLD_WIDTH.toFloat(), WORLD_HEIGHT.toFloat()) {
            override fun remove(thing: Thing) {
                super.remove(thing)
                if (thing.type == Thing.SNACK) {
                    val _thing: Thing = Snack(random.nextInt(TOTAL_COLORS))
                    _thing.position[random.nextInt(width) - half_width.toFloat()] = random.nextInt(height) - half_height.toFloat()
                    world!!.add(_thing)
                }
            }
        }
        var thing: Thing
        for (i in 0 until INIT_FOOD_COUNT) {
            thing = Snack(random.nextInt(TOTAL_COLORS))
            thing.position[random.nextInt(width) - half_width.toFloat()] = random.nextInt(height) - half_height.toFloat()
            world!!.add(thing)
        }
    }

    override fun update(dt: Float) {
        val time_frame = Math.max(dt, 0.05f)
        time_accumulator += time_frame
        while (time_accumulator >= World.TIME_UNIT) {
            world!!.step()
            time_accumulator -= World.TIME_UNIT
        }
        synchronized(post_requests) {
            post_requests.forEach { it.run() }
            post_requests.clear()
        }
    }

    override fun render() {
        for (player in game_map.values) {
            player.update()

            val things = chunks.get_neighbours(player)

            protocol_data?.dispatch(
                    player.bubble.position,
                    POV_WIDTH.toFloat(),
                    POV_HEIGHT.toFloat(),
                    things,
                    round_end_time_ms - System.currentTimeMillis(),
                    player.session!!
            )

            player.run {
                follow(cache2.set(screen_point.x, screen_point.y).plus(bubble.position))
            }
        }
    }

    override fun reset_round() {
        sessions.forEach {
            unjoin(it)
        }
        sessions.forEach {
            join(it)
        }
    }

    fun add_post_runnable(runnable: () -> Unit) {
        synchronized(post_requests) { post_requests.add(runnable) }
    }

    fun join(session: Session) {
        val half_width = 2500
        val half_height = 2500
        val width = half_width * 2
        val height = half_height * 2
        val random = Utils.random
        val x = random.nextInt(width) - half_width.toFloat()
        val y = random.nextInt(height) - half_height.toFloat()
        val color = random.nextInt(TOTAL_COLORS)
        add_post_runnable {
            sessions += session
            val player = Player(session, x, y, color, world!!)
            session.userData = player
            game_map[session.id] = player
        }
    }

    fun unjoin(session: Session) {
        session.userData = null
        add_post_runnable {
            sessions += session
            game_map.remove(session.id)?.let {
                world?.remove(it.bubble)
            }
        }
    }

    fun rejoin(session: Session) {
        unjoin(session)
        join(session)
    }

    fun on_mouse_moved(x: Int, y: Int, session: Session) {
        (session.userData as? Player?)?.screen_point?.set(x.toFloat(), y.toFloat())
    }
}