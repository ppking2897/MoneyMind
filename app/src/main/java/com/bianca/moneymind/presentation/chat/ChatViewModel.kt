package com.bianca.moneymind.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.model.InputType
import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.repository.TransactionRepository
import com.bianca.moneymind.domain.usecase.ai.ProcessUserInputUseCase
import com.bianca.moneymind.domain.usecase.ai.ProcessedTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val processUserInputUseCase: ProcessUserInputUseCase,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Add welcome message
        addAiMessage("嗨！說說你花了什麼，或拍張收據給我")
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSendMessage() {
        val input = _uiState.value.inputText.trim()
        if (input.isBlank() || _uiState.value.isProcessing) return

        // Add user message to chat
        addUserMessage(input)

        // Clear input
        _uiState.update { it.copy(inputText = "", isProcessing = true) }

        // Show typing indicator
        addTypingIndicator()

        // Process with AI
        viewModelScope.launch {
            processUserInputUseCase(input)
                .onSuccess { result ->
                    removeTypingIndicator()

                    if (result.transactions.isEmpty()) {
                        addAiMessage("抱歉，我無法理解這筆記錄。可以再說一次嗎？")
                    } else if (result.hasIncompleteTransactions && result.followUpQuestion != null) {
                        // Need more info
                        addAiMessage(result.followUpQuestion)
                        _uiState.update {
                            it.copy(pendingTransactions = result.transactions)
                        }
                    } else {
                        // Show transactions for confirmation
                        addTransactionConfirmation(result.transactions)
                    }

                    _uiState.update { it.copy(isProcessing = false) }
                }
                .onError { exception ->
                    removeTypingIndicator()
                    addAiMessage("處理時發生錯誤：${exception.message}")
                    _uiState.update { it.copy(isProcessing = false, error = exception.message) }
                }
        }
    }

    fun onConfirmTransactions(transactions: List<ProcessedTransaction>) {
        viewModelScope.launch {
            var successCount = 0

            for (processed in transactions) {
                val parsed = processed.parsed

                // Skip if amount is missing
                if (parsed.amount == null) continue

                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    type = parsed.type,
                    amount = parsed.amount,
                    description = parsed.description,
                    categoryId = processed.categorization.categoryId ?: "expense_other",
                    date = parsed.date,
                    createdAt = Instant.now(),
                    inputType = InputType.NLP,
                    receiptImagePath = null,
                    merchantName = parsed.merchantName,
                    note = null,
                    rawInput = parsed.description
                )

                transactionRepository.add(transaction)
                successCount++
            }

            // Update confirmation status in messages
            _uiState.update { state ->
                val updatedMessages = state.messages.map { message ->
                    if (message is ChatMessage.TransactionConfirmation &&
                        message.transactions == transactions
                    ) {
                        message.copy(isConfirmed = true)
                    } else {
                        message
                    }
                }
                state.copy(
                    messages = updatedMessages,
                    pendingTransactions = emptyList()
                )
            }

            if (successCount > 0) {
                addAiMessage("已記錄 $successCount 筆交易！")
            }
        }
    }

    fun onCancelTransactions() {
        // Remove pending transactions
        _uiState.update { state ->
            val updatedMessages = state.messages.filterNot { message ->
                message is ChatMessage.TransactionConfirmation && !message.isConfirmed
            }
            state.copy(
                messages = updatedMessages,
                pendingTransactions = emptyList()
            )
        }
        addAiMessage("好的，已取消。有什麼其他要記錄的嗎？")
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onSpeechResult(text: String) {
        _uiState.update { it.copy(inputText = text, isListening = false) }
    }

    fun onSpeechError(error: String) {
        _uiState.update { it.copy(error = error, isListening = false) }
    }

    fun onListeningStateChange(isListening: Boolean) {
        _uiState.update { it.copy(isListening = isListening) }
    }

    /**
     * Handle receipt scan result from CameraScreen
     */
    fun onReceiptSaved(amount: Double, description: String) {
        val formattedAmount = if (amount == amount.toLong().toDouble()) {
            amount.toLong().toString()
        } else {
            String.format("%.2f", amount)
        }

        addAiMessage("已透過收據掃描記錄：\n$description $$formattedAmount")
    }

    private fun addUserMessage(text: String) {
        val message = ChatMessage.UserMessage(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            text = text
        )
        _uiState.update { state ->
            state.copy(messages = state.messages + message)
        }
    }

    private fun addAiMessage(text: String) {
        val message = ChatMessage.AiMessage(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            text = text
        )
        _uiState.update { state ->
            state.copy(messages = state.messages + message)
        }
    }

    private fun addTransactionConfirmation(transactions: List<ProcessedTransaction>) {
        val message = ChatMessage.TransactionConfirmation(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            transactions = transactions
        )
        _uiState.update { state ->
            state.copy(messages = state.messages + message)
        }
    }

    private fun addTypingIndicator() {
        _uiState.update { state ->
            state.copy(messages = state.messages + ChatMessage.AiTyping())
        }
    }

    private fun removeTypingIndicator() {
        _uiState.update { state ->
            state.copy(messages = state.messages.filterNot { it is ChatMessage.AiTyping })
        }
    }
}
