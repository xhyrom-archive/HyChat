package me.xhyrom.hychat.modules

import me.xhyrom.hychat.HyChat
import me.xhyrom.hychat.listeners.antiSpamCooldown
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.player.AsyncPlayerChatEvent

object MuteChat {
    var isMuted = false

    fun handle(event: AsyncPlayerChatEvent): Boolean {
        if (event.player.hasPermission("hychat.mute.bypass")) return false

        if (isMuted) {
            event.isCancelled = true
            event.player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    HyChat.getInstance().localeGetStringPapi(event.player, "modules.mute-chat.message")
                )
            )

            return true
        }

        return false
    }
}