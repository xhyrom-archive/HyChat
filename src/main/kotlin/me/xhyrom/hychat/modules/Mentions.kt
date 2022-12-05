package me.xhyrom.hychat.modules

import io.papermc.paper.event.player.AsyncChatEvent
import me.xhyrom.hychat.HyChat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import java.util.regex.Pattern

class Mention(var players: List<Player>, var message: Component)

object Mentions {
    private val MENTION_REGEX = Pattern.compile(
        "@\\w{3,16}",
        Pattern.CASE_INSENSITIVE or Pattern.MULTILINE
    )
    private val MENTION_REPLACER = TextReplacementConfig.builder()
        .match(MENTION_REGEX)
        .replacement { c ->
            MiniMessage.miniMessage().deserialize(
                HyChat.getInstance().chatConfig().getString("mention-system.highlight-color").get(),
                Placeholder.component("player", c)
            )
        }
        .build()

    fun handle(event: AsyncChatEvent): Mention {
        if (!HyChat.getInstance().chatConfig().getBoolean("mention-system.enabled").get()) return Mention(emptyList(), event.message())

        val mentions = MENTION_REGEX.toRegex().findAll(PlainTextComponentSerializer.plainText().serialize(event.message()))
        val players = mentions.map { it.value.substring(1) }.mapNotNull { HyChat.getInstance().server.getPlayer(it) }.toList()

        return Mention(players, event.message().replaceText(MENTION_REPLACER))
    }
}