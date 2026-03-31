package io.github.uniclog.docker.runner.compose

import io.github.uniclog.docker.runner.docker.ComposeRunner
import java.net.BindException
import java.net.InetSocketAddress
import java.net.StandardSocketOptions
import java.nio.channels.ServerSocketChannel

data class PortAllocation(
    val projectName: String,
    val offset: Int
)

class PortAllocator(
    private val basePorts: List<Int>,
    private val maxOffset: Int = 200
) {

    fun allocate(name: String): PortAllocation? {
        var offset = 0
        while (offset <= maxOffset) {
            val projectName = projectNameWithOffset(name, offset)
            val portsToCheck = basePorts.map { it + offset }
            val portsFree = portsToCheck.all { !isPortUsed(it) }
            val projectFree = !ComposeRunner.dockerProjectExists(projectName)
            if (portsFree && projectFree) {
                return PortAllocation(projectName = projectName, offset = offset)
            }
            offset++
        }
        return null
    }

    private fun isPortUsed(port: Int): Boolean {
        return try {
            ServerSocketChannel.open().use { channel ->
                channel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
                channel.bind(InetSocketAddress(port))
            }
            false
        } catch (_: BindException) {
            true
        }
    }

    private fun projectNameWithOffset(name: String, offset: Int): String {
        val base = normalizeProjectName(name)
        return if (offset == 0) base else "${base}-${offset}"
    }

    private fun normalizeProjectName(name: String): String =
        name
            .lowercase()
            .replace(Regex("[^a-z0-9_.-]"), "-")
            .replace(Regex("[-_.]{2,}"), "-")
    //.trim('-', '_', '.')
}
