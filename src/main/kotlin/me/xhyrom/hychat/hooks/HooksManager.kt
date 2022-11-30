package me.xhyrom.hychat.hooks

import org.bukkit.Bukkit

class HooksManager {
    var placeholderApi: PlaceholderAPI? = null

    init {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderApi = PlaceholderAPI()
        }
    }
}