package org.mvplugins.multiverse.core.commands

import org.bukkit.Bukkit
import org.mvplugins.multiverse.core.config.CoreConfig
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class TeleportCommandTest : AbstractCommandTest() {

    private lateinit var config : CoreConfig

    @BeforeTest
    fun setUp() {
        config = serviceLocator.getActiveService(CoreConfig::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("CoreConfig is not available as a service") }

        server.addPlayer("Player1")
        server.addPlayer("Player2")
        server.addPlayer("Player3")
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("otherworld")).isSuccess)
    }

    @Test
    fun `Teleport a player to other world`() {
        assertTrue(Bukkit.dispatchCommand(console, "mv tp Player1 otherworld --unsafe"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("otherworld")?.spawnLocation, server.getPlayer("Player1")?.location)
    }

    @Test
    fun `Teleport multiple players to other world`() {
        assertTrue(Bukkit.dispatchCommand(console, "mv tp Player1,Player2 otherworld --unsafe"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("otherworld")?.spawnLocation, server.getPlayer("Player1")?.location)
        assertLocationEquals(server.getWorld("otherworld")?.spawnLocation, server.getPlayer("Player2")?.location)
    }

    @Test
    fun `Teleport multiple players to invalid world`() {
        assertTrue(Bukkit.dispatchCommand(console, "mv tp Player1,Player2 invalidworld"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("world")?.spawnLocation, server.getPlayer("Player1")?.location)
        assertLocationEquals(server.getWorld("world")?.spawnLocation, server.getPlayer("Player2")?.location)
    }

    @Test
    fun `Player no permission to teleport`() {
        assertTrue(player.performCommand("mv tp otherworld"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("world")?.spawnLocation, server.getPlayer("Player1")?.location)
    }

    @Test
    fun `Player permission to teleport self`() {
        addPermission("multiverse.teleport.self.w.otherworld")

        assertTrue(player.performCommand("mv tp otherworld --unsafe"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("otherworld")?.spawnLocation, player.location)

        assertTrue(player.performCommand("mv tp Player1 world --unsafe"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("world")?.spawnLocation, server.getPlayer("Player1")?.location)

        assertTrue(player.performCommand("mv tp Player2,Player3 world --unsafe"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("world")?.spawnLocation, server.getPlayer("Player2")?.location)
        assertLocationEquals(server.getWorld("world")?.spawnLocation, server.getPlayer("Player3")?.location)
    }

    @Test
    fun `Player permission to teleport others`() {
        addPermission("multiverse.teleport.other.w.otherworld")

        assertTrue(player.performCommand("mv tp Player1 otherworld --unsafe"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("otherworld")?.spawnLocation, server.getPlayer("Player1")?.location)

        assertTrue(player.performCommand("mv tp Player2,Player3 otherworld --unsafe"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("otherworld")?.spawnLocation, server.getPlayer("Player2")?.location)
        assertLocationEquals(server.getWorld("otherworld")?.spawnLocation, server.getPlayer("Player3")?.location)
    }

    @Test
    fun `Player permission to teleport others cannot teleport self`() {
        addPermission("multiverse.teleport.other.w.otherworld")

        assertTrue(player.performCommand("mv tp otherworld --unsafe"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("world")?.spawnLocation, player.location)

        assertTrue(player.performCommand("mv tp benwoo1110,Player1,Player2 world --unsafe"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("world")?.spawnLocation, server.getPlayer("benwoo1110")?.location)
        assertLocationEquals(server.getWorld("world")?.spawnLocation, server.getPlayer("Player1")?.location)
        assertLocationEquals(server.getWorld("world")?.spawnLocation, server.getPlayer("Player2")?.location)
    }

    @Test
    fun `Player permission to teleport self without finer permission`() {
        config.setUseFinerTeleportPermissions(false)
        addPermission("multiverse.teleport.self.w")

        assertTrue(player.performCommand("mv tp otherworld --unsafe"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("otherworld")?.spawnLocation, player.location)
    }

    @Test
    fun `Player permission to teleport others without finer permission`() {
        config.setUseFinerTeleportPermissions(false)
        addPermission("multiverse.teleport.other.w")

        assertTrue(player.performCommand("mv tp Player1,Player2 otherworld --unsafe"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("otherworld")?.spawnLocation, server.getPlayer("Player1")?.location)
        assertLocationEquals(server.getWorld("otherworld")?.spawnLocation, server.getPlayer("Player2")?.location)

        assertTrue(player.performCommand("mv tp benwoo1110,Player3 world --unsafe"))
        Thread.sleep(10) // wait for the player to teleport asynchronously
        assertLocationEquals(server.getWorld("world")?.spawnLocation, server.getPlayer("benwoo1110")?.location)
        assertLocationEquals(server.getWorld("world")?.spawnLocation, server.getPlayer("Player3")?.location)
    }
}
