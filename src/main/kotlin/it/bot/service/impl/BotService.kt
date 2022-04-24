package it.bot.service.impl

import io.quarkus.runtime.Startup
import it.bot.AllYouCanEatBot
import it.bot.service.interfaces.CommandParserService
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession


@Startup
@ApplicationScoped
class BotService(
    @ConfigProperty(name = "bot.token")
    private val botToken: String,
    @Inject val createOrderService: CreateOrderService,
    @Inject val joinOrderService: JoinOrderService,
    @Inject val leaveOrderService: LeaveOrderService,
    @Inject val addDishService: AddDishService
) {

    private val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
    private val commandParserServices: List<CommandParserService> = listOf(
        createOrderService, joinOrderService, leaveOrderService, addDishService
    )

    init {
        botsApi.registerBot(AllYouCanEatBot(botToken, commandParserServices))
    }
}