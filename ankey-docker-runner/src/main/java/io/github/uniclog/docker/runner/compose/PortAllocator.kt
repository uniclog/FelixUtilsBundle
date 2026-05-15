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
        val nameOffset = findFreeNameOffset(name) ?: return null
        val portOffset = findFreePortOffset() ?: return null
        return PortAllocation(
            projectName = projectNameWithOffset(name, nameOffset),
            offset = portOffset
        )
    }

    private fun findFreeNameOffset(name: String): Int? {
        for (offset in 0..maxOffset) {
            val projectName = projectNameWithOffset(name, offset)
            if (!ComposeRunner.dockerProjectExists(projectName)) {
                return offset
            }
        }
        return null
    }

    private fun findFreePortOffset(): Int? {
        for (offset in 0..maxOffset) {
            val portsFree = basePorts
                .map { it + offset }
                .all { !isPortUsed(it) }
            if (portsFree) {
                return offset
            }
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
