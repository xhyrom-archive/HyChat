package me.xhyrom.hychat.hooks

import org.bukkit.entity.Player

class PlaceholderAPI {
    fun setPlaceholders(player: Player, text: String): String {
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text)
    }
}