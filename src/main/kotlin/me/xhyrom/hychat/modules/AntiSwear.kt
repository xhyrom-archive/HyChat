package me.xhyrom.hychat.modules

import me.xhyrom.hychat.HyChat
import me.xhyrom.hychat.structs.Utils
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.player.AsyncPlayerChatEvent

object AntiSwear {
    fun handle(event: AsyncPlayerChatEvent): Boolean {
        if (!HyChat.getInstance().chatConfig().getBoolean("anti-swear.enabled").get() || event.player.hasPermission("hychat.anti-swear.bypass")) return false

        val blockedWords = HyChat.getInstance().chatConfig().getStringList("anti-swear.blocked-words").get()
        if (blockedWords.any { event.message.contains(it, ignoreCase = true) }) {
            event.isCancelled = true
            event.player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    HyChat.getInstance().locale().getString("modules.anti-swear.message").get(),
                    Utils.papiTag(event.player)
                )
            )

            return true
        }

        return false
    }
}