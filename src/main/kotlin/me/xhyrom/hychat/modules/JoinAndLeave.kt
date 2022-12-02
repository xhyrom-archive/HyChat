package me.xhyrom.hychat.modules

import me.xhyrom.hychat.HyChat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object JoinAndLeave {
    fun handleJoin(event: PlayerJoinEvent) {
        if (!HyChat.getInstance().config.getBoolean("join-and-leave.join-enabled") || (event.player.hasPermission("hychat.join-and-leave.bypass-join") && !event.player.isOp)) return

        val joinMessage = HyChat.getInstance().localeGetStringPapi(event.player, "modules.join-and-leave.join-message")

        event.joinMessage(MiniMessage.miniMessage().deserialize(
            joinMessage,
            Placeholder.component("player", Component.text(event.player.name)))
        )
    }

    fun handleQuit(event: PlayerQuitEvent) {
        if (!HyChat.getInstance().config.getBoolean("join-and-leave.leave-enabled") || (event.player.hasPermission("hychat.join-and-leave.bypass-leave") && !event.player.isOp)) return

        val quitMessage = HyChat.getInstance().localeGetStringPapi(event.player, "modules.join-and-leave.leave-message")

        event.quitMessage(MiniMessage.miniMessage().deserialize(
            quitMessage,
            Placeholder.component("player", Component.text(event.player.name)))
        )
    }
}