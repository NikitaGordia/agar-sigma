package com.sigma.agar.utils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

class Serializer : DeflaterOutputStream(
    ByteArrayOutputStream(1024),
    Deflater(Deflater.DEFAULT_COMPRESSION),
    1024,
    false
) {

    @Throws(IOException::class)
    override fun close() {
        super.close()
        def.end()
    }

    @Throws(IOException::class)
    fun serialize(data: ByteArray, off: Int, len: Int): ByteArray {
        val byteArrayStream = out as ByteArrayOutputStream
        byteArrayStream.reset()
        def.reset()
        write(data, off, len)
        finish()
        return byteArrayStream.toByteArray()
    }
}