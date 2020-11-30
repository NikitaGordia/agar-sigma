package com.sigma.agar

import com.google.gson.Gson
import com.sigma.agar.utils.protocol.ProtocolImpl
import org.webutil.httpserver.Session
import org.webutil.httpserver.websocket.WebSocketHandler

class GameWebSocket : WebSocketHandler() {

    init {
        super.getIndexSet() += "/sigma/agar"
    }

    private val game = Game().apply {
        protocol_data = ProtocolImpl(this@GameWebSocket)
        start()
    }
    private val gson = Gson()

    override fun onOpen(s: Session) {
        game.join(s)
    }

    override fun onClose(s: Session) {
        game.unjoin(s)
    }

    override fun onData(data: ByteArray, size: Int, s: Session) {}
    override fun onText(text: String, s: Session) {
        try {
            val msg = gson.fromJson(text, Message::class.java)
            game.on_mouse_moved(msg.x.toInt(), msg.y.toInt(), s)
        } catch (e: Throwable) {
            close_session(s)
        }
    }

    fun close_session(s: Session) {
        runCatching { s.close() }
    }
}