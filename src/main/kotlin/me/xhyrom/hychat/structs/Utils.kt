package me.xhyrom.hychat.structs

import me.xhyrom.hychat.HyChat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

import org.bukkit.entity.Player

object Utils {
    fun papiTag(player: Player): TagResolver {
        return TagResolver.resolver(
            "papi"
        ) { argumentQueue: ArgumentQueue, _: Context? ->
            if (HyChat.getInstance().getHooks().placeholderApi == null) return@resolver Tag.selfClosingInserting(
                Component.text("PlaceholderAPI not found")
            )

            // Get the string placeholder that they want to use.
            val papiPlaceholder = argumentQueue.popOr("papi tag requires an argument").value()

            // Then get PAPI to parse the placeholder for the given player.
            val parsedPlaceholder: String = HyChat.getInstance().getHooks().placeholderApi!!.setPlaceholders(player, "%$papiPlaceholder%")

            // We need to turn this ugly legacy string into a nice component.
            val componentPlaceholder: Component = LegacyComponentSerializer.legacy('&').deserialize(parsedPlaceholder)
            Tag.selfClosingInserting(componentPlaceholder)
        }
    }
}