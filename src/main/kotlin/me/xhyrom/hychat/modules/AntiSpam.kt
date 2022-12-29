package me.xhyrom.hychat.modules

import me.xhyrom.hychat.HyChat
import me.xhyrom.hychat.listeners.antiSpamCooldown
import me.xhyrom.hychat.structs.Utils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.event.player.AsyncPlayerChatEvent

object AntiSpam {
    fun handle(event: AsyncPlayerChatEvent): Boolean {
        if (!HyChat.getInstance().chatConfig().getBoolean("anti-spam.enabled").get() || event.player.hasPermission("hychat.anti-spam.bypass")) return false

        if (antiSpamCooldown[event.player.uniqueId] != null) {
            if (antiSpamCooldown[event.player.uniqueId]!! > System.currentTimeMillis()) {
                for (action in HyChat.getInstance().chatConfig().getStringList("anti-spam.actions").get()) {
                    when (action) {
                        "hychat::send-message" -> {
                            event.player.sendMessage(
                                MiniMessage.miniMessage().deserialize(
                                    HyChat.getInstance().locale().getString("modules.anti-spam.cooldown").get(),
                                    Utils.papiTag(event.player),
                                    Placeholder.component("cooldown", Component.text(formatTime(antiSpamCooldown[event.player.uniqueId]!! - System.currentTimeMillis())))
                                )
                            )
                        }
                        "hychat::notify" -> {
                            Utils.notifyAdmins(
                                "anti-spam",
                                event.player,
                                event.message,
                            )
                        }
                        "hychat::cancel" -> {
                            event.isCancelled = true
                            return true
                        }
                        else -> {
                            if (HyChat.getInstance().getHooks().placeholderApi != null) {
                                Bukkit.getScheduler().runTask(HyChat.getInstance(), Runnable {
                                    Bukkit.dispatchCommand(
                                        Bukkit.getConsoleSender(),
                                        HyChat.getInstance().getHooks().placeholderApi!!.setPlaceholders(
                                            event.player,
                                            action.replace("<player>", event.player.name)
                                        )
                                    )
                                })
                            } else {
                                Bukkit.getScheduler().runTask(HyChat.getInstance(), Runnable {
                                    Bukkit.dispatchCommand(
                                        Bukkit.getConsoleSender(),
                                        action.replace("<player>", event.player.name)
                                    )
                                })
                            }
                        }
                    }
                }

                return true
            }
        }

        val defaultCooldown = HyChat.getInstance().chatConfig().getLong("anti-spam.cooldown").get()

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