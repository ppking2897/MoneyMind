package com.bianca.moneymind.presentation.chat

import com.bianca.moneymind.domain.usecase.ai.ProcessedTransaction

/**
 * Chat message types
 */
sealed class ChatMessage {
    abstract val id: String
    abstract val timestamp: Long

    /**
     * User's text message
     */
    data class UserMessage(
        override val id: String,
        override val timestamp: Long,
        val text: String
    ) : ChatMessage()

    /**
     * AI's text response
     */
    data class AiMessage(
        override val id: String,
        override val timestamp: Long,
        val text: String
    ) : ChatMessage()

    /**
     * AI shows parsed transactions for confirmation
     */
    data class TransactionConfirmation(
        override val id: String,
        override val timestamp: Long,
        val transactions: List<ProcessedTransaction>,
        val isConfirmed: Boolean = false
    ) : ChatMessage()

    /**
     * AI is typing indicator
     */
    data class AiTyping(
        override val id: String = "typing",
        override val timestamp: Long = System.currentTimeMillis()
    ) : ChatMessage()
}

/**
 * UI State for Chat Screen
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isProcessing: Boolean = false,
    val isListening: Boolean = false,
    val error: String? = null,
    val pendingTransactions: List<ProcessedTransaction> = emptyList()
) {
    val hasContent: Boolean
        get() = inputText.isNotBlank()

    val canSend: Boolean
        get() = hasContent && !isProcessing && !isListening
}
