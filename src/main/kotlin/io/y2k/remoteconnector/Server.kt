package io.y2k.remoteconnector

import android.os.Handler
import android.os.Looper
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.ObjectInputStream

object Server {

    private val uiHandler by lazy { Handler(Looper.getMainLooper()) }

    fun <T> start(f: (T) -> Unit): Closeable {
        val nanoHTTPD = mkServer<T> { node ->
            f(node)
        }

        nanoHTTPD.start()

        return Closeable { nanoHTTPD.stop() }
    }

    private fun <T> mkServer(update: (T) -> Unit): NanoHTTPD =
        object : NanoHTTPD(8080) {
            override fun serve(session: IHTTPSession): Response =
                if (Method.PUT == session.method || Method.POST == session.method) {
                    println("LOGX :: POST/PUT | " + session.headers)

                    val len = session.headers["content-length"]!!.toInt()
                    val buf = ByteArray(len)

                    var count = 0
                    while (count < len) {
                        count += session.inputStream.read(buf, count, len - count)
                    }

                    @Suppress("UNCHECKED_CAST")
                    val v = ObjectInputStream(ByteArrayInputStream(buf)).readObject() as T
                    println(v)

                    uiHandler.post {
                        update(v)
                    }

                    newFixedLengthResponse(Response.Status.NO_CONTENT, MIME_PLAINTEXT, "")
                } else {
                    println("LOGX :: GET")

                    var msg = "<html><body><h1>Test Server</h1>\n"
                    msg += "<p>TODO</p>"
                    newFixedLengthResponse("$msg</body></html>\n")
                }
        }
}
