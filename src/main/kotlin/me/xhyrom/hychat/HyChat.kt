package me.xhyrom.hychat

import me.xhyrom.hychat.hooks.HooksManager
import me.xhyrom.hychat.listeners.ChatListener
import me.xhyrom.hychat.listeners.PacketListener
import me.xhyrom.hychat.listeners.PlayerListener
import me.xhyrom.hychat.modules.MuteChat
import me.xhyrom.hylib.bukkit.api.structs.BukkitCommand
import me.xhyrom.hylib.common.api.HyLibProvider
import me.xhyrom.hylib.common.api.structs.Command
import me.xhyrom.hylib.common.api.structs.Config
import me.xhyrom.hylib.common.api.structs.Language
import me.xhyrom.hylib.libs.commandapi.arguments.ArgumentSuggestions
import me.xhyrom.hylib.libs.commandapi.arguments.StringArgument
import me.xhyrom.hylib.libs.commandapi.executors.CommandArguments
import me.xhyrom.hylib.libs.commandapi.executors.CommandExecutor
import me.xhyrom.hylib.libs.packetevents.api.PacketEvents
import me.xhyrom.hylib.libs.packetevents.impl.factory.spigot.SpigotPacketEventsBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class HyChat : JavaPlugin() {
    companion object {
        private var instance: HyChat? = null

        fun getInstance(): HyChat {
            return instance!!
        }
    }

    private var chatConfig: Config? = null
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
        chatConfig = HyLibProvider.get().getConfigManager().register(File(this.dataFolder, "config.yml").path, this.getResource("config.yml")!!)
        language = HyLibProvider.get().getLanguageManager().register(this.dataFolder.path) { path ->
            this.getResource(path)!!
        }

        HyLibProvider.get().getBStatsManager().registerAddon(this, 17003, chatConfig())

        server.pluginManager.registerEvents(ChatListener(), this)
        server.pluginManager.registerEvents(PlayerListener(), this)

        if (chatConfig().getBoolean("no-chat-reports.enabled").get()) {
            PacketEvents.getAPI().eventManager.registerListener(PacketListener())
            PacketEvents.getAPI().init()
        }

        createCommand()
    }

    private fun createCommand() {
        HyLibProvider.get().getCommandManager().register(
            (HyLibProvider.get().getCommandManager().create("chat") as BukkitCommand)
                .withFullDescription("chat plugin management")
                .withSubcommand(
                    BukkitCommand("mute")
                        .withFullDescription("mute or unmute chat")
                        .withPermission("hychat.command.mute")
                        .executes(
                            CommandExecutor { _: CommandSender, _: CommandArguments ->
                                run {
                                    MuteChat.isMuted = !MuteChat.isMuted

                                    for (player in Bukkit.getOnlinePlayers()) {
                                        player.sendMessage(
                                            MiniMessage.miniMessage().deserialize(
                                                locale().getString(
                                                    if (MuteChat.isMuted) "commands.chat.mute.muted"
                                                    else "commands.chat.mute.unmuted"
                                                ).get()
                                            )
                                        )
                                    }
                                }
                            })
                )
                .withSubcommand(
                    BukkitCommand("reload")
                        .withFullDescription("reload config")
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
                            CommandExecutor { sender: CommandSender, args: CommandArguments ->
                                run {
                                    when (args[0] as String) {
                                        "config" -> {
                                            if (!chatConfig().reload()) {
                                                sender.sendMessage(
                                                    MiniMessage.miniMessage().deserialize(
                                                        locale().getString("commands.chat.reload.fail").get(),
                                                        Placeholder.component("type", Component.text("Chat"))
                                                    )
                                                )

                                                return@CommandExecutor
                                            }

                                            sender.sendMessage(
                                                MiniMessage.miniMessage().deserialize(
                                                    locale().getString("commands.chat.reload.success").get(),
                                                    Placeholder.component("type", Component.text("Chat"))
                                                )
                                            )
                                        }
                                        "lang" -> {
                                            if (!locale().reload()) {
                                                sender.sendMessage(
                                                    MiniMessage.miniMessage().deserialize(
                                                        locale().getString("commands.chat.reload.fail").get(),
                                                        Placeholder.component("type", Component.text("Language"))
                                                    )
                                                )

                                                return@CommandExecutor
                                            }

                                            sender.sendMessage(
                                                MiniMessage.miniMessage().deserialize(
                                                    locale().getString("commands.chat.reload.success").get(),
                                                    Placeholder.component("type", Component.text("Language"))
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        )
                ) as Command
        )
    }

    fun getHooks(): HooksManager {
        return hooks!!
    }

    fun locale(): Config {
        return language!!.getLang(chatConfig().getString("locale").get())
    }

    fun chatConfig(): Config {
        return chatConfig!!
    }
}
