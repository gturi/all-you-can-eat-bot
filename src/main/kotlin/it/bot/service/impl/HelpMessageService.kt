package it.bot.service.impl

import it.bot.model.command.BotCommand
import it.bot.model.command.HelpMessageCommand
import it.bot.service.interfaces.CommandParserService
import it.bot.util.MessageUtils
import javax.enterprise.context.ApplicationScoped
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@ApplicationScoped
class HelpMessageService : CommandParserService() {

    override val botCommand = HelpMessageCommand()

    var supportedCommands: List<BotCommand> = listOf()

    override fun executeOperation(update: Update, matchResult: MatchResult): SendMessage {
        val commandsToString = supportedCommands.groupBy {
            it.commandType
        }.entries.joinToString("\n") { entry ->
            val commands = entry.value.joinToString("\n") {
                "${it.command} ${it.format}\n    - ${it.description}"
            }
            "\n<b>${entry.key.description}</b>\n$commands"
        }

        val messageText = "Commands supported by all you can eat bot:\n$commandsToString" +
                "\n\nNote: the value after ':' will be used when a command param has not been specified"

        return MessageUtils.createMessage(update, messageText).apply {
            enableHtml(true)
        }
    }
}
