package it.bot

import io.quarkus.logging.Log
import it.bot.service.interfaces.CommandParserService
import it.bot.util.MessageUtils
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class AllYouCanEatBot(
    private val botUsername: String,
    private val botToken: String,
    private val commandParserServices: List<CommandParserService>,
) : TelegramLongPollingBot() {

    override fun onUpdateReceived(update: Update) {
        if (Log.isDebugEnabled()) {
            Log.debug("update received $update")
        }

        if (update.hasMessage() && update.message.hasText()) {
            try {
                handleCommand(update)
            } catch (exception: Exception) {
                handleUnexpectedError(update, exception)
            }
        }
    }

    private fun handleCommand(update: Update) {
        commandParserServices.find {
            matches(it.command, MessageUtils.getChatMessage(update))
        }.let {
            parseUpdate(it, update)
        }.also {
            sendMessage(it)
        }
    }

    private fun handleUnexpectedError(update: Update, exception: Exception) {
        Log.error(
            "unexpected error occurred: " +
                    "chatId ${MessageUtils.getChatId(update)}, " +
                    "userId ${MessageUtils.getTelegramUserId(update)}, " +
                    "message '${MessageUtils.getChatMessage(update)}' ",
            exception
        )
        val message = MessageUtils.createMessage(
            update, "Error: something unexpected happened. Reason: ${exception.message}"
        )
        sendMessage(message)
    }

    private fun matches(botCommand: String, text: String): Boolean {
        return text.startsWith("$botCommand ") or (text == botCommand)
    }

    private fun parseUpdate(commandParserService: CommandParserService?, update: Update): SendMessage? {
        return if (commandParserService == null) {
            Log.error("no command support for '${MessageUtils.getChatMessage(update)}'")
            null
        } else {
            commandParserService.parseUpdate(update)
        }
    }

    private fun sendMessage(message: SendMessage?) {
        message?.also {
            try {
                execute(it)
            } catch (e: TelegramApiException) {
                Log.error(e)
            }
        } ?: Log.info("no message to send")
    }

    override fun getBotUsername(): String {
        return botUsername
    }

    override fun getBotToken(): String {
        return botToken
    }
}
