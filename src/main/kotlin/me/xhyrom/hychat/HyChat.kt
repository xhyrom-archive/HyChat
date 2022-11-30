package me.xhyrom.hychat

import me.xhyrom.hychat.hooks.HooksManager
import me.xhyrom.hychat.listeners.ChatListener
import me.xhyrom.hychat.listeners.PacketListener
import me.xhyrom.hylib.api.HyLib
import me.xhyrom.hylib.api.structs.Config
import me.xhyrom.hylib.api.structs.Language
import me.xhyrom.hylib.libs.commandapi.arguments.ArgumentSuggestions
import me.xhyrom.hylib.libs.commandapi.arguments.LiteralArgument
import me.xhyrom.hylib.libs.commandapi.arguments.StringArgument
import me.xhyrom.hylib.libs.commandapi.executors.CommandExecutor
import me.xhyrom.hylib.libs.packetevents.api.PacketEvents
import me.xhyrom.hylib.libs.packetevents.impl.factory.spigot.SpigotPacketEventsBuilder
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin


class HyChat : JavaPlugin() {
    companion object {
        private var instance: HyChat? = null

        fun getInstance(): HyChat {
            return instance!!
        }
    }

    private var config: Config? = null
    private var language: Language? = null
    private var hooks: HooksManager? = null

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().settings.debug(false).bStats(false).checkForUpdates(false)
        PacketEvents.getAPI().load()
    }

    override fun onEnable() {
        instance = this
        hooks = HooksManager()
        config = HyLib.getInstance().getConfigManager().register(this, "config")
        language = HyLib.getInstance().getLanguageManager().register(this)

        server.pluginManager.registerEvents(ChatListener(), this)

        PacketEvents.getAPI().eventManager.registerListener(PacketListener())
        PacketEvents.getAPI().init()

        createCommand()
    }

    fun createCommand() {
        HyLib.getInstance().getCommandManager().addSubCommand(
            HyLib.getInstance().getCommandManager().createCommand("chat")
                .withFullDescription("reload chat configuration")
                .withArguments(
                    LiteralArgument("reload").setListed(false),
                    StringArgument("type").includeSuggestions(
                        ArgumentSuggestions.strings(
                            "config",
                            "lang"
                        )
                    )
                )
                .executes(
                    CommandExecutor { sender: CommandSender, args: Array<Any?> ->
                        run {
                            when (args[0] as String) {
                                "config" -> {
                                    if (!config!!.reload()) {
                                        sender.sendMessage(
                                            MiniMessage.miniMessage().deserialize(
                                                locale().getString("commands.chat.reload.fail").replace(
                                                    "%type%",
                                                    "Chat"
                                                )
                                            )
                                        )

                                        return@CommandExecutor
                                    }

                                    sender.sendMessage(
                                        MiniMessage.miniMessage().deserialize(
                                            locale().getString("commands.chat.reload.success").replace(
                                                "%type%",
                                                "Chat"
                                            )
                                        )
                                    )
                                }
                                "lang" -> {
                                    if (!locale().reload()) {
                                        sender.sendMessage(
                                            MiniMessage.miniMessage().deserialize(
                                                locale().getString("commands.chat.reload.fail").replace(
                                                    "%type%",
                                                    "Language"
                                                )
                                            )
                                        )

                                        return@CommandExecutor
                                    }

                                    sender.sendMessage(
                                        MiniMessage.miniMessage().deserialize(
                                            locale().getString("commands.chat.reload.success").replace(
                                                "%type%",
                                                "Language"
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
        )
    }

    fun getHooks(): HooksManager {
        return hooks!!
    }
    fun locale(): Config {
        return language!!.getLocale(config!!.getString("locale"))
    }
}