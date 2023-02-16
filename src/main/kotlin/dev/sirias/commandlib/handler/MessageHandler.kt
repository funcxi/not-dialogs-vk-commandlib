package dev.sirias.commandlib.handler

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.vk.api.sdk.objects.messages.Message
import dev.sirias.commandlib.model.sender.GSON
import dev.sirias.commandlib.service.CommandService
import java.util.*

class MessageHandler(private val commandService: CommandService, private val commandPrefixes: Array<out Char>) {
    fun handleMessage(message: Message) {
        val commandArgs = message.text.lowercase().split(" ").toTypedArray()
        val payload = if (message.replyMessage != null) {
            fromJson(message.replyMessage.payload)
        } else if (message.fwdMessages.isNotEmpty()) {
            fromJson(message.fwdMessages[0].payload)
        } else null

        val fromId = message.fromId

        if (payload != null && payload.has("user_id") && payload.get("user_id").asInt != fromId) return

        val command = commandService.getCommand(
            if (payload == null) {
                if (commandPrefixes.any { commandArgs[0].startsWith(it) }) {
                    commandArgs[0].substring(1)
                } else null
            } else {
                payload.get("command").asString
            }) ?: return

        if (payload != null && !command.messageIds.containsValue(UUID.fromString(payload.get("unique_id")?.asString))) return

        val modifyArgs = commandArgs.copyOfRange(1, commandArgs.size)

        val defaultMean = command.defaultMean

        val subCommand = command.subCommands[modifyArgs[0]]

        if (subCommand == null) {
            defaultMean?.execute(command, message, modifyArgs, commandService)

            return
        }

        subCommand.execute(command, message, modifyArgs.copyOfRange(1, modifyArgs.size), commandService)
    }
}

fun <T> fromJson(json: JsonElement, clazz: Class<T>): T = GSON.fromJson(json, clazz)

fun <T> fromJson(json: String, clazz: Class<T>): T = GSON.fromJson(json, clazz)

fun fromJson(json: String) = fromJson(json, JsonObject::class.java)

fun argsFromPayload(json: String) = fromJson(
    fromJson(json, JsonObject::class.java)
    .get("args")
    .asJsonArray, Array<String>::class.java
)
