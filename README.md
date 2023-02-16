# User VK Command library 

Данная библиотека предназначена для упрощения создания команд **_исключительно_** для страничных [VK](https://vk.com/) ботов

[![](https://jitpack.io/v/funcxi/not-dialogs-vk-commandlib.svg)](https://jitpack.io/#funcxi/not-dialogs-vk-commandlib)

### Подключение библиотеки в проект:
#### Gradle Groovy DSL:
``` groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.funcxi:not-dialogs-vk-commandlib:command-lib-latest'
}
```

#### Gradle Kotlin DSL:
``` groovy
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.funcxi:not-dialogs-vk-commandlib:$command-lib-latest")
}
```

#### Maven:
``` xml
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>

<dependency>
  <groupId>com.github.funcxi</groupId>
  <artifactId>not-dialogs-vk-commandlib</artifactId>
  <version>command-lib-latest</version>
</dependency>
```

### Используемые зависимости:
* [VK Java SDK](https://github.com/VKCOM/vk-java-sdk)
* [Gson](https://github.com/google/gson)
* [Guava](https://github.com/google/guava)

### Использование библиотеки:
Первый шаг для начало работы с библиотекой — Создание сервиса команд (подключение к VKAPI) для дальнейшего управления командами:

``` kotlin
CommandServiceBuilder.create()
    .commandPrefixes('/', '!')
    .id(0)
    .token("ваш_токен")
    .exceptionHandler<CommandSender> { commandSender, exception ->
        commandSender.sendReplyMessage("При обработке команды произошла неизвестная ошибка: ${exception.message}")
        exception.printStackTrace()
    }
    .commandHandler<CommandSender> { commandSender, command, args ->
        println("Пользователь ID:${commandSender.message.fromId} использовал команду/субкоманду /${command.commandNames[0]}
         ${args.contentToString()}")
    }
    .commandHandler<CommandSender> { commandSender, _, _ -> println("CHAT:ID:${commandSender.message.fromId}") }
    .build()
    .apply {
        registerCommands(TestCommand())
    }
```

Теперь приступим к созданию самих команд. Библиотека имеет возможность создавать обычные команды и диалоговые, и даже их совмещать:

``` kotlin
class TestCommand : BaseCommand("test", "тест") {
    @MinArg(1, "Недостаточно аргументов для использования команды")
    @Default // Назначает данную функцию дефолтной для команды. Используется когда подкоманда не найдена или при пустых аргументах (/test)
    @Cooldown(5, TimeUnit.SECONDS, "test_default") // время - в чем измеряется время - ключ задержки
    fun default(commandSender: CommandSender, args: Array<String>) {
        commandSender.sendMessage("текст: ${args.contentToString()}")
    }
    
    @SubCommand("test")
    fun test(commandSender: CommandSender) {
        commandSender.sendMessage("Привет!")
    }
    
    @ExceptionHandler // обработчик ошибок для этой команды
    fun exceptionHandler(): (CommandSender, Exception) -> Unit = {
            commandSender: CommandSender,
            exception: Exception-> commandSender.sendReplyMessage("Ошибка: ${exception.message}")
    }
}
```

При надобности можно в аргументы функции добавить класс **Message** от VK Java SDK и через него получать нужную информацию о сообщении, например:
``` kotlin
@Default
fun default(commandSender: CommandSender, message: Message) {
    val replyMessage = message.replyMessage
    
    if (replyMessage == null) {
        commandSender.sendReplyMessage("Ответьте на нужное сообщение для получения ID пользователя")
        return
    }
    
    commandSender.sendReplyMessage("ID пользователя: ${replyMessage.fromId}")
}
```

Так же библиотека имеет возможность реализации **собственного CommandSender'a**:
``` kotlin
class CustomCommandSender(
    override val message: Message,
    override val args: Array<String>,
    override val commandService: CommandService,
    override val command: BaseCommand,
    override val commandEntity: AbstractCommandEntity
) : CommandSender(message, args, commandService, command, commandEntity) {
    fun sendErrorMessage(message: String, vararg objects: Any) = sendMessage("⛔ $message", objects)

    fun sendInfoMessage(message: String, vararg objects: Any) = sendMessage("✅ $message", objects)
}
```

Теперь чтобы его использовать в командах, надо его зарегистрировать:
``` kotlin
CommandServiceBuilder.create()
    .commandPrefixes('/', '!')
    .id(0)
    .token("ваш_токен")
    .commandSender(CustomCommandSender::class.java)
    .build()
    .apply {
       // code
    }
```

``` kotlin
@Default
fun default(commandSender: CustomCommandSender) {
    commandSender.sendInfoMessage("Hello world!")
}
```
