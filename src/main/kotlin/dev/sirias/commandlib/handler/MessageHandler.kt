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

        val fromId = message.fromId

        if (fromId > 0) return

        val command = commandService.getCommand(commandArgs[0].substring(1))?: return
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
