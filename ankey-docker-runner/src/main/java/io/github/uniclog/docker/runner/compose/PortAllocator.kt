package io.github.uniclog.docker.runner.compose

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

    fun allocate(configurationName: String, projectExists: (String) -> Boolean): PortAllocation? {
        var offset = 0
        while (offset <= maxOffset) {
            val projectName = projectNameWithOffset(configurationName, offset)
            val portsToCheck = basePorts.map { it + offset }
            val portsFree = portsToCheck.all { !isPortUsed(it) }
            val projectFree = !projectExists(projectName)
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

    private fun normalizeProjectName(name: String): String =
        name
            .lowercase()
            .replace(Regex("[^a-z0-9_.-]"), "-")
            .replace(Regex("[-_.]{2,}"), "-")
            .trim('-', '_', '.')

    private fun projectNameWithOffset(name: String, offset: Int): String {
        val base = normalizeProjectName(name)
        return if (offset == 0) base else "$base-$offset"
    }
}
