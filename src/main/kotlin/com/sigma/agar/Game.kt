package com.sigma.agar

import com.sigma.agar.physics.World
import com.sigma.agar.physics.entity.Player
import com.sigma.agar.physics.entity.Snack
import com.sigma.agar.physics.entity.Thing
import com.sigma.agar.utils.GameLoop
import com.sigma.agar.utils.GameLoop.GameLoopListener
import com.sigma.agar.utils.Utils
import com.sigma.agar.utils.Tuple
import com.sigma.agar.utils.protocol.Protocol
import org.web.httpserver.Session
import java.util.*

class Game : GameLoop(), GameLoopListener {

    companion object {
        const val FPS = 60
        const val WORLD_WIDTH = 2500
        const val WORLD_HEIGHT = 2500
        const val TOTAL_COLORS = 10
        const val INIT_FOOD_COUNT = 2000
    }

    init {
        fps = FPS
        listener = this
        create()
    }

    private val cache1 = Tuple()
    private val cache2 = Tuple()
    private var time_accumulator = 0f
    private var world: World? = null
    private val post_requests: MutableList<Runnable> = ArrayList()
    private val game_map: MutableMap<Long, Player> = HashMap()
    private var protocol_data: Protocol? = null

    private fun create() {
        val random = Utils.random
        val half_width = WORLD_WIDTH / 2
        val half_height = WORLD_HEIGHT / 2
        val width = WORLD_WIDTH
        val height = WORLD_HEIGHT
        world = object : World(WORLD_WIDTH.toFloat(), WORLD_HEIGHT.toFloat()) {
            override fun remove(thing: Thing) {
                super.remove(thing)
                if (thing.type == Thing.SNACK) {
                    val _thing: Thing = Snack(0)
                    _thing.position[random.nextInt(width) - half_width.toFloat()] = random.nextInt(height) - half_height.toFloat()
                    world!!.add(_thing)
                }
            }
        }
        var thing: Thing
        for (i in 0 until INIT_FOOD_COUNT) {
            thing = Snack(0)
            thing.position[random.nextInt(width) - half_width.toFloat()] = random.nextInt(height) - half_height.toFloat()
            world?.add(thing)
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
            post_requests.forEach(Runnable::run)
            post_requests.clear()
        }
    }

    override fun render() {
        val things: MutableList<Thing> = ArrayList()
        var size: Float
        for (player in game_map.values) {
            player.run {
                update()
                get_center(cache1)
            }
            box_search.set(player)
            size = 200 + box_search.area * 0.0001f
            box_search.run {
                min.minus(size, size)
                max.plus(size, size)
            }
            world!!.box_search.query(aabb, things)
            if (things.isNotEmpty()) {
                box_search.run {
                    protocol_data!!.dispatch(cache1, width, height, things, player.session)
                }
                things.clear()
            }
            player.run {
                follow(cache2.set(screen_point.x, screen_point.y).plus(cache1))
            }
        }
    }

    fun add_post_runnable(runnable: () -> Unit) {
        synchronized(post_requests) { post_requests.add(runnable) }
    }

    fun join_player(session: Session) {
        val half_width = world!!.max.x.toInt()
        val half_height = world!!.max.y.toInt()
        val width = half_width * 2
        val height = half_height * 2
        val random = Utils.random
        val x = random.nextInt(width) - half_width.toFloat()
        val y = random.nextInt(height) - half_height.toFloat()
        val color = random.nextInt(TOTAL_COLORS)
        add_post_runnable {
            val player = Player(x, y, color, world!!)
            player.session = session
            session.userData = player
            game_map[session.id] = player
        }
    }

    fun unjoin(session: Session) {
        session.userData = null
        add_post_runnable {
            val player = game_map.remove(session.id)
            player?.session = null
        }
    }

    fun on_key_pressed(keyCode: Int, down: Boolean, session: Session) {
        if (down) {
            when (keyCode) {
                87 -> add_post_runnable {
                    (session.userData as? Player?)?.run {
                        share(center.plus(screen_point))
                    }
                }
                32 -> add_post_runnable {
                    (session.userData as? Player?)?.split()
                }
            }
        }
    }

    fun on_mouse_moved(x: Int, y: Int, session: Session) {
        (session.userData as? Player?)?.screen_point?.set(x.toFloat(), y.toFloat())
    }

    fun set_protocol_data(protocolData: Protocol?) {
        this.protocol_data = protocolData
    }
}