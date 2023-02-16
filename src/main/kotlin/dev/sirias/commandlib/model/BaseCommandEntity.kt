package dev.sirias.commandlib.model

import dev.sirias.commandlib.model.data.CommandContentData
import dev.sirias.commandlib.utility.execute
import java.lang.reflect.Method

sealed class BaseCommandEntity(
    open val method: Method,
    open val commandContent: CommandContentData
) {
    fun execute(vararg parameters: Any) = method.execute(*(mutableListOf(*parameters, this).toTypedArray()))
}

data class CommandEntity(
    override val method: Method,
    override val commandContent: CommandContentData
) : BaseCommandEntity(method, commandContent)

data class DialogStateEntity(
    override val method: Method,
    val state: String,
    val nextState: String,
    override val commandContent: CommandContentData
) : BaseCommandEntity(method, commandContent)

data class SubCommandEntity(
    override val method: Method,
    val commandNames: Array<out String>,
    override val commandContent: CommandContentData
) : BaseCommandEntity(method, commandContent)