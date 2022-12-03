package me.xhyrom.hychat

import me.xhyrom.hychat.hooks.HooksManager
import me.xhyrom.hychat.listeners.ChatListener
import me.xhyrom.hychat.listeners.PacketListener
import me.xhyrom.hychat.listeners.PlayerListener
import me.xhyrom.hychat.modules.MuteChat
import me.xhyrom.hylib.api.HyLib
import me.xhyrom.hylib.api.structs.Config
import me.xhyrom.hylib.api.structs.Language
import me.xhyrom.hylib.libs.commandapi.arguments.ArgumentSuggestions
import me.xhyrom.hylib.libs.commandapi.arguments.StringArgument
import me.xhyrom.hylib.libs.commandapi.executors.CommandExecutor
import me.xhyrom.hylib.libs.packetevents.api.PacketEvents
import me.xhyrom.hylib.libs.packetevents.impl.factory.spigot.SpigotPacketEventsBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
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
        HyLib.getInstance().getBStatsManager().registerAddon(this)

        hooks = HooksManager()
        config = HyLib.getInstance().getConfigManager().register(this, "config")
        language = HyLib.getInstance().getLanguageManager().register(this)

        server.pluginManager.registerEvents(ChatListener(), this)
        server.pluginManager.registerEvents(PlayerListener(), this)

        PacketEvents.getAPI().eventManager.registerListener(PacketListener())
        PacketEvents.getAPI().init()

        createCommand()
    }

    fun createCommand() {
        HyLib.getInstance().getCommandManager().addSubCommand(
            HyLib.getInstance().getCommandManager().createCommand("chat")
                .withFullDescription("chat plugin management")
                .withSubcommand(
                    HyLib.getInstance().getCommandManager().createCommand("mute")
                        .withPermission("hychat.command.mute")
                        .executes(
                            CommandExecutor { sender: CommandSender, args: Array<Any?> ->
                                run {
                                    MuteChat.isMuted = !MuteChat.isMuted

                                    for (player in Bukkit.getOnlinePlayers()) {
                                        player.sendMessage(
                                            MiniMessage.miniMessage().deserialize(
                                                locale().getString(
                                                    if (MuteChat.isMuted) "commands.chat.mute.muted"
                                                    else "commands.chat.mute.unmuted"
                                                )
                                            )
                                        )
                                    }
                                }
                            })
                )
                .withSubcommand(
                    HyLib.getInstance().getCommandManager().createCommand("reload")
                        .withPermission("hychat.command.reload")
                        .withArguments(
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
                                                        locale().getString("commands.chat.reload.fail"),
                                                        Placeholder.component("type", Component.text("Chat"))
                                                    )
                                                )

                                                return@CommandExecutor
                                            }

                                            sender.sendMessage(
                                                MiniMessage.miniMessage().deserialize(
                                                    locale().getString("commands.chat.reload.success"),
                                                    Placeholder.component("type", Component.text("Chat"))
                                                )
                                            )
                                        }
                                        "lang" -> {
                                            if (!locale().reload()) {
                                                sender.sendMessage(
                                                    MiniMessage.miniMessage().deserialize(
                                                        locale().getString("commands.chat.reload.fail"),
                                                        Placeholder.component("type", Component.text("Language"))
                                                    )
                                                )

                                                return@CommandExecutor
                                            }

                                            sender.sendMessage(
                                                MiniMessage.miniMessage().deserialize(
                                                    locale().getString("commands.chat.reload.success"),
                                                    Placeholder.component("type", Component.text("Language"))
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        )
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