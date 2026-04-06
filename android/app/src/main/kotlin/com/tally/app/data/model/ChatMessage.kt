package com.tally.app.data.model

import java.util.UUID

enum class MessageRole { USER, ASSISTANT }

enum class MessageStatus { SENDING, STREAMING, DONE, ERROR }

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val status: MessageStatus = MessageStatus.DONE,
)
