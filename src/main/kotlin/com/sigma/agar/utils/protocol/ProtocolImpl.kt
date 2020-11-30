package com.sigma.agar.utils.protocol

import com.sigma.agar.GameWebSocket
import com.sigma.agar.physics.things.Thing
import com.sigma.agar.utils.Serializer
import com.sigma.agar.utils.Tuple
import org.webutil.httpserver.Session
import java.util.concurrent.Executors

class ProtocolImpl(private val socket: GameWebSocket) : Protocol {

    private val serializer = Serializer()
    private val executor = Executors.newFixedThreadPool(1)

    companion object {
        fun write_int(arr: ByteArray, offset: Int, value: Int): Int {
            arr[offset] = (0xFF and (value ushr 24)).toByte()
            arr[offset + 1] = (0xFF and (value ushr 16)).toByte()
            arr[offset + 2] = (0xFF and (value ushr 8)).toByte()
            arr[offset + 3] = (0xFF and value).toByte()

            val offset_four_bytes = 4
            return offset_four_bytes
        }
    }

    override fun dispatch(center: Tuple, width: Float, height: Float, things: List<Thing>, end_time_ms: Long, s: Session) {
        var arr = ByteArray(1024 + 512)
        var bit_offset = 0
        bit_offset += write_int(arr, bit_offset, center.x.toInt())
        bit_offset += write_int(arr, bit_offset, center.y.toInt())
        bit_offset += write_int(arr, bit_offset, width.toInt())
        bit_offset += write_int(arr, bit_offset, height.toInt())

        for (thing in things) {
            arr = ensureCapacity(arr, bit_offset, 16)
            var info = thing.color and 0xF shl 1
            if (thing.type == Thing.BUBBLE) {
                info = info or 1
            }
            bit_offset += write_int(arr, bit_offset, info)
            bit_offset += write_int(arr, bit_offset, Math.ceil(thing.radius.toDouble()).toInt())
            bit_offset += write_int(arr, bit_offset, thing.position.x.toInt())
            bit_offset += write_int(arr, bit_offset, thing.position.y.toInt())
        }

        bit_offset += write_int(arr, bit_offset, (end_time_ms / 1000).toInt())

        println(bit_offset)

        dispatch_async(arr, bit_offset, s)
    }

    private fun dispatch_async(arr: ByteArray, offset: Int, s: Session) {
        executor.execute {
            try {
                val compressed = serializer.serialize(arr, 0, offset)
                socket.sendData(compressed, s)
            } catch (_: Throwable) {
                socket.close_session(s)
            }
        }
    }

    private fun ensureCapacity(arr: ByteArray, offset: Int, length: Int): ByteArray {
        var arr = arr
        while (arr.size - offset < length) {
            val tmp = ByteArray((arr.size * 1.75).toInt())
            System.arraycopy(arr, 0, tmp, 0, arr.size)
            arr = tmp
        }
        return arr
    }
}