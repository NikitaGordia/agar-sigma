package com.sigma.agar

import org.webutil.httpserver.HttpRequest
import org.webutil.httpserver.HttpResponse
import org.webutil.httpserver.HttpServerImpl
import org.webutil.httpserver.webserver.WebServerHandler
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*


object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val socket = run_socket()
        val server = run_server()

        Scanner(System.`in`).use(Scanner::nextLine)

        socket.disconnect()
        server.disconnect()
    }

    private fun run_server() = HttpServerImpl(8080).apply {
        httpListener = object : WebServerHandler() {
            override fun handleQuery(p0: HttpRequest?): HttpResponse {
                return HttpResponse.build(HttpResponse.Status.NOT_FOUND)
            }

            @Throws(Exception::class)
            override fun toStream(file: File): InputStream {
                return FileInputStream(file)
            }
        }.apply {
            contentFolder = "web"
            indexMap["/"] = "/index.html"
        }
        connect()
    }

    private fun run_socket() = HttpServerImpl(8081).apply {
        httpListener = GameWebSocket()
        connect()
    }
}