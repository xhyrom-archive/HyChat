package me.xhyrom.hychat.modules

import me.xhyrom.hychat.HyChat
import me.xhyrom.hychat.listeners.antiSpamCooldown
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.player.AsyncPlayerChatEvent

object AntiSpam {
    fun handle(event: AsyncPlayerChatEvent): Boolean {
        if (!HyChat.getInstance().config.getBoolean("anti-spam.enabled") || event.player.hasPermission("hychat.anti-spam.bypass")) return false

        if (antiSpamCooldown[event.player.uniqueId] != null) {
            if (antiSpamCooldown[event.player.uniqueId]!! > System.currentTimeMillis()) {
                event.isCancelled = true
                event.player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                        HyChat.getInstance().localeGetStringPapi(event.player, "modules.anti-spam.cooldown")
                            .replace("%cooldown%", formatTime(antiSpamCooldown[event.player.uniqueId]!! - System.currentTimeMillis()))
                    )
                )
                return true
            }
        }

        val defaultCooldown = HyChat.getInstance().config.getLong("anti-spam.cooldown")

        for (permission in event.player.effectivePermissions) {
            if (!permission.permission.startsWith("hychat.anti-spam.cooldown.")) continue

            antiSpamCooldown[event.player.uniqueId] = System.currentTimeMillis() + permission.permission.split(".")[3].toLong()
            return false
        }

        antiSpamCooldown[event.player.uniqueId] = System.currentTimeMillis() + defaultCooldown

        return false
    }

    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60) % 60)
        val hours = (milliseconds / (1000 * 60 * 60) % 24)
        val days = (milliseconds / (1000 * 60 * 60 * 24) % 365)

        return "${if (days > 0) "$days days " else ""}${if (hours > 0) "$hours hours " else ""}${if (minutes > 0) "$minutes minutes " else ""}${if (seconds > 0) "$seconds seconds" else ""}"
    }
}