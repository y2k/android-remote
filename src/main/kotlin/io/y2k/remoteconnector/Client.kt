package io.y2k.remoteconnector

import android.annotation.SuppressLint
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

object Client {

    fun sendObject(any: Any) {
        val bytes = toBytes(any)

        sendBytes(bytes)
    }

    private fun toBytes(node: Any): ByteArray {
        val stream = ByteArrayOutputStream()
        ObjectOutputStream(stream).writeObject(node)
        return stream.toByteArray()
    }

    fun sendBytes(bytes: ByteArray) {
        val urlString = "http://${getIp()}:8080/"

        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.doOutput = true
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/octet-stream")
        connection.connect()

        connection.outputStream.use { it.write(bytes) }
        connection.inputStream.buffered().readBytes()
    }

    @SuppressLint("NewApi")
    private fun getIp(): String {
        val dir = System.getenv("ANDROID_HOME")
        val p = Runtime.getRuntime().exec("$dir/platform-tools/adb shell ifconfig wlan0")
        p.waitFor(5, TimeUnit.SECONDS)
        val response = String(p.inputStream.readBytes())
        return Regex("inet addr:([\\d.]+)").find(response)!!.groupValues[1]
    }
}
