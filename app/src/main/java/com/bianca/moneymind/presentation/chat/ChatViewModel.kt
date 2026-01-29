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
        _uiState.update { it.copy(inputText = "") }

        // Check if we have pending transactions waiting for more info
        val pending = _uiState.value.pendingTransactions
        if (pending.isNotEmpty()) {
            handleFollowUpResponse(input, pending)
            return
        }

        // Process as new input
        processNewInput(input)
    }

    /**
     * Handle user's follow-up response to complete pending transactions
     */
    private fun handleFollowUpResponse(input: String, pending: List<ProcessedTransaction>) {
        // Check if input looks like new transaction(s) rather than a simple answer
        // e.g., "早餐50 坐車50" contains numbers and multiple items
        val hasMultipleItems = input.contains(Regex("[,，、]")) ||
                input.contains(Regex("\\d+.*\\d+"))
        val looksLikeNewInput = hasMultipleItems ||
                (input.contains(Regex("\\d")) && input.length > 10)

        if (looksLikeNewInput) {
            // Treat as new input, discard pending and re-parse with AI
            _uiState.update { it.copy(pendingTransactions = emptyList()) }
            processNewInput(input)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            // Determine what info the user provided
            val updatedTransactions = pending.map { processed ->
                val missingFields = processed.parsed.missingFields.toMutableList()
                var updatedParsed = processed.parsed

                // Check if user provided amount (number only, no other text)
                if ("amount" in missingFields) {
                    val amount = input.replace(Regex("[^0-9.]"), "").toDoubleOrNull()
                    if (amount != null) {
                        updatedParsed = updatedParsed.copy(amount = amount)
                        missingFields.remove("amount")
                    }
                }

                // Check if user provided category (text match)
                if ("categoryId" in missingFields) {
                    val categoryId = matchCategoryFromInput(input)
                    if (categoryId != null) {
                        updatedParsed = updatedParsed.copy(categoryId = categoryId)
                        missingFields.remove("categoryId")
                    }
                }

                processed.copy(
                    parsed = updatedParsed.copy(missingFields = missingFields),
                    isComplete = missingFields.isEmpty()
                )
            }

            // Check if still incomplete
            val stillIncomplete = updatedTransactions.filter { !it.isComplete }
            if (stillIncomplete.isNotEmpty()) {
                val nextMissing = stillIncomplete.flatMap { it.parsed.missingFields }.distinct()
                val followUp = when {
                    "amount" in nextMissing -> "請問金額是多少？"
                    "categoryId" in nextMissing -> "請問這筆消費的類別是什麼？"
                    else -> "還需要什麼資訊嗎？"
                }
                addAiMessage(followUp)
                _uiState.update {
                    it.copy(pendingTransactions = updatedTransactions, isProcessing = false)
                }
            } else {
                // All complete, show confirmation
                _uiState.update { it.copy(pendingTransactions = emptyList()) }
                addTransactionConfirmation(updatedTransactions)
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    /**
     * Process new input with AI (extracted for reuse)
     */
    private fun processNewInput(input: String) {
        addTypingIndicator()
        _uiState.update { it.copy(isProcessing = true) }

        viewModelScope.launch {
            processUserInputUseCase(input)
                .onSuccess { result ->
                    removeTypingIndicator()

                    if (result.transactions.isEmpty()) {
                        addAiMessage("抱歉，我無法理解這筆記錄。可以再說一次嗎？")
                    } else if (result.hasIncompleteTransactions && result.followUpQuestion != null) {
                        addAiMessage(result.followUpQuestion)
                        _uiState.update {
                            it.copy(pendingTransactions = result.transactions)
                        }
                    } else {
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

    /**
     * Match user input to a category
     */
    private fun matchCategoryFromInput(input: String): String? {
        val normalized = input.lowercase().trim()

        // Simple keyword matching for categories
        return when {
            normalized.contains("餐") || normalized.contains("吃") ||
            normalized.contains("飯") || normalized.contains("食") -> "expense_food"

            normalized.contains("交通") || normalized.contains("車") ||
            normalized.contains("油") || normalized.contains("捷運") -> "expense_transport"

            normalized.contains("購物") || normalized.contains("買") ||
            normalized.contains("網購") -> "expense_shopping"

            normalized.contains("娛樂") || normalized.contains("電影") ||
            normalized.contains("遊戲") -> "expense_entertainment"

            normalized.contains("生活") || normalized.contains("水電") ||
            normalized.contains("房租") -> "expense_living"

            normalized.contains("醫") || normalized.contains("藥") ||
            normalized.contains("看病") -> "expense_medical"

            normalized.contains("教育") || normalized.contains("學") ||
            normalized.contains("書") -> "expense_education"

            normalized.contains("薪") || normalized.contains("收入") -> "income_salary"

            else -> null
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
