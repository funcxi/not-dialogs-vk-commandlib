package dev.sirias.commandlib.service

import com.vk.api.sdk.actions.Messages
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.messages.Message
import dev.sirias.commandlib.BaseCommand
import dev.sirias.commandlib.model.sender.CommandSender
import dev.sirias.commandlib.handler.MessageHandler
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CommandService(
    id: Int,
    token: String,
    commandPrefixes: Array<out Char>,
    val commandSender: Class<out CommandSender>,
    val commandHandlers: List<(CommandSender, BaseCommand, Array<String>) -> Unit>,
    val exceptionHandler: ((CommandSender, Exception) -> Unit)?
) {
    private val commands: MutableMap<String, BaseCommand> = HashMap()
    private val messageHandler = MessageHandler(this, commandPrefixes)
    private val userActor: UserActor = UserActor(id, token)

    private val vk = VkApiClient(HttpTransportClient.getInstance())
    val vkMessages: Messages = vk.messages()
    init {
        Executors.newSingleThreadScheduledExecutor().apply {
            val longPollServer = vkMessages.getLongPollServer(userActor)
            var ts = longPollServer.execute().ts

            scheduleAtFixedRate({
                val eventQuery = vkMessages.getLongPollHistory(userActor).ts(ts)
                val messages = eventQuery.execute().messages.items

                if (messages.isNotEmpty()) {
                    ts = longPollServer.execute().ts

                    for (message in messages) {
                        if (message.isOut) return@scheduleAtFixedRate

                        messageHandler.handleMessage(message)
                    }
                }
            }, 100, 120, TimeUnit.MILLISECONDS)
        }
    }

    fun send() = vkMessages.send(userActor)

    fun registerCommands(vararg commands: BaseCommand) {
        for (command in commands) {
            if (exceptionHandler != null) command.exceptionHandler = exceptionHandler

            for (commandName in command.commandNames) this.commands[commandName] = command
        }
    }

    fun unregisterCommands(vararg commandNames: String) {
        for (commandName in commandNames) commands.remove(commandName)
    }

    fun unregisterCommands(vararg commands: BaseCommand) {
        for (command in commands)
            this.commands.remove(this.commands.entries
                .stream()
                .filter { it.value == command }
                .map { it.key }
                .findFirst()
                .orElse(null)
            )
    }

    fun getCommand(commandName: String?): BaseCommand? {
        for (command in commands.values) {
            if (command.commandNames.contains(commandName)) return command
        }

        return null
    }
}
