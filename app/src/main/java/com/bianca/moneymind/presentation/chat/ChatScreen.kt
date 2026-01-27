package com.bianca.moneymind.presentation.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.usecase.ai.ProcessedTransaction
import com.bianca.moneymind.ui.theme.MoneyMindTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    receiptSavedAmount: Double? = null,
    receiptSavedDescription: String? = null,
    onReceiptResultHandled: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle receipt scan result
    LaunchedEffect(receiptSavedAmount, receiptSavedDescription) {
        if (receiptSavedAmount != null && receiptSavedDescription != null) {
            viewModel.onReceiptSaved(receiptSavedAmount, receiptSavedDescription)
            onReceiptResultHandled()
        }
    }

    // Speech recognition helper
    val speechHelper = remember {
        SpeechRecognitionHelper(
            context = context,
            onResult = viewModel::onSpeechResult,
            onError = viewModel::onSpeechError,
            onListening = viewModel::onListeningStateChange
        )
    }

    // Clean up speech recognizer
    DisposableEffect(Unit) {
        onDispose {
            speechHelper.destroy()
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechHelper.startListening()
        } else {
            viewModel.onSpeechError("請允許麥克風權限以使用語音輸入")
        }
    }

    val onMicClick: () -> Unit = {
        if (uiState.isListening) {
            speechHelper.stopListening()
        } else {
            // Check permission
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    speechHelper.startListening()
                }
                else -> {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    ChatContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onInputChange = viewModel::onInputChange,
        onSendMessage = viewModel::onSendMessage,
        onCameraClick = onNavigateToCamera,
        onMicClick = onMicClick,
        onConfirmTransactions = viewModel::onConfirmTransactions,
        onCancelTransactions = viewModel::onCancelTransactions,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatContent(
    uiState: ChatUiState,
    onNavigateBack: () -> Unit,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onCameraClick: () -> Unit,
    onMicClick: () -> Unit,
    onConfirmTransactions: (List<ProcessedTransaction>) -> Unit,
    onCancelTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = uiState.isListening
    val listState = rememberLazyListState()

    // Scroll to bottom when new message arrives
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI 記帳助手",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.messages,
                    key = { it.id }
                ) { message ->
                    when (message) {
                        is ChatMessage.UserMessage -> {
                            UserMessageBubble(message = message)
                        }

                        is ChatMessage.AiMessage -> {
                            AiMessageBubble(message = message)
                        }

                        is ChatMessage.TransactionConfirmation -> {
                            TransactionConfirmationCard(
                                message = message,
                                onConfirm = { onConfirmTransactions(message.transactions) },
                                onCancel = onCancelTransactions
                            )
                        }

                        is ChatMessage.AiTyping -> {
                            TypingIndicator()
                        }
                    }
                }
            }

            // Input area
            ChatInputBar(
                inputText = uiState.inputText,
                isProcessing = uiState.isProcessing,
                isListening = isListening,
                onInputChange = onInputChange,
                onSendMessage = onSendMessage,
                onCameraClick = onCameraClick,
                onMicClick = onMicClick
            )
        }
    }
}

@Composable
private fun UserMessageBubble(
    message: ChatMessage.UserMessage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = formatTime(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun AiMessageBubble(
    message: ChatMessage.AiMessage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatTime(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun TransactionConfirmationCard(
    message: ChatMessage.TransactionConfirmation,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (message.isConfirmed) "已記錄：" else "確認記錄以下 ${message.transactions.size} 筆？",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                message.transactions.forEach { processed ->
                    TransactionItem(processed = processed)
                    if (processed != message.transactions.last()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                if (!message.isConfirmed) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onCancel) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = onConfirm) {
                            Text("確認")
                        }
                    }
                }
            }
        }
        Text(
            text = formatTime(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun TransactionItem(
    processed: ProcessedTransaction,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = processed.displayDescription,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = processed.parsed.date.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val amountText = if (processed.parsed.type == TransactionType.EXPENSE) {
            "-$${processed.parsed.amount?.toInt() ?: "?"}"
        } else {
            "+$${processed.parsed.amount?.toInt() ?: "?"}"
        }
        val amountColor = if (processed.parsed.type == TransactionType.EXPENSE) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        }

        Text(
            text = amountText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

@Composable
private fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    inputText: String,
    isProcessing: Boolean,
    isListening: Boolean,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onCameraClick: () -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(if (isListening) "正在聆聽..." else "輸入訊息...")
            },
            enabled = !isProcessing && !isListening,
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSendMessage() })
        )

        IconButton(onClick = onCameraClick, enabled = !isProcessing && !isListening) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = "拍照"
            )
        }

        IconButton(onClick = onMicClick, enabled = !isProcessing) {
            Icon(
                imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                contentDescription = if (isListening) "停止語音輸入" else "語音輸入",
                tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = onSendMessage,
            enabled = inputText.isNotBlank() && !isProcessing && !isListening
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "發送"
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
private fun ChatContentPreview() {
    MoneyMindTheme {
        ChatContent(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage.AiMessage(
                        id = "1",
                        timestamp = System.currentTimeMillis(),
                        text = "嗨！說說你花了什麼，或拍張收據給我"
                    ),
                    ChatMessage.UserMessage(
                        id = "2",
                        timestamp = System.currentTimeMillis(),
                        text = "午餐便當 85 元"
                    )
                ),
                inputText = "",
                isListening = false
            ),
            onNavigateBack = {},
            onInputChange = {},
            onSendMessage = {},
            onCameraClick = {},
            onMicClick = {},
            onConfirmTransactions = {},
            onCancelTransactions = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatContentListeningPreview() {
    MoneyMindTheme {
        ChatContent(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage.AiMessage(
                        id = "1",
                        timestamp = System.currentTimeMillis(),
                        text = "嗨！說說你花了什麼，或拍張收據給我"
                    )
                ),
                inputText = "",
                isListening = true
            ),
            onNavigateBack = {},
            onInputChange = {},
            onSendMessage = {},
            onCameraClick = {},
            onMicClick = {},
            onConfirmTransactions = {},
            onCancelTransactions = {}
        )
    }
}
