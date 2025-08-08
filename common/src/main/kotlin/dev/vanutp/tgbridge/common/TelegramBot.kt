package dev.vanutp.tgbridge.common

import com.google.gson.annotations.SerializedName
import dev.vanutp.tgbridge.common.ConfigManager.config
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Duration
import java.util.concurrent.TimeoutException
import kotlin.reflect.KClass

data class TgUser(
    val id: Long,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String?,
    val username: String?,
) {
    val fullName
        get() = (firstName + " " + (lastName ?: "")).trim()
}

data class TgChat(
    val id: Long,
    val title: String = "",
    val username: String? = null,
)

class TgAny

data class TgPoll(
    val question: String,
)

data class TgMessageOrigin(
    @SerializedName("sender_user")
    val senderUser: TgUser? = null,
    @SerializedName("sender_user_name")
    val senderUserName: String? = null,
    @SerializedName("sender_chat")
    val senderChat: TgChat? = null,
    val chat: TgChat? = null,
)

interface TgMessageMedia {
    val animation: TgAny?
    val photo: List<TgAny>?
    val audio: TgAny?
    val document: TgAny?
    val sticker: TgAny?
    val video: TgAny?
    val videoNote: TgAny?
    val voice: TgAny?
    val poll: TgPoll?
}

data class TgExternalReplyInfo(
    val origin: TgMessageOrigin,
    val chat: TgChat? = null,
    override val animation: TgAny? = null,
    override val photo: List<TgAny>? = null,
    override val audio: TgAny? = null,
    override val document: TgAny? = null,
    override val sticker: TgAny? = null,
    override val video: TgAny? = null,
    @SerializedName("video_note")
    override val videoNote: TgAny? = null,
    override val voice: TgAny? = null,
    override val poll: TgPoll? = null,
) : TgMessageMedia {
    val senderName
        get() = origin.senderUser?.fullName
            ?: origin.senderUserName
            ?: origin.senderChat?.title
            ?: origin.chat?.title
            ?: ""
}

data class TgTextQuote(
    val text: String,
    val entities: List<TgEntity>? = emptyList(),
)

enum class TgEntityType {
    @SerializedName("mention")
    MENTION,
    @SerializedName("hashtag")
    HASHTAG,
    @SerializedName("cashtag")
    CASHTAG,
    @SerializedName("bot_command")
    BOT_COMMAND,
    @SerializedName("url")
    URL,
    @SerializedName("email")
    EMAIL,
    @SerializedName("phone_number")
    PHONE_NUMBER,
    @SerializedName("bold")
    BOLD,
    @SerializedName("italic")
    ITALIC,
    @SerializedName("underline")
    UNDERLINE,
    @SerializedName("strikethrough")
    STRIKETHROUGH,
    @SerializedName("spoiler")
    SPOILER,
    @SerializedName("blockquote")
    BLOCKQUOTE,
    @SerializedName("expandable_blockquote")
    EXPANDABLE_BLOCKQUOTE,
    @SerializedName("code")
    CODE,
    @SerializedName("pre")
    PRE,
    @SerializedName("text_link")
    TEXT_LINK,
    @SerializedName("text_mention")
    TEXT_MENTION,
    @SerializedName("custom_emoji")
    CUSTOM_EMOJI,
}

data class TgEntity(
    val type: TgEntityType,
    val offset: Int,
    val length: Int,
    val url: String? = null,
)

data class TgVideoChatParticipantsInvited(
    val users: List<TgUser>,
)

data class TgMessage(
    val chat: TgChat,
    @SerializedName("message_id")
    val messageId: Int,
    val from: TgUser? = null,
    @SerializedName("sender_chat")
    val senderChat: TgChat? = null,
    @SerializedName("forward_from")
    val forwardFrom: TgUser? = null,
    @SerializedName("forward_from_chat")
    val forwardFromChat: TgChat? = null,
    @SerializedName("reply_to_message")
    val replyToMessage: TgMessage? = null,
    @SerializedName("external_reply")
    val externalReply: TgExternalReplyInfo? = null,
    val quote: TgTextQuote? = null,
    @SerializedName("message_thread_id")
    val messageThreadId: Int? = null,
    @SerializedName("is_topic_message")
    val isTopicMessage: Boolean? = null,
    @SerializedName("author_signature")
    val authorSignature: String? = null,
    val text: String? = null,
    @SerializedName("entities")
    val textEntities: List<TgEntity>? = null,
    val caption: String? = null,
    @SerializedName("caption_entities")
    val captionEntities: List<TgEntity>? = null,
    override val animation: TgAny? = null,
    override val photo: List<TgAny>? = null,
    override val audio: TgAny? = null,
    override val document: TgAny? = null,
    override val sticker: TgAny? = null,
    override val video: TgAny? = null,
    @SerializedName("video_note")
    override val videoNote: TgAny? = null,
    override val voice: TgAny? = null,
    override val poll: TgPoll? = null,
    @SerializedName("pinned_message")
    val pinnedMessage: TgMessage? = null,

    @SerializedName("new_chat_members")
    val newChatMembers: List<TgUser>? = null,
    @SerializedName("left_chat_member")
    val leftChatMember: TgUser? = null,

    @SerializedName("video_chat_scheduled")
    val videoChatScheduled: TgAny? = null,
    @SerializedName("video_chat_started")
    val videoChatStarted: TgAny? = null,
    @SerializedName("video_chat_ended")
    val videoChatEnded: TgAny? = null,
    @SerializedName("video_chat_participants_invited")
    val videoChatParticipantsInvited: TgVideoChatParticipantsInvited? = null,
) : TgMessageMedia {
    val senderName
        get() = authorSignature
            ?: senderChat?.title
            ?: from?.fullName
            ?: ""
    val effectiveText
        get() = text ?: caption
    val entities
        get() = textEntities ?: captionEntities ?: emptyList()
}

data class TgUpdate(
    @SerializedName("update_id")
    val updateId: Int,
    val message: TgMessage? = null,
)

data class TgResponse<T>(
    val ok: Boolean,
    val result: T?,
    val description: String?,
)

data class TgSendMessageRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("text")
    val text: String,
    @SerializedName("entities")
    val entities: List<TgEntity>? = null,
    @SerializedName("reply_to_message_id")
    val replyToMessageId: Int? = null,
    @SerializedName("parse_mode")
    val parseMode: String? = "HTML",
    @SerializedName("disable_web_page_preview")
    val disableWebPagePreview: Boolean = true,
)

data class TgEditMessageRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("message_id")
    val messageId: Int,
    @SerializedName("text")
    val text: String,
    @SerializedName("entities")
    val entities: List<TgEntity>? = null,
    @SerializedName("parse_mode")
    val parseMode: String? = "HTML",
    @SerializedName("disable_web_page_preview")
    val disableWebPagePreview: Boolean = true,
)

data class TgDeleteMessageRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("message_id")
    val messageId: Int,
)

interface TgApi {
    @GET("getMe")
    suspend fun getMe(): TgResponse<TgUser>

    @POST("sendMessage")
    suspend fun sendMessage(@Body data: TgSendMessageRequest): TgResponse<TgMessage>

    @POST("editMessageText")
    suspend fun editMessageText(@Body data: TgEditMessageRequest): TgResponse<TgMessage>

    @POST("deleteMessage")
    suspend fun deleteMessage(@Body data: TgDeleteMessageRequest): TgResponse<Boolean>

    @GET("getUpdates")
    suspend fun getUpdates(
        @Query("offset") offset: Int,
        @Query("timeout") timeout: Int,
        @Query("allowed_updates") allowedUpdates: List<String> = listOf("message"),
    ): TgResponse<List<TgUpdate>>

    @POST("deleteWebhook")
    suspend fun deleteWebhook(): TgResponse<Boolean>
}

const val POLL_TIMEOUT_SECONDS = 60

class TelegramBot(botApiUrl: String, botToken: String, private val logger: ILogger) {
    private val okhttpClient = OkHttpClient.Builder()
        .readTimeout(Duration.ofSeconds((POLL_TIMEOUT_SECONDS + 10).toLong()))
        .build()
    private val client = Retrofit.Builder()
        .client(okhttpClient)
        .baseUrl("$botApiUrl/bot$botToken/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TgApi::class.java)
    private var pollTask: Job? = null
    private val commandHandlers: MutableList<suspend (TgMessage) -> Boolean> = mutableListOf()
    private val messageHandlers: MutableList<suspend (TgMessage) -> Unit> = mutableListOf()
    lateinit var me: TgUser
        private set


    fun registerMessageHandler(handler: suspend (TgMessage) -> Unit) {
        messageHandlers.add(handler)
    }

    fun registerCommandHandler(command: String, handler: suspend (TgMessage) -> Unit) {
        val cmdRegex = Regex("^/$command(@${me.username})?(\\s|$)", RegexOption.IGNORE_CASE)
        commandHandlers.add {
            if (cmdRegex.matches(it.effectiveText ?: "")) {
                handler(it)
                return@add true
            } else {
                return@add false
            }
        }
    }

    suspend fun init() {
        retriableCall { client.deleteWebhook() }
        me = retriableCall { client.getMe() }
    }

    fun startPolling(scope: CoroutineScope) {
        if (pollTask != null) {
            throw IllegalStateException("polling already started")
        }
        pollTask = scope.launch {
            var offset = -1
            while (true) {
                try {
                    val updates = call {
                        client.getUpdates(
                            offset,
                            timeout = POLL_TIMEOUT_SECONDS,
                        )
                    }
                    if (updates.isEmpty()) {
                        continue
                    }
                    offset = updates.last().updateId + 1
                    updates.forEach { update ->
                        if (update.message == null) {
                            return@forEach
                        }
                        for (handler in commandHandlers) {
                            if (handler.invoke(update.message)) {
                                return@forEach
                            }
                        }
                        messageHandlers.forEach {
                            it.invoke(update.message)
                        }
                    }
                } catch (e: Exception) {
                    when (e) {
                        is CancellationException -> break
                        else -> {
                            logger.error(e.message.toString(), e)
                            delay(1000)
                        }
                    }
                }
            }
            logger.info("pollTask finished")
        }
    }

    suspend fun recoverPolling(scope: CoroutineScope) {
        val task = pollTask
        if (task != null) {
            if (!task.isCompleted) {
                task.cancelAndJoin()
            }
            pollTask = null
        }
        startPolling(scope)
    }

    suspend fun shutdown() {
        pollTask?.cancelAndJoin()
        okhttpClient.dispatcher().executorService().shutdown()
        okhttpClient.connectionPool().evictAll()
    }

    private suspend fun <T> call(f: suspend () -> TgResponse<T>): T {
        try {
            return f().result!!
        } catch (e: HttpException) {
            val resp = e.response() ?: throw e
            throw Exception("Telegram exception: ${resp.errorBody()?.string() ?: "no response body"}")
        }
    }

    private suspend fun <T> retriableCall(f: suspend () -> TgResponse<T>): T {
        val retryConf = config.advanced.connectionRetry
        return withRetry(
            maxAttempts = retryConf.maxAttempts,
            initialDelay = retryConf.initialDelay,
            maxDelay = retryConf.maxDelay,
            retryExceptions = setOf(
                UnknownHostException::class,
                ConnectException::class,
                SocketTimeoutException::class,
                SocketException::class,
                TimeoutException::class,
            )
        ) {
            call(f)
        }
    }

    private suspend fun <T> withRetry(
        maxAttempts: Int = 3,
        initialDelay: Long = 1000L,
        maxDelay: Long = 300000L,
        retryExceptions: Set<KClass<out Exception>> = setOf(Exception::class),
        operation: suspend () -> T
    ): T {
        var attempt = 0
        val infiniteRetries = maxAttempts <= 0

        while (true) {
            try {
                return operation()
            } catch (e: Exception) {
                if (!retryExceptions.contains(e::class)) {
                    logger.error("Not retriable exception", e)
                    throw e
                }
                attempt++

                if (!infiniteRetries && attempt >= maxAttempts) {
                    logger.error("Operation failed after $maxAttempts attempts", e)
                    throw e
                }

                val delay = minOf(initialDelay * (1L shl (attempt - 1)), maxDelay)
                val attemptText = if (infiniteRetries) "attempt $attempt" else "attempt $attempt/$maxAttempts"
                logger.warn("Operation failed ($attemptText), retrying in ${delay / 1000} seconds: ${e.javaClass.canonicalName}: ${e.message}")
                delay(delay)
            }
        }
    }

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        entities: List<TgEntity>? = null,
        replyToMessageId: Int? = null,
        parseMode: String? = "HTML",
        disableWebPagePreview: Boolean = true,
    ) = retriableCall {
        client.sendMessage(
            TgSendMessageRequest(
                chatId,
                text,
                entities,
                replyToMessageId,
                parseMode,
                disableWebPagePreview
            )
        )
    }

    suspend fun editMessageText(
        chatId: Long,
        messageId: Int,
        text: String,
        entities: List<TgEntity>? = null,
        parseMode: String? = "HTML",
        disableWebPagePreview: Boolean = true,
    ) = retriableCall {
        client.editMessageText(
            TgEditMessageRequest(
                chatId,
                messageId,
                text,
                entities,
                parseMode,
                disableWebPagePreview
            )
        )
    }

    suspend fun deleteMessage(chatId: Long, messageId: Int) = retriableCall {
        client.deleteMessage(TgDeleteMessageRequest(chatId, messageId))
    }
}
