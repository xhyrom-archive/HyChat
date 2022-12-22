package me.xhyrom.hychat.listeners

import me.xhyrom.hychat.HyChat
import me.xhyrom.hylib.libs.packetevents.api.event.PacketListenerAbstract
import me.xhyrom.hylib.libs.packetevents.api.event.PacketListenerPriority
import me.xhyrom.hylib.libs.packetevents.api.event.PacketSendEvent
import me.xhyrom.hylib.libs.packetevents.api.protocol.chat.message.ChatMessage_v1_19_1
import me.xhyrom.hylib.libs.packetevents.api.protocol.chat.message.ChatMessage_v1_19_3
import me.xhyrom.hylib.libs.packetevents.api.protocol.packettype.PacketType
import me.xhyrom.hylib.libs.packetevents.api.wrapper.play.server.WrapperPlayServerChatMessage
import me.xhyrom.hylib.libs.packetevents.api.wrapper.play.server.WrapperPlayServerServerData
import me.xhyrom.hylib.libs.packetevents.api.wrapper.status.server.WrapperStatusServerResponse
import java.util.*

class PacketListener : PacketListenerAbstract(PacketListenerPriority.LOW) {
    override fun onPacketSend(event: PacketSendEvent) {
        when (event.packetType) {
            PacketType.Status.Server.RESPONSE -> {
                val wrapper = WrapperStatusServerResponse(event)
                val newObj = wrapper.component
                newObj.addProperty("preventsChatReports", true)
                wrapper.component = newObj
            }
            PacketType.Play.Server.SERVER_DATA -> {
                if (!HyChat.getInstance().chatConfig().getBoolean("no-chat-reports.enforce-secure-chat").get()) return

                val serverData = WrapperPlayServerServerData(event)
                serverData.isEnforceSecureChat = true
            }
            PacketType.Play.Server.CHAT_MESSAGE -> {
                if (!HyChat.getInstance().chatConfig().getBoolean("no-chat-reports.strip-signature").get()) return

                val chatMessage = WrapperPlayServerChatMessage(event)

                if (chatMessage.message is ChatMessage_v1_19_1) {
                    (chatMessage.message as ChatMessage_v1_19_1).signature = byteArrayOf()
                    (chatMessage.message as ChatMessage_v1_19_1).salt = 0
                    (chatMessage.message as ChatMessage_v1_19_1).senderUUID = UUID(0L, 0L)
                    (chatMessage.message as ChatMessage_v1_19_1).previousSignature = null
                }

                if (chatMessage.message is ChatMessage_v1_19_3) {
                    (chatMessage.message as ChatMessage_v1_19_3).signature = byteArrayOf()
                    (chatMessage.message as ChatMessage_v1_19_3).salt = 0
                    (chatMessage.message as ChatMessage_v1_19_3).senderUUID = UUID(0L, 0L)
                }
            }
            PacketType.Play.Server.PLAYER_CHAT_HEADER -> {
                if (HyChat.getInstance().chatConfig().getBoolean("no-chat-reports.send-header-chat-packet").get()) return

                event.isCancelled = true
            }
        }
    }
}