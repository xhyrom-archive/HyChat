package me.xhyrom.hychat.modules

import me.xhyrom.hychat.HyChat
import me.xhyrom.hychat.structs.Utils
import net.kyori.adventure.text.minimessage.MiniMessage
import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.Plugin

import kotlin.math.max
import kotlin.math.min

object AntiSwear {
    fun handle(event: AsyncPlayerChatEvent): Boolean {
        if (!HyChat.getInstance().chatConfig().getBoolean("anti-swear.enabled").get() || event.player.hasPermission("hychat.anti-swear.bypass")) return false

        val blockedWords = HyChat.getInstance().chatConfig().getStringList("anti-swear.blocked-words").get()

        if (blockedWords.any { blockedWord ->
                event.message.contains(blockedWord, ignoreCase = true) ||
                checkSimilarity(
                    event.message.filter { it.isLetterOrDigit() }, blockedWord.filter { it.isLetterOrDigit() }
                )
        }) {
            for (action in HyChat.getInstance().chatConfig().getStringList("anti-swear.actions").get()) {
                when (action) {
                    "hychat::send-message" -> {
                        event.player.sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                HyChat.getInstance().locale().getString("modules.anti-swear.message").get(),
                                Utils.papiTag(event.player)
                            )
                        )
                    }
                    "hychat::notify" -> {
                        Utils.notifyAdmins(
                            "anti-swear",
                            event.player,
                            event.message,
                        )
                    }
                    "hychat::cancel" -> {
                        event.isCancelled = true
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

        return false
    }

    private fun checkSimilarity(s1: String, s2: String): Boolean {
        if (
            HyChat.getInstance().chatConfig().getBoolean("anti-swear.jaro-winkler-distance.check").get() &&
            StringUtils.getJaroWinklerDistance(s1.lowercase(), s2.lowercase()) >= HyChat.getInstance().chatConfig().getDouble("anti-swear.jaro-winkler-distance.threshold").get()
        ) return true

        if (
            HyChat.getInstance().chatConfig().getBoolean("anti-swear.levenshtein-distance.check").get() &&
            findSimilarityLevenshteinDistance(s1.lowercase(), s2.lowercase()) >= HyChat.getInstance().chatConfig().getDouble("anti-swear.levenshtein-distance.threshold").get()
        ) return true

        return false
    }

    private fun findSimilarityLevenshteinDistance(x: String, y: String): Double {
        val maxLength = max(x.length, y.length)
        return if (maxLength > 0) {
            (maxLength * 1.0 - getLevenshteinDistance(x, y)) / maxLength * 1.0
        } else 1.0
    }
    
    private fun getLevenshteinDistance(X: String, Y: String): Int {
        val m = X.length
        val n = Y.length
        val T = Array(m + 1) { IntArray(n + 1) }
        for (i in 1..m) {
            T[i][0] = i
        }

        for (j in 1..n) {
            T[0][j] = j
        }

        var cost: Int
        for (i in 1..m) {
            for (j in 1..n) {
                cost = if (X[i - 1] == Y[j - 1]) 0 else 1
                T[i][j] = min(min(T[i - 1][j] + 1, T[i][j - 1] + 1),
                T[i - 1][j - 1] + cost)
            }
        }

        return T[m][n]
    }
}