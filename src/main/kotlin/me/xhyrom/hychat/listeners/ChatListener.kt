package me.xhyrom.hychat.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import me.xhyrom.hychat.HyChat
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.*
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

val antiSpamCooldown = mutableMapOf<UUID, Long>()

class ChatListener : Listener {
    private val safeMiniMessage: MiniMessage = MiniMessage.builder()
        .tags(TagResolver.builder()
            .resolver(StandardTags.color())
            .resolver(StandardTags.decorations())
            .resolver(StandardTags.gradient())
            .resolver(StandardTags.rainbow())
            .build()
        )
        .build()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChat(event: AsyncChatEvent) {
        var format = HyChat.getInstance().config.getString("chat-format")!!

        if (HyChat.getInstance().getHooks().placeholderApi != null) {
            format = HyChat.getInstance().getHooks().placeholderApi!!.setPlaceholders(event.player, format.replace("%", "%%"))
        }

        event.renderer { _, sourceDisplayName, message, _ ->
            MiniMessage.miniMessage().deserialize(
                format,
                Placeholder.component("message", message),
                Placeholder.component("player", sourceDisplayName)
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onChatAntiSpam(event: AsyncPlayerChatEvent) {
        if (!HyChat.getInstance().config.getBoolean("anti-spam.enabled") || event.player.hasPermission("hychat.anti-spam.bypass")) return

        if (antiSpamCooldown[event.player.uniqueId] != null) {
            if (antiSpamCooldown[event.player.uniqueId]!! > System.currentTimeMillis()) {
                event.isCancelled = true
                event.player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                        HyChat.getInstance().locale().getString("modules.anti-spam.cooldown")
                            .replace("%cooldown%", formatTime(antiSpamCooldown[event.player.uniqueId]!! - System.currentTimeMillis()))
                    )
                )
                return
            }
        }

        val defaultCooldown = HyChat.getInstance().config.getLong("anti-spam.cooldown")

        for (permission in event.player.effectivePermissions) {
            if (!permission.permission.startsWith("hychat.anti-spam.cooldown.")) continue

            antiSpamCooldown[event.player.uniqueId] = System.currentTimeMillis() + permission.permission.split(".")[3].toLong()
            return
        }

        antiSpamCooldown[event.player.uniqueId] = System.currentTimeMillis() + defaultCooldown
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onChatAntiSwear(event: AsyncPlayerChatEvent) {
        if (!HyChat.getInstance().config.getBoolean("anti-swear.enabled") || event.player.hasPermission("hychat.anti-swear.bypass")) return

        val blockedWords = HyChat.getInstance().config.getStringList("anti-swear.blocked-words")
        if (blockedWords.any { event.message.contains(it, ignoreCase = true) }) {
            event.isCancelled = true
            event.player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    HyChat.getInstance().locale().getString("modules.anti-swear.message")
                )
            )
        }
    }

    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60) % 60)
        val hours = (milliseconds / (1000 * 60 * 60) % 24)
        val days = (milliseconds / (1000 * 60 * 60 * 24) % 365)

        return "${if (days > 0) "$days days " else ""}${if (hours > 0) "$hours hours " else ""}${if (minutes > 0) "$minutes minutes " else ""}${if (seconds > 0) "$seconds seconds" else ""}"
    }
}