package me.xhyrom.hychat.modules

import me.xhyrom.hychat.HyChat
import me.xhyrom.hychat.structs.Utils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object JoinAndLeave {
    fun handleJoin(event: PlayerJoinEvent) {
        if (!HyChat.getInstance().chatConfig().getBoolean("join-and-leave.join-enabled").get() || (event.player.hasPermission("hychat.join-and-leave.bypass-join") && !event.player.isOp)) return

        val joinMessage = HyChat.getInstance().locale().getString("modules.join-and-leave.join-message").get()

        event.joinMessage(MiniMessage.miniMessage().deserialize(
            joinMessage,
            Utils.papiTag(event.player),
            Placeholder.component("player", Component.text(event.player.name)))
        )
    }

    fun handleQuit(event: PlayerQuitEvent) {
        if (!HyChat.getInstance().chatConfig().getBoolean("join-and-leave.leave-enabled").get() || (event.player.hasPermission("hychat.join-and-leave.bypass-leave") && !event.player.isOp)) return

        val quitMessage = HyChat.getInstance().locale().getString("modules.join-and-leave.leave-message").get()

        event.quitMessage(MiniMessage.miniMessage().deserialize(
            quitMessage,
            Utils.papiTag(event.player),
            Placeholder.component("player", Component.text(event.player.name)))
        )
    }
}