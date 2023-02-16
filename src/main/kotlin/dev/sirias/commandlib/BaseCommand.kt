package dev.sirias.commandlib

import dev.sirias.commandlib.annotation.Default
import dev.sirias.commandlib.annotation.ExceptionHandler
import dev.sirias.commandlib.annotation.SubCommand
import dev.sirias.commandlib.model.CommandEntity
import dev.sirias.commandlib.model.SubCommandEntity
import dev.sirias.commandlib.model.data.CommandContentData
import dev.sirias.commandlib.model.sender.CommandSender
import java.util.*

abstract class BaseCommand(open vararg val commandNames: String) {
    var defaultMean: CommandEntity? = null
    val subCommands: MutableMap<String, SubCommandEntity> = HashMap()

    val commandContent: CommandContentData = CommandContentData(this)

    var exceptionHandler: ((CommandSender, Exception) -> Unit)? = null

    val messageIds: MutableMap<Int, UUID> = HashMap()

    init {
        javaClass.apply {

            for (method in declaredMethods) {
                method.isAccessible = true

                if (method.isAnnotationPresent(Default::class.java))
                    defaultMean = CommandEntity(
                        method,
                        CommandContentData(method)
                    )

                if (method.isAnnotationPresent(ExceptionHandler::class.java))
                    exceptionHandler = method.invoke(this@BaseCommand) as (CommandSender, Exception) -> Unit

                val commandContent = CommandContentData(method)


                if (method.isAnnotationPresent(SubCommand::class.java)) {
                    val subCommand = method.getAnnotation(SubCommand::class.java)

                    for (commandName in subCommand.commandNames)
                        subCommands[commandName] = SubCommandEntity(
                            method,
                            subCommand.commandNames,
                            commandContent
                        )
                }
            }
        }
    }
}
