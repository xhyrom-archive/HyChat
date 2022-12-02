package me.xhyrom.hychat.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import me.xhyrom.hychat.HyChat
import me.xhyrom.hychat.modules.AntiSpam
import me.xhyrom.hychat.modules.AntiSwear
import me.xhyrom.hychat.modules.MuteChat
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.ClickEvent.Action
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.*
import java.util.regex.Pattern

val antiSpamCooldown = mutableMapOf<UUID, Long>()

class ChatListener : Listener {
    private val URL_REGEX = Pattern.compile(
        "(https?://)?[a-z0-9]+(\\.[a-z0-9]+)*(\\.[a-z0-9]{1,10})((/+)[^/ ]*)*",
        Pattern.CASE_INSENSITIVE or Pattern.MULTILINE
    )
    private val REPLACER = TextReplacementConfig.builder()
        .match(URL_REGEX)
        .replacement { c ->
            c.clickEvent(
                ClickEvent.openUrl(
                    if (c.content().startsWith("http")) c.content() else "https://" + c.content()
                )
            )
        }
        .build()
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
    fun onChatFormat(event: AsyncChatEvent) {
        if (HyChat.getInstance().config.getBoolean("clickable-links")) {
            event.message(event.message().replaceText(REPLACER))
        }

        if (!HyChat.getInstance().config.getBoolean("chat-format.enabled")) return

        var format = HyChat.getInstance().config.getString("chat-format.format")!!

        if (HyChat.getInstance().getHooks().placeholderApi != null) {
            format = HyChat.getInstance().getHooks().placeholderApi!!.setPlaceholders(event.player, format).replace("%", "%%")
        }

        event.renderer { player, sourceDisplayName, message, _ ->
            var sdn = sourceDisplayName
            MiniMessage.miniMessage().deserialize(
                format,
                Placeholder.component("message", message),
                Placeholder.component(
                    "player",
                    run {
                        if (HyChat.getInstance().config.getBoolean("chat-format.name-hover.message.enabled")) {
                            var nameHoverFormat = HyChat.getInstance().config.getString("chat-format.name-hover.message.format")!!

                            if (HyChat.getInstance().getHooks().placeholderApi != null) {
                                nameHoverFormat = HyChat.getInstance().getHooks().placeholderApi!!.setPlaceholders(event.player, nameHoverFormat).replace("%", "%%")
                            }

                            sdn = sdn.hoverEvent(
                                HoverEvent.hoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    MiniMessage.miniMessage().deserialize(
                                        nameHoverFormat,
                                        Placeholder.component("player", sourceDisplayName),
                                        Placeholder.component("message", message)
                                    )
                                )
                            )
                        }

                        if (HyChat.getInstance().config.getBoolean("chat-format.name-hover.on-click.enabled")) {
                            val nameHoverClickAction = HyChat.getInstance().config.getString("chat-format.name-hover.on-click.action")!!
                            var nameHoverClickValue = HyChat.getInstance().config.getString("chat-format.name-hover.on-click.value")!!
                                .replace("<player>", PlainTextComponentSerializer.plainText().serialize(sourceDisplayName))
                                .replace("<message>", PlainTextComponentSerializer.plainText().serialize(message))

                            if (HyChat.getInstance().getHooks().placeholderApi != null) {
                                nameHoverClickValue = HyChat.getInstance().getHooks().placeholderApi!!.setPlaceholders(event.player, nameHoverClickValue).replace("%", "%%")
                            }

                            sdn = sdn.clickEvent(
                                ClickEvent.clickEvent(
                                    Action.valueOf(nameHoverClickAction.uppercase()),
                                    nameHoverClickValue
                                )
                            )
                        }

                        sdn
                    }
                )
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onChatAntiSpam(event: AsyncPlayerChatEvent) {
        if (MuteChat.handle(event)) return
        if (AntiSwear.handle(event)) return
        if (AntiSpam.handle(event)) return
    }
}