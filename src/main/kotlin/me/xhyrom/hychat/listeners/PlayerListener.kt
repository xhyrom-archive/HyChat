package me.xhyrom.hychat.listeners

import me.xhyrom.hychat.modules.JoinAndLeave
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        JoinAndLeave.handleJoin(event)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        JoinAndLeave.handleQuit(event)
    }
}