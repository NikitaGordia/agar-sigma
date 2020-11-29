package com.sigma.agar.utils

open class GameLoop {
    var current_fps = 0
        private set
    var listener: GameLoopListener? = null

    private var thread: Thread? = null
    var fps: Int = 60
        set(value) {
            if (value < 1) throw RuntimeException("Fps must be greater than 0, fps: $fps")
            field = value
        }

    @Synchronized
    fun start() {
        if (!isRunning) {
            thread = object : Thread(GameLoop::class.java.name) {
                override fun run() {
                    try {
                        val desired_time = 1000000000 / fps.toLong()
                        var start = System.nanoTime()
                        var last = start
                        var fps_time: Long = 0
                        var frames = 0
                        while (!isInterrupted) {
                            last = start
                            start = System.nanoTime()
                            val sl = start - last
                            frames++
                            fps_time += sl
                            if (fps_time > 1000000000) {
                                current_fps = frames
                                frames = 0
                                fps_time = 0
                            }
                            val dt = sl * 0.000000001f
                            listener?.update(dt)
                            listener?.render()
                            val diff = ((desired_time - (System.nanoTime() - start)) * 0.000001f).toLong()
                            if (diff > 0) {
                                sleep(diff)
                            }
                        }
                    } catch (_: Exception) {
                    } finally {
                        this@GameLoop.stop()
                    }
                }
            }
            thread?.start()
        }
    }

    @get:Synchronized
    val isRunning: Boolean
        get() = thread?.isInterrupted == false

    @Synchronized
    fun stop() {
        if (isRunning) {
            try {
                thread?.interrupt()
            } finally {
                thread = null
            }
        }
    }

    interface GameLoopListener {
        fun update(dt: Float)
        fun render()
    }
}