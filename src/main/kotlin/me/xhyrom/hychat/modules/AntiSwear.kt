package me.xhyrom.hychat.modules

import me.xhyrom.hychat.HyChat
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.player.AsyncPlayerChatEvent

object AntiSwear {
    fun handle(event: AsyncPlayerChatEvent): Boolean {
        if (!HyChat.getInstance().config.getBoolean("anti-swear.enabled") || event.player.hasPermission("hychat.anti-swear.bypass")) return false

        val blockedWords = HyChat.getInstance().config.getStringList("anti-swear.blocked-words")
        if (blockedWords.any { event.message.contains(it, ignoreCase = true) }) {
            event.isCancelled = true
            event.player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    HyChat.getInstance().locale().getString("modules.anti-swear.message")
                )
            )

            return true
        }

        return false
    }
}